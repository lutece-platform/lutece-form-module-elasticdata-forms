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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for OptionalQuestionIndexation objects
 */
public final class OptionalQuestionIndexationHome
{
    // Static variable pointed at the DAO instance
    private static IOptionalQuestionIndexationDAO _dao = SpringContextService.getBean( "elasticdata-forms.optionalQuestionIndexationDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "elasticdata-forms" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private OptionalQuestionIndexationHome( )
    {
    }

    /**
     * Create an instance of the optionalQuestionIndexation class
     * 
     * @param optionalQuestionIndexation
     *            The instance of the OptionalQuestionIndexation which contains the informations to store
     * @return The instance of optionalQuestionIndexation which has been created with its primary key.
     */
    public static OptionalQuestionIndexation create( OptionalQuestionIndexation optionalQuestionIndexation )
    {
        _dao.insert( optionalQuestionIndexation, _plugin );
        return optionalQuestionIndexation;
    }

    /**
     * Update of the optionalQuestionIndexation which is specified in parameter
     * 
     * @param optionalQuestionIndexation
     *            The instance of the OptionalQuestionIndexation which contains the data to store
     * @return The instance of the optionalQuestionIndexation which has been updated
     */
    public static OptionalQuestionIndexation update( OptionalQuestionIndexation optionalQuestionIndexation )
    {
        _dao.store( optionalQuestionIndexation, _plugin );

        return optionalQuestionIndexation;
    }

    /**
     * Remove the optionalQuestionIndexation whose identifier is specified in parameter
     * 
     * @param nKey
     *            The optionalQuestionIndexation Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a optionalQuestionIndexation whose identifier is specified in parameter
     * 
     * @param nKey
     *            The optionalQuestionIndexation primary key
     * @return an instance of OptionalQuestionIndexation
     */
    public static OptionalQuestionIndexation findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Returns an instance of a optionalQuestionIndexation whose identifier is specified in parameter
     * 
     * @param nKey
     *            The optionalQuestionIndexation primary key
     * @return an instance of OptionalQuestionIndexation
     */
    public static OptionalQuestionIndexation findByQuestionId( int nQuestionId )
    {
        return _dao.loadByQuestionId( nQuestionId, _plugin );
    }

    /**
     * Load the data of all the optionalQuestionIndexation objects and returns them as a list
     * 
     * @return the list which contains the data of all the optionalQuestionIndexation objects
     */
    public static List<OptionalQuestionIndexation> getOptionalQuestionIndexationsList( )
    {
        return _dao.selectOptionalQuestionIndexationsList( _plugin );
    }

    /**
     * Load the id of all the optionalQuestionIndexation objects and returns them as a list
     * 
     * @return the list which contains the id of all the optionalQuestionIndexation objects
     */
    public static List<Integer> getIdOptionalQuestionIndexationsList( )
    {
        return _dao.selectIdOptionalQuestionIndexationsList( _plugin );
    }

    /**
     * Load the data of all the optionalQuestionIndexation objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the optionalQuestionIndexation objects
     */
    public static List<OptionalQuestionIndexation> getOptionalQuestionIndexationListByFormId( int nFormId )
    {
        return _dao.selectOptionalQuestionIndexationsListByFormId( nFormId, _plugin );
    }

}
