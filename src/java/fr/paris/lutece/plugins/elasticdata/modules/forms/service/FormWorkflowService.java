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

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.action.ActionFilter;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;

/**
 * States and actions of the workflow of a form. A form without workflow has the identifier -1, which the workflow filters
 * read as "any workflow": it must get no state and no action, not those of every workflow of the site.
 */
public final class FormWorkflowService
{
    /**
     * Private constructor
     */
    private FormWorkflowService( )
    {
    }

    /**
     * Tell whether a form workflow identifier designates a workflow
     *
     * @param nIdWorkflow
     *            the workflow identifier of the form
     * @return true if the form has a workflow
     */
    public static boolean hasWorkflow( int nIdWorkflow )
    {
        return nIdWorkflow > 0;
    }

    /**
     * Return the states of the workflow of a form
     *
     * @param stateService
     *            the state service
     * @param nIdWorkflow
     *            the workflow identifier of the form
     * @return the states of that workflow, an empty list for a form without workflow
     */
    public static List<State> getStateList( IStateService stateService, int nIdWorkflow )
    {
        if ( !hasWorkflow( nIdWorkflow ) )
        {
            return new ArrayList<>( );
        }

        StateFilter stateFilter = new StateFilter( );
        stateFilter.setIdWorkflow( nIdWorkflow );

        return stateService.getListStateByFilter( stateFilter );
    }

    /**
     * Return the manual actions of the workflow of a form
     *
     * @param actionService
     *            the action service
     * @param nIdWorkflow
     *            the workflow identifier of the form
     * @return the actions of that workflow, an empty list for a form without workflow
     */
    public static List<Action> getActionList( IActionService actionService, int nIdWorkflow )
    {
        if ( !hasWorkflow( nIdWorkflow ) )
        {
            return new ArrayList<>( );
        }

        ActionFilter actionFilter = new ActionFilter( );
        actionFilter.setIdWorkflow( nIdWorkflow );
        actionFilter.setAutomaticReflexiveAction( false );

        return actionService.getListActionByFilter( actionFilter );
    }
}
