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
package fr.paris.lutece.plugins.elasticdata.modules.forms.business;

import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.modules.forms.util.Lambert93;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import net.sf.json.JSONObject;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.state.StateService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * FormsDataSource
 */
public class FormsDataSource extends AbstractDataSource
{

    @Override
    public Collection<DataObject> fetchDataObjects( )
    {
        ArrayList<DataObject> collResult = new ArrayList<>( );
        List<Form> listForms = FormHome.getFormList( );
        for ( Form form : listForms )
        {
            List<FormResponse> listFormResponses = FormResponseHome.selectAllFormResponsesUncompleteByIdForm( form.getId( ) );
            listFormResponses.parallelStream( ).forEach( formResponse -> {
                if ( !formResponse.isFromSave( ) )
                {
                    collResult.add( create( formResponse, form ) );
                }
            } );
        }
        return collResult;
    }

    @Override
    public String getMappings( )
    {
        List<Form> forms = FormHome.getFormList( );
        JSONObject fields = new JSONObject( );

        for ( Form form : forms )
        {

            List<OptionalQuestionIndexation> optionalQuestionIndexations = OptionalQuestionIndexationHome
                    .getOptionalQuestionIndexationListByFormId( form.getId( ) );
            for ( OptionalQuestionIndexation optionalQuestionIndexation : optionalQuestionIndexations )
            {
                Question question = QuestionHome.findByPrimaryKey( optionalQuestionIndexation.getIdQuestion( ) );
                String entryTypeBeanName = question.getEntry( ).getEntryType( ).getBeanName( );
                JSONObject fieldMapping = getFieldMappingFromBeanName( entryTypeBeanName );

                if ( fieldMapping != null )
                {
                    String key = question.getId( ) + "." + StringUtils.abbreviate( question.getTitle( ), 100 );
                    if ( entryTypeBeanName.equals( "forms.entryTypeGeolocation" ) )
                    {
                        key = "userResponses." + key + ".elastic.geopoint";
                    }
                    fields.put( key, fieldMapping );
                }

            }

        }

        fields.put( "timestamp", getFieldMappingFromBeanName( "entryTypeDate" ) );

        JSONObject properties = new JSONObject( );
        properties.put( "properties", fields );

        JSONObject mappings = new JSONObject( );
        mappings.put( "mappings", properties );

        return mappings.toString( );
    }

    /**
     * Create a form response object
     * 
     * @param formResponse
     *            The FormResponse
     * @param form
     *            The Form
     * @return The form response object
     */
    public FormResponseDataObject create( FormResponse formResponse, Form form )
    {
        IResourceHistoryService _resourceHistoryService = SpringContextService.getBean( ResourceHistoryService.BEAN_SERVICE );
        StateService stateService = SpringContextService.getBean( StateService.BEAN_SERVICE );
        State stateFormResponse = null;
        Action action = null;
        int formResponseId = formResponse.getId( );
        int formId = formResponse.getFormId( );
        Timestamp formResponseCreation = formResponse.getCreation( );
        FormResponseDataObject formResponseDataObject = new FormResponseDataObject( );

        formResponseDataObject.setId( String.valueOf( formResponseId ) );
        formResponseDataObject.setFormName( form.getTitle( ) );
        formResponseDataObject.setFormId( form.getId( ) );
        formResponseDataObject.setTimestamp( formResponseCreation.getTime( ) );

        ResourceHistory resourceHist = _resourceHistoryService.getLastHistoryResource( formResponseId, FormResponse.RESOURCE_TYPE, form.getIdWorkflow( ) );

        if(resourceHist != null)
        {
            action= (resourceHist.getAction( ).isAutomaticReflexiveAction( ) )? resourceHist.getAction( ): null;
            stateFormResponse = stateService.findByPrimaryKey( resourceHist.getAction( ).getStateAfter( ).getId( ) );
            long duration = duration( formResponseCreation, resourceHist.getCreationDate( ) );
            formResponseDataObject.setTaskDuration( duration );  
        }
        else
        {
            stateFormResponse = stateService.findByResource( formResponseId, FormResponse.RESOURCE_TYPE, form.getIdWorkflow( ) );
        }

        if( stateFormResponse != null ) {
            formResponseDataObject.setWorkflowState( stateFormResponse.getName( ) );
        }

        if( action != null ) {
            formResponseDataObject.setActionName( action.getName( ) );
        }

        getFormQuestionResponseListToIndex( formResponseId, formId, formResponseDataObject );
        return formResponseDataObject;
    }

    /**
     * Index Form Response data object to Elasticdata
     * 
     * @param nIdResource
     *            The FormResponse id
     * @param nIdTask
     *            The Form
     */
    public void indexDocument( int nIdResource, int nIdTask )
    {
        FormResponse formResponse = FormResponseHome.findByPrimaryKey( nIdResource );
        Form form = FormHome.findByPrimaryKey( formResponse.getFormId( ) );
        try
        {
            // Force init data sources of ElasticData plugin
            DataSourceService.getDataSources( );
            DataSource source = DataSourceService.getDataSource( "FormsDataSource" );
            FormResponseDataObject formResponseDataObject = create( formResponse, form );
            DataSourceService.processIncrementalIndexing( source, formResponseDataObject );
        }
        catch( ElasticClientException e )
        {
            AppLogService.error( "Unable to process incremental indexing of idRessource :" + nIdResource, e );
        }
        catch( NullPointerException e )
        {
            AppLogService.error( "Unable to get DataSource :" + nIdResource, e );
        }
    }

