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
import fr.paris.lutece.plugins.elasticdata.service.DataSourceIncrementalService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeCheckBox;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeGeolocation;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeSelectOrder;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.Field;
import fr.paris.lutece.plugins.genericattributes.business.FieldHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.plugins.genericattributes.util.GenericAttributesUtils;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.action.ActionFilter;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistoryFilter;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * FormsDataSource
 */
@ApplicationScoped
public class FormsDataSource extends AbstractDataSource
{
    @Inject
    private IActionService _actionService;
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    private IStateService _stateService;

    private static final String DATA_SOURCE_NAME = "FormsDataSource";
    private static final String DOCUMENT_TYPE_NAME_FORM_RESPONSE = "formResponse";
    private static final String DOCUMENT_TYPE_NAME_FORM_RESPONSE_HISTORY = "formResponseHistory";
    private static final String RESSOURCE_TYPE = "FORMS_FORM_RESPONSE";
    private static final int SQL_MAX_SELECT_IN = 80;
    Map<Integer, String> _mapFields = new HashMap<>( );
    
    public FormsDataSource( ){}

    @Inject
    public FormsDataSource( @ConfigProperty( name = "elasticdata-forms.formsDataSource.id" ) String strDataSourceId,
    		@ConfigProperty( name = "elasticdata-forms.formsDataSource.name" ) String strDataSourceName,
    		@ConfigProperty( name = "elasticdata-forms.formsDataSource.targetIndexName" ) String strDataSourceTargetIndexName,
    		@ConfigProperty( name = "elasticdata-forms.formsDataSource.mappings" ) String strDataSourceMappings )
    {
        setId( strDataSourceId );
        setName( strDataSourceName );
        setTargetIndexName( strDataSourceTargetIndexName );
        setMappings( strDataSourceMappings );
    }

    @Override
    public List<String> getIdDataObjects( )
    {
        List<Form> listForms = FormHome.getFormList( );
        List<Integer> listFormResponseId = new ArrayList<>( );
        listForms.parallelStream( ).forEach( form -> {
            List<FormResponse> listFormResponses = FormResponseHome.selectAllFormResponsesUncompleteByIdForm( form.getId( ) );
            listFormResponseId.addAll( listFormResponses.parallelStream( ).map( i -> i.getId( ) ).distinct( ).collect( Collectors.toList( ) ) );
        } );
        return listFormResponseId.stream( ).map( Object::toString ).collect( Collectors.toList( ) );
    }

    @Override
    public synchronized List<DataObject> getDataObjects( List<String> listIdDataObjects )
    {
        List<DataObject> collResult = new Vector<>( );
        _mapFields = new HashMap<>( );
        AtomicInteger counter = new AtomicInteger( );
        // split for db performance
        Map<Integer, List<String>> listIdDataObjectSplited = listIdDataObjects.stream( )
                .collect( Collectors.groupingBy( it -> counter.getAndIncrement( ) / SQL_MAX_SELECT_IN ) );

        listIdDataObjectSplited.entrySet( ).parallelStream( ).forEach( e -> {

            List<Integer> listIdFormResponse = e.getValue( ).stream( ).map( x -> Integer.valueOf( x ) ).collect( Collectors.toList( ) );
            List<FormResponse> formResponseList = FormResponseHome.getFormResponseUncompleteByPrimaryKeyList( listIdFormResponse );
            List<FormQuestionResponse> listFormQuestionResponse = FormQuestionResponseHome.getFormQuestionResponseListByFormResponseList( listIdFormResponse );
            List<List<FormResponse>> listFormFormResponse = formResponseList.stream( )
                    .collect( Collectors.groupingBy( FormResponse::getFormId, Collectors.toList( ) ) ).values( ).stream( ).collect( Collectors.toList( ) );

            for ( List<FormResponse> listformResponse : listFormFormResponse )
            {
                Form form = FormHome.findByPrimaryKey( listformResponse.get( 0 ).getFormId( ) );
                List<ResourceHistory> listResourceHistory = getResourceHistoryList( listIdFormResponse, form.getIdWorkflow( ) );

                collResult.addAll( getDataObjects( listformResponse, listFormQuestionResponse, listResourceHistory, form ) );
            }
        } );
        return collResult;
    }

