/*
 * Copyright (c) 2002-2019, Mairie de Paris
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
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceHistoryService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            }
            );
        }
        return collResult;
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
        String stateFormResponse = null;
        FormResponseDataObject formResponseDataObject = new FormResponseDataObject( );
        formResponseDataObject.setId( String.valueOf( formResponse.getId( ) ) );
        formResponseDataObject.setFormName( form.getTitle( ) );
        formResponseDataObject.setFormId( form.getId( ) );
        formResponseDataObject.setTimestamp( formResponse.getCreation( ).getTime( ) );
        ResourceHistory resourceHist = _resourceHistoryService
                .getLastHistoryResource( formResponse.getId( ), FormResponse.RESOURCE_TYPE, form.getIdWorkflow( ) );
        if ( resourceHist != null )
        {
            stateFormResponse = resourceHist.getAction( ).getName( );
            long duration = compareTwoTimeStamps( formResponse.getCreation( ), resourceHist.getCreationDate( ) );
            formResponseDataObject.setTaskDuration( duration );
        }
        formResponseDataObject.setWorkflowState( stateFormResponse );
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
            DataSourceService.getDataSources();

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
     * return The duration in days
     * 
     * @param start
     *            The younger time.
     * @param end
     *            The older time.
     * @return The duration in days
     */
    public static long compareTwoTimeStamps( java.sql.Timestamp start, java.sql.Timestamp end )
    {
        long milliseconds1 = start.getTime( );
        long milliseconds2 = end.getTime( );
        long diff = milliseconds2 - milliseconds1;
        long diffDays = diff / ( 24 * 60 * 60 * 1000 );
        return diffDays;
    }
}
