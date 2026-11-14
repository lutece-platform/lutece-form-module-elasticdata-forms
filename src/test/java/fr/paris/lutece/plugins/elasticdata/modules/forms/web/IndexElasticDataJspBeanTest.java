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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalStatusIndexation;
import fr.paris.lutece.plugins.elasticdata.modules.forms.business.OptionalStatusIndexationHome;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.workflow.Workflow;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.workflow.IWorkflowService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.test.AdminUserUtils;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.inject.Inject;

/**
 * Settings of the indexation of a form in the back office: the save action requires its security token and a missing
 * or unknown form opens the list of the forms.
 */
public class IndexElasticDataJspBeanTest extends LuteceTestCase
{
    private static final String RIGHT = "ELASTICDATA_FORMS_MANAGEMENT";
    private static final String PARAMETER_ID_FORM = "idForm";
    private static final String ACTION_MODIFY = "modifyFormIndexation";
    private static final String VIEW_MANAGE = "view=manageFormsIndexation";
    private static final Pattern TOKEN_FIELD = Pattern.compile( "name=\"token\"[^>]*value=\"([^\"]+)\"" );

    @Inject
    private IndexElasticDataJspBean _bean;
    @Inject
    private IWorkflowService _workflowService;
    @Inject
    private IStateService _stateService;

    private Workflow _workflow;
    private State _state;
    private Form _formA;
    private Form _formB;

    @BeforeEach
    protected void createForms( )
    {
        _workflow = new Workflow( );
        _workflow.setName( "IndexElasticDataJspBeanTest" );
        _workflow.setDescription( "" );
        _workflow.setCreationDate( new Timestamp( System.currentTimeMillis( ) ) );
        _workflow.setEnabled( true );
        _workflow.setWorkgroup( "all" );
        _workflowService.create( _workflow );

        _state = new State( );
        _state.setName( "Done" );
        _state.setDescription( "" );
        _state.setWorkflow( _workflow );
        _state.setInitialState( true );
        _state.setRequiredWorkgroupAssigned( false );
        _state.setOrder( 1 );
        _stateService.create( _state );

        _formA = createForm( "A" );
        _formB = createForm( "B" );
    }

    @AfterEach
    protected void removeForms( )
    {
        for ( Form form : List.of( _formA, _formB ) )
        {
            OptionalStatusIndexationHome.getOptionalStatusIndexationListByFormId( form.getId( ) )
                    .forEach( optionalStatus -> OptionalStatusIndexationHome.remove( optionalStatus.getId( ) ) );
            FormHome.remove( form.getId( ) );
        }
        _stateService.remove( _state.getId( ) );
        _workflowService.remove( _workflow.getId( ) );
    }

    /**
     * The settings page carries the token of the save action, which accepts it
     *
     * @throws AccessDeniedException
     *             if the save is refused
     */
    @Test
    public void testSaveAcceptsTheTokenOfThePage( ) throws AccessDeniedException
    {
        MockHttpServletRequest request = newRequest( );
        request.addParameter( PARAMETER_ID_FORM, String.valueOf( _formA.getId( ) ) );
        String strHtml = _bean.getModifyFormIndexation( request );

        Matcher matcher = TOKEN_FIELD.matcher( strHtml );
        assertTrue( matcher.find( ), "The settings form must carry a security token" );
        request.setMethod( "POST" );
        request.addParameter( "action", ACTION_MODIFY );
        request.addParameter( "token", matcher.group( 1 ) );
        request.addParameter( "status" + _state.getId( ), String.valueOf( _state.getId( ) ) );
        _bean.processController( request, new MockHttpServletResponse( ) );

        assertEquals( List.of( _state.getId( ) ), savedStates( _formA ) );
    }

    /**
     * Without the security token, the save is refused and nothing is saved
     */
    @Test
    public void testSaveRefusedWithoutToken( )
    {
        MockHttpServletRequest request = newRequest( );
        request.setMethod( "POST" );
        request.addParameter( "action", ACTION_MODIFY );
        request.addParameter( PARAMETER_ID_FORM, String.valueOf( _formA.getId( ) ) );
        request.addParameter( "status" + _state.getId( ), String.valueOf( _state.getId( ) ) );

        assertThrows( AccessDeniedException.class, ( ) -> _bean.processController( request, new MockHttpServletResponse( ) ) );
        assertTrue( savedStates( _formA ).isEmpty( ) );
    }

    /**
     * Without a form id, with an id that is not a number or with the id of no form, the settings view opens the list of
     * the forms
     *
     * @throws AccessDeniedException
     *             if the view is refused
     */
    @Test
    public void testModifyViewWithoutValidFormOpensTheList( ) throws AccessDeniedException
    {
        for ( String strIdForm : new String [ ] {
                null, "", "abc", "999999"
        } )
        {
            MockHttpServletRequest request = newRequest( );
            request.addParameter( "view", ACTION_MODIFY );
            if ( strIdForm != null )
            {
                request.addParameter( PARAMETER_ID_FORM, strIdForm );
            }
            RedirectResponse response = new RedirectResponse( );
            _bean.processController( request, response );

            String strLocation = response._strLocation;
            assertTrue( strLocation != null && strLocation.contains( VIEW_MANAGE ), "idForm=" + strIdForm + " : " + strLocation );
        }
    }

    private List<Integer> savedStates( Form form )
    {
        return OptionalStatusIndexationHome.getOptionalStatusIndexationListByFormId( form.getId( ) ).stream( )
                .map( OptionalStatusIndexation::getIdStatus ).collect( Collectors.toList( ) );
    }

    private Form createForm( String strTitle )
    {
        Form form = new Form( );
        form.setTitle( strTitle );
        form.setBreadcrumbName( "forms.horizontalBreadcrumb" );
        form.setIdWorkflow( _workflow.getId( ) );
        FormHome.create( form );

        return form;
    }

    private MockHttpServletRequest newRequest( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setMethod( "GET" );
        AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), RIGHT );

        return request;
    }

    /**
     * A response that keeps the URL of the redirect
     */
    private static final class RedirectResponse extends MockHttpServletResponse
    {
        private String _strLocation;

        @Override
        public void sendRedirect( String strLocation )
        {
            _strLocation = strLocation;
        }
    }
}