    /**
     * Get a list of documents to index
     * 
     * @param listformResponse
     *            The list of form response
     * @param listFormQuestionResponse
     *            The list of form question response
     * @param listResourceHistory
     *            The list of ressource history
     * @param form
     *            The form
     * @return a list of form response object
     */
    public List<FormResponseDataObject> getDataObjects( List<FormResponse> listformResponse, List<FormQuestionResponse> listFormQuestionResponse,
            List<ResourceHistory> listResourceHistory, Form form )
    {
        List<FormResponseDataObject> formResponseDataObjectList = new ArrayList<>( );

        int nIdForm = form.getId( );
        int nIdWorkflow = form.getIdWorkflow( );

        List<State> listStates = getStateList( nIdWorkflow );
        List<Action> listActions = getActionList( nIdWorkflow );

        List<Integer> optionalQuestionIndexations = OptionalQuestionIndexationHome.getOptionalQuestionIndexationListByFormId( nIdForm ).stream( )
                .map( OptionalQuestionIndexation::getIdQuestion ).collect( Collectors.toList( ) );
        List<Question> listQuestions = QuestionHome.findByPrimaryKeyList( optionalQuestionIndexations );

        for ( FormResponse formResponse : listformResponse )
        {
            int formResponseId = formResponse.getId( );
            Timestamp formResponseCreation = formResponse.getCreation( );

            List<ResourceHistory> listResourceHistoryFiltred = listResourceHistory.stream( )
                    .filter( resourceHistory -> resourceHistory.getIdResource( ) == formResponseId ).collect( Collectors.toList( ) );
            ResourceHistory lastRessourceHistory = listResourceHistoryFiltred.stream( )
                    .filter( resourceHistory -> resourceHistory.getIdResource( ) == formResponseId ).reduce( ( first, second ) -> second ).orElse( null );

            List<FormQuestionResponse> listFormResponseQuestionResponse = listFormQuestionResponse.parallelStream( )
                    .filter( x -> optionalQuestionIndexations.contains( x.getQuestion( ).getId( ) ) && x.getIdFormResponse( ) == formResponse.getId( ) )
                    .collect( Collectors.toList( ) );

            FormResponseDataObject formResponseDataObject = new FormResponseDataObject( );
            formResponseDataObject.setId( String.valueOf( formResponseId ) );
            formResponseDataObject.setFormResponseId( formResponseId );
            formResponseDataObject.setFormName( form.getTitle( ) );
            formResponseDataObject.setFormId( form.getId( ) );
            formResponseDataObject.setTimestamp( formResponseCreation.getTime( ) );
            formResponseDataObject.setParentId( String.valueOf( form.getId( ) ) );
            formResponseDataObject.setParentName( form.getTitle( ) );
            formResponseDataObject.setDocumentTypeName( DOCUMENT_TYPE_NAME_FORM_RESPONSE );
            setLastResourceHistory( formResponseDataObject, listStates, listActions, lastRessourceHistory, formResponseCreation );
            setUserResponses( formResponseDataObject, listFormResponseQuestionResponse, listQuestions );

            formResponseDataObjectList.addAll(
                    getFormResponseHistory( formResponseDataObject, listResourceHistoryFiltred, formResponse.getCreation( ), listActions, listStates ) );
            formResponseDataObjectList.add( formResponseDataObject );
        }

        return formResponseDataObjectList;
    }

    /**
     * populate the form response data object with the last ressource history
     * 
     * @param formResponseDataObject
     *            the form response data object
     * @param listStates
     *            the list of states
     * @param listActions
     *            the list of actions
     * @param lastRessourceHistory
     *            the last ressource history
     */
    private void setLastResourceHistory( FormResponseDataObject formResponseDataObject, List<State> listStates, List<Action> listActions,
            ResourceHistory lastRessourceHistory, Timestamp formResponseCreation )
    {
        if ( lastRessourceHistory != null )
        {
            Action lastCompletedAction = listActions.stream( ).filter( action2 -> action2.getId( ) == lastRessourceHistory.getAction( ).getId( ) ).findFirst( )
                    .orElse( null );
            long lcompleteDuration = duration( formResponseCreation, lastRessourceHistory.getCreationDate( ) );
            formResponseDataObject.setCompleteDuration( lcompleteDuration );
            if ( lastCompletedAction != null )
            {
                State stateFormResponse = listStates.stream( ).filter( state2 -> state2.getId( ) == lastCompletedAction.getStateAfter( ).getId( ) ).findFirst( )
                        .orElse( null );
                if ( stateFormResponse != null )
                {
                    formResponseDataObject.setWorkflowState( stateFormResponse.getName( ) );
                    formResponseDataObject.setActionName( lastCompletedAction.getName( ) );
                }
            }
        }
    }

