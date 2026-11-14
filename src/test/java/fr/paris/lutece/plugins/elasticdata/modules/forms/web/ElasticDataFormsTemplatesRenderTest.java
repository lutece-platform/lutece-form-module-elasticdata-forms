/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.elasticdata.modules.forms.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalQuestionIndexation;
import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalStatusIndexation;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Renders the elasticdata-forms back office pages with HTML auto-escaping (LUT-33153): the titles and descriptions of
 * the forms, the titles of their steps and questions and the names of the workflow states are text typed by an
 * administrator, written once escaped; the edit button of a form is named after it; the checkboxes of a step and the
 * checkboxes of the states are grouped under a legend; the selection already saved checks its boxes. The core macros
 * the templates call are replaced by stand-ins that write their arguments (escaped) and their nested content: only the
 * FreeMarker engine is needed.
 */
public class ElasticDataFormsTemplatesRenderTest
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String TEMPLATE_MANAGE = "admin/plugins/elasticdata/modules/forms/manage_forms_indexation.html";
    private static final String TEMPLATE_MODIFY = "admin/plugins/elasticdata/modules/forms/modify_form_indexation.html";

    /** Stand-ins of the core macros the templates call. btnGroupRadioCheckbox refuses a labelParams that is not a map. */
    private static final String MACROS = "macros.ftl";
    private static final String MACROS_SOURCE = "<#macro box rest...><#nested></#macro><#macro boxBody rest...><#nested></#macro>"
            + "<#macro boxHeader title='' rest...><h2>${title}</h2><#nested></#macro>"
            + "<#macro messages infos=[] rest...></#macro>"
            + "<#macro table rest...><table><#nested></table></#macro><#macro tableHeadBodySeparator rest...></#macro>"
            + "<#macro aButton href='' title='' params={} rest...><a href=\"${href}\"<#list params as key, param> ${key}=\"${param}\"</#list>>${title}</a></#macro>"
            + "<#macro tform action='' rest...><form action=\"${action}\"><#nested></form></#macro>"
            + "<#macro input name='' value='' rest...><input name=\"${name}\" value=\"${value}\"></#macro>"
            + "<#macro fieldSet legend='' rest...><fieldset><legend>${legend}</legend><#nested></fieldset></#macro>"
            + "<#macro btnGroupRadioCheckbox name='' value='' labelKey='' labelParams={} checked=false rest...>"
            + "<#if !labelParams?is_hash><#stop 'labelParams is not a map'></#if>"
            + "<label><input type=\"checkbox\" name=\"${name}\" value=\"${value}\"<#if checked> checked</#if>>${labelKey}</label></#macro>"
            + "<#macro actionButtons rest...></#macro>";

    private static final String TITLE = "Form <b>\"x\"</b>";
    private static final String TITLE_ESCAPED = "Form &lt;b&gt;&quot;x&quot;&lt;/b&gt;";

    /**
     * Renders a template of the module with the configuration of AbstractFreeMarkerTemplateService for HTML templates.
     *
     * @param strTemplate
     *            the template path
     * @param model
     *            the model
     * @return the HTML
     * @throws IOException
     *             if the template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( String strTemplate, Map<String, Object> model ) throws IOException, TemplateException
    {
        StringTemplateLoader macros = new StringTemplateLoader( );
        macros.putTemplate( MACROS, MACROS_SOURCE );
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_28 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setNumberFormat( "0.######" );
        cfg.setTemplateLoader( new MultiTemplateLoader( new TemplateLoader [ ] {
                macros, new FileTemplateLoader( new File( TEMPLATES_ROOT ) )
        } ) );
        cfg.addAutoInclude( MACROS );
        model.put( "infos", new ArrayList<>( ) );
        StringWriter writer = new StringWriter( );
        cfg.getTemplate( strTemplate ).process( model, writer );

        return writer.toString( );
    }

    /**
     * A form whose title and description hold markup characters.
     *
     * @return the form
     */
    private static Form form( )
    {
        Form form = new Form( );
        form.setId( 1234 );
        form.setTitle( TITLE );
        form.setDescription( "Desc <i>d</i>" );
        form.setIdWorkflow( 3 );

        return form;
    }

    /**
     * The list of forms writes their titles and descriptions as text, and links each form by its id.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testManageWritesTheFormsAsText( ) throws Exception
    {
        List<Form> listForms = new ArrayList<>( );
        listForms.add( form( ) );
        Map<String, Object> model = new HashMap<>( );
        model.put( "form_list", listForms );

        String strHtml = render( TEMPLATE_MANAGE, model );

        assertTrue( strHtml.contains( TITLE_ESCAPED ), strHtml );
        assertTrue( strHtml.contains( "Desc &lt;i&gt;d&lt;/i&gt;" ), strHtml );
        assertTrue( strHtml.contains( "view=modifyFormIndexation&amp;idForm=1234\"" ), strHtml );
        assertTrue( strHtml.contains( "aria-label=\"#i18n{portal.util.labelModify} " + TITLE_ESCAPED + "\"" ), strHtml );
        assertFalse( strHtml.contains( "<b>" ), strHtml );
        assertFalse( strHtml.contains( "&amp;lt;" ), strHtml );
    }

    /**
     * A form without description (the column accepts NULL) is listed with an empty description cell.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testManageListsAFormWithoutDescription( ) throws Exception
    {
        Form form = form( );
        form.setDescription( null );
        List<Form> listForms = new ArrayList<>( );
        listForms.add( form );
        Map<String, Object> model = new HashMap<>( );
        model.put( "form_list", listForms );

        String strHtml = render( TEMPLATE_MANAGE, model );

        assertTrue( strHtml.contains( TITLE_ESCAPED ), strHtml );
        assertTrue( strHtml.contains( "idForm=1234\"" ), strHtml );
    }

    /**
     * The selection page writes the titles of the form, its steps, its questions and the names of the states as text,
     * and checks the boxes of the selection already saved.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testModifyWritesTheLabelsAsTextAndChecksTheSavedSelection( ) throws Exception
    {
        Step step = new Step( );
        step.setId( 5 );
        step.setTitle( "Step <u>1</u>" );
        Question questionSelected = new Question( );
        questionSelected.setId( 1001 );
        questionSelected.setIdStep( 5 );
        questionSelected.setTitle( "Name & <em>surname</em>" );
        Question questionOther = new Question( );
        questionOther.setId( 1002 );
        questionOther.setIdStep( 5 );
        questionOther.setTitle( "Age" );
        Map<Step, List<Question>> mapStepQuestions = new LinkedHashMap<>( );
        mapStepQuestions.put( step, List.of( questionSelected, questionOther ) );
        State state = new State( );
        state.setId( 2001 );
        state.setName( "Done <s>ok</s>" );
        OptionalQuestionIndexation optionalQuestion = new OptionalQuestionIndexation( );
        optionalQuestion.setIdQuestion( 1001 );
        OptionalStatusIndexation optionalStatus = new OptionalStatusIndexation( );
        optionalStatus.setIdStatus( 2001 );

        Map<String, Object> model = new HashMap<>( );
        model.put( "form", form( ) );
        model.put( "form_step_question_list", mapStepQuestions );
        model.put( "form_status_list", List.of( state ) );
        model.put( "optional_question_indexation_list", List.of( optionalQuestion ) );
        model.put( "optional_status_indexation_list", List.of( optionalStatus ) );
        model.put( "token", "tk" );

        String strHtml = render( TEMPLATE_MODIFY, model );

        assertTrue( strHtml.contains( "<h2>" + TITLE_ESCAPED + " - " ), strHtml );
        assertTrue( strHtml.contains( "<legend>Step &lt;u&gt;1&lt;/u&gt;</legend>" ), strHtml );
        assertTrue( strHtml.contains( "<legend>#i18n{module.elasticdata.forms.modify_form.status}</legend>" ), strHtml );
        assertTrue( strHtml.contains( "Name &amp; &lt;em&gt;surname&lt;/em&gt;" ), strHtml );
        assertTrue( strHtml.contains( "Done &lt;s&gt;ok&lt;/s&gt;" ), strHtml );
        assertTrue( strHtml.contains( "name=\"idForm\" value=\"1234\"" ), strHtml );
        assertTrue( strHtml.contains( "name=\"token\" value=\"tk\"" ), strHtml );
        assertTrue( strHtml.contains( "name=\"1001\" value=\"1001\" checked>" ), strHtml );
        assertTrue( strHtml.contains( "name=\"1002\" value=\"1002\">" ), strHtml );
        assertTrue( strHtml.contains( "name=\"status2001\" value=\"2001\" checked>" ), strHtml );
        assertFalse( strHtml.contains( "&amp;lt;" ), strHtml );
    }
}
