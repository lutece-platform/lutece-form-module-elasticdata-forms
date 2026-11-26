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
package fr.paris.lutece.plugins.elasticdata.modules.forms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.action.ActionFilter;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;

/**
 * A form without workflow (identifier -1) gets no state and no action
 */
public class FormWorkflowServiceTest
{
    private static final int WORKFLOW_A = 1;
    private static final int WORKFLOW_B = 2;

    /**
     * Two workflows with one state each; the filter behaves like the real one: -1 means any workflow
     */
    private static IStateService stateService( )
    {
        return (IStateService) Proxy.newProxyInstance( FormWorkflowServiceTest.class.getClassLoader( ), new Class<?> [ ] {
                IStateService.class
        }, ( proxy, method, args ) -> {
            if ( "getListStateByFilter".equals( method.getName( ) ) )
            {
                int nIdWorkflow = ( (StateFilter) args [0] ).getIdWorkflow( );
                List<State> list = new ArrayList<>( );
                for ( int nId : new int [ ] {
                        WORKFLOW_A, WORKFLOW_B
                } )
                {
                    if ( nIdWorkflow == -1 || nIdWorkflow == nId )
                    {
                        State state = new State( );
                        state.setId( nId * 10 );
                        list.add( state );
                    }
                }
                return list;
            }
            throw new UnsupportedOperationException( method.getName( ) );
        } );
    }

    private static IActionService actionService( )
    {
        return (IActionService) Proxy.newProxyInstance( FormWorkflowServiceTest.class.getClassLoader( ), new Class<?> [ ] {
                IActionService.class
        }, ( proxy, method, args ) -> {
            if ( "getListActionByFilter".equals( method.getName( ) ) )
            {
                int nIdWorkflow = ( (ActionFilter) args [0] ).getIdWorkflow( );
                List<Action> list = new ArrayList<>( );
                for ( int nId : new int [ ] {
                        WORKFLOW_A, WORKFLOW_B
                } )
                {
                    if ( nIdWorkflow == -1 || nIdWorkflow == nId )
                    {
                        Action action = new Action( );
                        action.setId( nId * 10 );
                        list.add( action );
                    }
                }
                return list;
            }
            throw new UnsupportedOperationException( method.getName( ) );
        } );
    }

    @Test
    public void testFormWithoutWorkflowGetsNoState( )
    {
        assertTrue( FormWorkflowService.getStateList( stateService( ), -1 ).isEmpty( ) );
        assertTrue( FormWorkflowService.getStateList( stateService( ), 0 ).isEmpty( ) );
    }

    @Test
    public void testFormWithoutWorkflowGetsNoAction( )
    {
        assertTrue( FormWorkflowService.getActionList( actionService( ), -1 ).isEmpty( ) );
    }

    @Test
    public void testFormWithWorkflowGetsItsStatesOnly( )
    {
        List<State> list = FormWorkflowService.getStateList( stateService( ), WORKFLOW_B );
        assertEquals( 1, list.size( ) );
        assertEquals( WORKFLOW_B * 10, list.get( 0 ).getId( ) );
        assertEquals( 1, FormWorkflowService.getActionList( actionService( ), WORKFLOW_A ).size( ) );
    }
}