    /**
     * return a list of ressource history data object according to a list of form responses
     * 
     * @param formResponseDataObject
     *            the form response data object
     * @param listResourceHistoryFiltred
     *            the list of ressource history
     * @param formResponseDateCreation
     *            the form response date creation
     * @param listActions
     *            the list of actions
     * @param listStates
     *            the list of states
     * @return list of ressource history data object
     */
    private List<FormResponseDataObject> getFormResponseHistory( FormResponseDataObject formResponseDataObject,
            List<ResourceHistory> listResourceHistoryFiltred, Timestamp formResponseDateCreation, List<Action> listActions, List<State> listStates )
    {
        Timestamp lstartingDateDuration = formResponseDateCreation;
        List<FormResponseDataObject> formResponseDataObjectList = new ArrayList<>( );
        for ( ResourceHistory resourceHistory : listResourceHistoryFiltred )
        {
            long lTaskDuration = duration( lstartingDateDuration, resourceHistory.getCreationDate( ) );
            long lCompleteDuration = duration( formResponseDateCreation, resourceHistory.getCreationDate( ) );
            FormResponseDataObject FormResponseHistoryDataObject = new FormResponseDataObject( );
            FormResponseHistoryDataObject.setId( DOCUMENT_TYPE_NAME_FORM_RESPONSE_HISTORY + "_" + resourceHistory.getId( ) );
            FormResponseHistoryDataObject.setFormName( formResponseDataObject.getFormName( ) );
            FormResponseHistoryDataObject.setFormId( formResponseDataObject.getFormId( ) );
            FormResponseHistoryDataObject.setFormResponseId( formResponseDataObject.getFormResponseId( ) );
            FormResponseHistoryDataObject.setTimestamp( resourceHistory.getCreationDate( ).getTime( ) );
            FormResponseHistoryDataObject.setTaskDuration( lTaskDuration );
            FormResponseHistoryDataObject.setActionName( resourceHistory.getAction( ).getName( ) );
            FormResponseHistoryDataObject.setParentName( resourceHistory.getWorkflow( ).getName( ) );
            FormResponseHistoryDataObject.setParentId( String.valueOf( resourceHistory.getWorkflow( ).getId( ) ) );
            FormResponseHistoryDataObject.setDocumentTypeName( DOCUMENT_TYPE_NAME_FORM_RESPONSE_HISTORY );
            FormResponseHistoryDataObject.setCompleteDuration( lCompleteDuration );
            FormResponseHistoryDataObject.setUserResponses( formResponseDataObject.getUserResponses( ) );
            FormResponseHistoryDataObject.setWorflowAdminCreator( resourceHistory.getUserAccessCode( ) );
            Action action = listActions.stream( ).filter( action2 -> action2.getId( ) == resourceHistory.getAction( ).getId( ) ).findFirst( ).orElse( null );
            if ( action != null )
            {
                State stateFormResponse = listStates.stream( ).filter( state2 -> state2.getId( ) == action.getStateAfter( ).getId( ) ).findFirst( )
                        .orElse( null );
                if ( stateFormResponse != null )
                {
                    FormResponseHistoryDataObject.setWorkflowState( stateFormResponse.getName( ) );
                }
            }
            lstartingDateDuration = resourceHistory.getCreationDate( );
            formResponseDataObjectList.add( FormResponseHistoryDataObject );
        }

        return formResponseDataObjectList;
    }

