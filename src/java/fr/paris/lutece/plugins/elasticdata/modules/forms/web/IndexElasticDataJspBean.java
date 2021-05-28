/*
 * Copyright (c) 2002-2020, City of Paris
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalQuestionIndexation;
import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalQuestionIndexationHome;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.forms.business.StepHome;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

/**
 * ManageElasticData JSP Bean abstract class for JSP Bean
 */
@Controller( controllerJsp = "IndexingAppElasticData.jsp", controllerPath = "jsp/admin/plugins/elasticdata/modules/forms", right = "ELASTICDATA_FORMS_MANAGEMENT" )
public class IndexElasticDataJspBean extends MVCAdminJspBean
{
    private static final String TEMPLATE_MANAGE_FORMS_INDEXATION = "/admin/plugins/elasticdata/modules/forms/manage_forms_indexation.html";
    private static final String TEMPLATE_MODIFY_INDEXATION = "/admin/plugins/elasticdata/modules/forms/modify_form_indexation.html";

    private static final String VIEW_MANAGE_FORMS_INDEXATION = "manageFormsIndexation";
    private static final String VIEW_MODIFY_FORM_INDEXATION = "modifyFormIndexation";

    private static final String ACTION_INDEX = "index";
    private static final String ACTION_MODIFY_FORM_INDEXATION = "modifyFormIndexation";

    private static final String MARK_FORM_LIST = "form_list";
    private static final String MARK_FORM = "form";
    private static final String MARK_FORM_STEP_QUESTION_LIST = "form_step_question_list";
    private static final String MARK_OPTIONAL_QUESTION_INDEXATION_LIST = "optional_question_indexation_list";

    private static final String PROPERTY_PAGE_TITLE = "module.description";
    protected static final String MESSAGE_SUCCESS_SAVE = "module.elasticdata.forms.modify.save.success";

    private static final String PARAMETER_DATA_SOURCE = "data_source";
    private static final String PARAMETER_FORM_ID = "idForm";

    private static final long serialVersionUID = 1L;

    /**
     * View the home of the feature
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_FORMS_INDEXATION, defaultView = true )
    public String getManageFromsIndexation( HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );
        List<Form> listForms = FormHome.getFormList( );
        model.put( MARK_FORM_LIST, listForms );
        return getPage( PROPERTY_PAGE_TITLE, TEMPLATE_MANAGE_FORMS_INDEXATION, model );
    }

    /**
     * View the home of the feature
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MODIFY_FORM_INDEXATION )
    public String getModifyFormIndexation( HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );
        String strIdForm = request.getParameter( PARAMETER_FORM_ID );
        int nIdFrom = Integer.valueOf( strIdForm );
        LinkedHashMap<Step, List<Question>> stepWithQuestionList = new LinkedHashMap<>( );
        Form form = FormHome.findByPrimaryKey( nIdFrom );
        List<Question> questionList = QuestionHome.getListQuestionByIdForm( nIdFrom );
        List<Step> stepList = StepHome.getStepsListByForm( nIdFrom );

        for ( Question question : questionList )
        {
            Entry entry = EntryHome.findByPrimaryKey( question.getEntry( ).getIdEntry( ) );
            question.setEntry( entry );
        }

        for ( Step step : stepList )
        {
            List<Question> stepQuestionList = questionList.stream( ).filter( q -> q.getIdStep( ) == step.getId( ) ).collect( Collectors.toList( ) );
            stepWithQuestionList.put( step, stepQuestionList );
        }

        model.put( MARK_OPTIONAL_QUESTION_INDEXATION_LIST, OptionalQuestionIndexationHome.getOptionalQuestionIndexationListByFormId( nIdFrom ) );
        model.put( MARK_FORM, form );
        model.put( MARK_FORM_STEP_QUESTION_LIST, stepWithQuestionList );
        return getPage( PROPERTY_PAGE_TITLE, TEMPLATE_MODIFY_INDEXATION, model );
    }

    /**
     * Process the full indexing of a given data source
     * 
     * @param request
     *            The HTTP request
     * @return The redirected page
     */
    @Action( ACTION_INDEX )
    public String doIndex( HttpServletRequest request ) throws ElasticClientException
    {
        StringBuilder sbLogs = new StringBuilder( );
        String strDataSourceId = request.getParameter( PARAMETER_DATA_SOURCE );
        DataSource source = DataSourceService.getDataSource( strDataSourceId );
        DataSourceService.processFullIndexing( source, false );
        addInfo( sbLogs.toString( ) );
        return redirectView( request, VIEW_MANAGE_FORMS_INDEXATION );
    }

    /**
     * Process the full indexing of a given data source
     * 
     * @param request
     *            The HTTP request
     * @return The redirected page
     */
    @Action( ACTION_MODIFY_FORM_INDEXATION )
    public String doModifyFormIndexation( HttpServletRequest request ) throws ElasticClientException
    {
        String strIdForm = request.getParameter( PARAMETER_FORM_ID );
        int nIdFrom = Integer.valueOf( strIdForm );
        List<Question> questionList = QuestionHome.getListQuestionByIdForm( nIdFrom );
        for ( Question question : questionList )
        {
            int nIdQuestion = question.getId( );
            String checkBoxValue = request.getParameter( String.valueOf( question.getId( ) ) );
            OptionalQuestionIndexation optionalQuestionIndexation = OptionalQuestionIndexationHome.findByQuestionId( nIdQuestion );
            if ( checkBoxValue != null )
            {
                if ( optionalQuestionIndexation == null )
                {
                    OptionalQuestionIndexation newOptionalQuestionIndexation = new OptionalQuestionIndexation( );
                    newOptionalQuestionIndexation.setIdQuestion( nIdQuestion );
                    newOptionalQuestionIndexation.setIdForm( question.getStep( ).getIdForm( ) );
                    OptionalQuestionIndexationHome.create( newOptionalQuestionIndexation );
                }
            }
            else
            {
                if ( optionalQuestionIndexation != null )
                {
                    OptionalQuestionIndexationHome.remove( optionalQuestionIndexation.getId( ) );
                }
            }
        }
        addInfo( I18nService.getLocalizedString( MESSAGE_SUCCESS_SAVE, getLocale( ) ) );
        return redirect( request, VIEW_MODIFY_FORM_INDEXATION, PARAMETER_FORM_ID, nIdFrom );
    }
}