    /**
     * Find form question response list
     * 
     * @param nIdFormResponse
     *            The FormResponse id
     * @param nIdForm
     *            The Form id
     * @return user
     */
    private void getFormQuestionResponseListToIndex( int nIdFormResponse, int nIdForm, FormResponseDataObject formResponseDataObject )
    {
        List<OptionalQuestionIndexation> optionalQuestionIndexations = OptionalQuestionIndexationHome.getOptionalQuestionIndexationListByFormId( nIdForm );
        Map<String, String> userResponses = new HashMap<>( );
        Map<String, String [ ]> userResponsesMultiValued = new HashMap<>( );

        if ( optionalQuestionIndexations != null )
        {
            for ( OptionalQuestionIndexation optionalQuestionIndexation : optionalQuestionIndexations )
            {
                int idQuestion = optionalQuestionIndexation.getIdQuestion( );
                Question question = QuestionHome.findByPrimaryKey( idQuestion );
                List<FormQuestionResponse> formQuestionResponseList = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion( nIdFormResponse,
                        idQuestion );
                for ( FormQuestionResponse formQuestionResponse : formQuestionResponseList )
                {
                    String [ ] responses = { };
                    String questionTitle = StringUtils.abbreviate( question.getTitle( ), 100 );
                    List<Response> responseList = formQuestionResponse.getEntryResponse( );

                    for ( Response response : responseList )
                    {

                        if ( response.getField( ) != null )
                        {
                            if ( question.getEntry( ).getEntryType( ).getBeanName( ).equals( "forms.entryTypeCheckBox" ) )
                            {
                                responses = ArrayUtils.add( responses, response.getResponseValue( ) );
                            }
                            else
                            {
                                userResponses.put( question.getId( ) + "." + questionTitle + "." + response.getField( ).getCode( ), response.getResponseValue( ) );
                            }
                        }

                        if ( response.getField( ) == null )
                        {
                            userResponses.put( question.getId( ) + "." + questionTitle, response.getResponseValue( ) );
                        }

                    }

                    if ( responses.length > 0 )
                    {
                        userResponsesMultiValued.put( question.getId( ) + "." + questionTitle, responses );
                    }

                    if ( question.getEntry( ).getEntryType( ).getBeanName( ).equals( "forms.entryTypeGeolocation" ) )
                    {
                        Response x = responseList.stream( ).filter( response -> "X".equals( response.getField( ).getValue( ) ) ).findAny( ).orElse( null );
                        Response y = responseList.stream( ).filter( response -> "Y".equals( response.getField( ).getValue( ) ) ).findAny( ).orElse( null );

                        if ( x != null && y != null && NumberUtils.isCreatable( x.getResponseValue( ) ) && NumberUtils.isCreatable( y.getResponseValue( ) ) )
                        {
                            String geopoint = Lambert93.toLatLon( Double.parseDouble( x.getResponseValue( ) ), Double.parseDouble( y.getResponseValue( ) ) );
                            userResponses.put( question.getId( ) + "." + question.getTitle( ) + ".elastic.geopoint", geopoint );
                        }
                    }
                }
            }
        }

        formResponseDataObject.setUserResponses( userResponses );
        formResponseDataObject.setUserResponsesMultiValued( userResponsesMultiValued );
    }

    /**
     * return elasticsearch field mapping
     * 
     * @param strEntryTypeName
     *            Entry type name
     * @return elasticsearch field mapping
     */
    private static JSONObject getFieldMappingFromBeanName( String entryTypeBeanName )
    {

        JSONObject entryType = new JSONObject( );

        if ( entryTypeBeanName.contains( "entryTypeDate" ) )
        {
            entryType.put( "type", "date" );
            entryType.put( "format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis" );
            return entryType;
        }

        if ( entryTypeBeanName.contains( "entryTypeNumbering" ) )
        {
            entryType.put( "type", "long" );
            return entryType;
        }

        if ( entryTypeBeanName.equals( "forms.entryTypeGeolocation" ) )
        {
            entryType.put( "type", "geo_point" );
            return entryType;
        }

        return null;
    }

    /**
     * return The duration in days
     * 
     * @param currentTime
     *            The younger time.
     * @param oldTime
     *            The older time.
     * @return The duration in days
     */
    private static long duration( java.sql.Timestamp start, java.sql.Timestamp end )
    {
        long milliseconds1 = start.getTime( );
        long milliseconds2 = end.getTime( );
        long diff = milliseconds2 - milliseconds1;
        long diffDays = diff / ( 24 * 60 * 60 * 1000 );
        return diffDays;
    }

    public static <T> Predicate<T> distinctByKey( Function<? super T, Object> keyExtractor )
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>( );
        return t -> map.putIfAbsent( keyExtractor.apply( t ), Boolean.TRUE ) == null;
    }
}