    /**
     * return a list of ressource history according to a list of form response id
     * 
     * @param listIdDataObjects
     *            the list of form response id
     * @param nIdWorkflow
     *            the form workflow id
     * @return list of ressource history
     */
    private List<ResourceHistory> getResourceHistoryList( List<Integer> listIdDataObjects, int nIdWorkflow )
    {
        ResourceHistoryFilter filter = new ResourceHistoryFilter( );
        filter.setListIdResources( listIdDataObjects );
        filter.setResourceType( RESSOURCE_TYPE );
        filter.setIdWorkflow( nIdWorkflow );
        return _resourceHistoryService.getAllHistoryByFilter( filter ).stream( ).sorted( Comparator.comparing( ResourceHistory::getCreationDate ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * return a list of states according to workflow id
     * 
     * @param nIdWorkflow
     *            the workflow id
     * @return list of states
     */
    private List<State> getStateList( int nIdWorkflow )
    {
        StateFilter stateFilter = new StateFilter( );
        stateFilter.setIdWorkflow( nIdWorkflow );
        return _stateService.getListStateByFilter( stateFilter );
    }

    /**
     * return a list of actions according to workflow id
     * 
     * @param nIdWorkflow
     *            the workflow id
     * @return list of actions
     */
    private List<Action> getActionList( int nIdWorkflow )
    {
        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( nIdWorkflow );
        actionFilter.setAutomaticReflexiveAction( false );
        return _actionService.getListActionByFilter( actionFilter );
    }

    /**
     * Sets the user responses based on the form response data, form question responses, and a list of questions.
     *
     * @param formResponseDataObject
     *            The form response data object.
     * @param formQuestionResponseList
     *            The list of form question responses.
     * @param listQuestion
     *            The list of questions.
     */
    private void setUserResponses( FormResponseDataObject formResponseDataObject, List<FormQuestionResponse> formQuestionResponseList,
            List<Question> listQuestion )
    {
        Map<String, Object> userResponses = new HashMap<>( );

        formQuestionResponseList.forEach( formQuestionResponse -> listQuestion.stream( )
                .filter( question -> question.getId( ) == formQuestionResponse.getQuestion( ).getId( ) ).findFirst( ).ifPresent( question -> {
                    String baseKey = question.getId( ) + "." + StringUtils.abbreviate( question.getTitle( ), 100 );

                    if ( !formQuestionResponse.getEntryResponse( ).isEmpty( ) )
                    {
                        IEntryTypeService entryTypeService = EntryTypeServiceManager
                                .getEntryTypeService( formQuestionResponse.getEntryResponse( ).get( 0 ).getEntry( ) );

                        if ( entryTypeService instanceof EntryTypeCheckBox )
                        {
                            List<String> responses = formQuestionResponse.getEntryResponse( ).stream( ).map( Response::getResponseValue )
                                    .collect( Collectors.toList( ) );
                            userResponses.put( baseKey, responses );
                        }
                        else
                            if ( entryTypeService instanceof EntryTypeSelectOrder )
                            {
                                List<String> responses = formQuestionResponse.getEntryResponse( ).stream( )
                                        .sorted( Comparator.comparing( Response::getSortOrder ) ).map( Response::getResponseValue )
                                        .collect( Collectors.toList( ) );
                                userResponses.put( baseKey, responses );
                            }
                            else
                                if ( entryTypeService instanceof EntryTypeGeolocation )
                                {
                                    Map<String, String> responses = formQuestionResponse.getEntryResponse( ).stream( ).collect( HashMap::new,
                                            ( map, response ) -> {
                                                Integer idField = response.getField( ).getIdField( );
                                                String fieldCode = getFieldCode( idField );

                                                if ( fieldCode != null )
                                                {
                                                    map.put( fieldCode, response.getResponseValue( ) );
                                                }
                                                else
                                                {
                                                    map.put( String.valueOf( idField ), response.getResponseValue( ) );
                                                }
                                            }, HashMap::putAll );
                                    userResponses.put( baseKey, responses );
                                }
                                else
                                {
                                    formQuestionResponse.getEntryResponse( ).forEach( response -> userResponses.put( baseKey, response.getResponseValue( ) ) );
                                }
                    }
                } ) );

        formResponseDataObject.setUserResponses( userResponses );
    }

    /**
     * Index Form Response data object to Elasticdata
     * 
     * @param nIdResource
     *            The FormResponse id
     * @param nIdTask
     *            The task id
     */
    public void indexDocument( int nIdResource, int nIdTask )
    {
        DataSourceIncrementalService.addTask( DATA_SOURCE_NAME, String.valueOf( nIdResource ), nIdTask );
    }

    /**
     * return The duration in milliseconds
     * 
     * @param start
     *            The start time.
     * @param end
     *            The end time.
     * @return The duration in days
     */
    private static long duration( java.sql.Timestamp start, java.sql.Timestamp end )
    {
        long milliseconds1 = start.getTime( );
        long milliseconds2 = end.getTime( );
        return milliseconds2 - milliseconds1;
    }

    /**
     * Retrieves the field code for the specified field ID.
     *
     * @param nIdField
     *            The ID of the field.
     * @return The field code associated with the specified ID, or null if not found.
     */
    public String getFieldCode( int nIdField )
    {
        if ( _mapFields.isEmpty( ) )
        {

            List<Form> listForms = FormHome.getFormList( );

            Map<Integer, String> mapIdEntry = new HashMap<>( );
            for ( Form form : listForms )
            {
                mapIdEntry.putAll( EntryHome.findEntryByForm( GenericAttributesUtils.getPlugin( ), form.getId( ) ) );
            }
            List<Integer> listIdEntry = mapIdEntry.keySet( ).stream( ).collect( Collectors.toList( ) );
            _mapFields = FieldHome.getFieldListByListIdEntry( listIdEntry ).stream( ).collect( Collectors.toMap( Field::getIdField, Field::getCode ) );
        }

        return _mapFields.get( nIdField );
    }

}
