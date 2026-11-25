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
package fr.paris.lutece.plugins.elasticdata.modules.forms.business;


import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;

/**
 * A draft (« save for later » of the front office, from_save) is not a submitted response: the full indexing does not send it to Elasticsearch
 */
public class FormsDataSourceDraftTest extends LuteceTestCase
{
    @Inject
    private FormsDataSource _dataSource;

    private Form _form;
    private FormResponse _draft;
    private FormResponse _submitted;

    @BeforeEach
    protected void createResponses( )
    {
        _form = new Form( );
        _form.setTitle( "FormsDataSourceDraftTest" );
        _form.setBreadcrumbName( "forms.horizontalBreadcrumb" );
        FormHome.create( _form );
        _draft = createResponse( true );
        _submitted = createResponse( false );
    }

    @AfterEach
    protected void removeResponses( )
    {
        FormResponseHome.remove( _draft.getId( ) );
        FormResponseHome.remove( _submitted.getId( ) );
        FormHome.remove( _form.getId( ) );
    }

    @Test
    public void testFullIndexingSkipsDrafts( )
    {
        List<String> listIds = _dataSource.getIdDataObjects( );

        assertTrue( listIds.contains( String.valueOf( _submitted.getId( ) ) ), listIds.toString( ) );
        assertTrue( !listIds.contains( String.valueOf( _draft.getId( ) ) ), listIds.toString( ) );
    }

    private FormResponse createResponse( boolean bDraft )
    {
        FormResponse response = new FormResponse( );
        response.setFormId( _form.getId( ) );
        response.setGuid( "FormsDataSourceDraftTest" );
        response.setFromSave( bDraft );
        FormResponseHome.create( response );

        return response;
    }
}
