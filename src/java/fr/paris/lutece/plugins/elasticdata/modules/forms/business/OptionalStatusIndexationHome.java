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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class provides instances management methods (create, find, ...) for OptionalStatusIndexation objects
 */
public final class OptionalStatusIndexationHome
{
    // Static variable pointed at the DAO instance
    private static IOptionalStatusIndexationDAO _dao = SpringContextService.getBean( "elasticdata-forms.optionalStatusIndexationDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "elasticdata-forms" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private OptionalStatusIndexationHome(  )
    {
    }

    /**
     * Create an instance of the optionalStatusIndexation class
     *
     * @param optionalStatusIndexation The instance of the OptionalStatusIndexation which contains the informations to store
     * @return The  instance of optionalStatusIndexation which has been created with its primary key.
     */
    public static OptionalStatusIndexation create( OptionalStatusIndexation optionalStatusIndexation )
    {
        _dao.insert( optionalStatusIndexation, _plugin );

        return optionalStatusIndexation;
    }

    /**
     * Update of the optionalStatusIndexation which is specified in parameter
     *
     * @param optionalStatusIndexation The instance of the OptionalStatusIndexation which contains the data to store
     * @return The instance of the  optionalStatusIndexation which has been updated
     */
    public static OptionalStatusIndexation update( OptionalStatusIndexation optionalStatusIndexation )
    {
        _dao.store( optionalStatusIndexation, _plugin );

        return optionalStatusIndexation;
    }

    /**
     * Remove the optionalStatusIndexation whose identifier is specified in parameter
     *
     * @param nKey The optionalStatusIndexation Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a optionalStatusIndexation whose identifier is specified in parameter
     *
     * @param nKey The optionalStatusIndexation primary key
     * @return an instance of OptionalStatusIndexation
     */
    public static Optional<OptionalStatusIndexation> findByPrimaryKey( int nKey )
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
    public static OptionalStatusIndexation findByStatusId( int nStatusId )
    {
        return _dao.loadByStatusId( nStatusId, _plugin );
    }
    
    /**
     * Load the data of all the optionalStatusIndexation objects and returns them as a list
     *
     * @return the list which contains the data of all the optionalStatusIndexation objects
     */
    public static List<OptionalStatusIndexation> getOptionalStatusIndexationsList( )
    {
        return _dao.selectOptionalStatusIndexationsList( _plugin );
    }
    
    /**
     * Load the id of all the optionalStatusIndexation objects and returns them as a list
     *
     * @param mapFilterCriteria contains search bar names/values inputs 
     * @param strColumnToOrder contains the column name to use for orderBy statement in case of sorting request (must be null)
     * @param strSortMode contains the sortMode in case of sorting request : ASC or DESC (must be null)
     * @return the list which contains the id of all the project objects
     */
    public static List<Integer> getIdOptionalStatusIndexationsList( Map <String,String> mapFilterCriteria, String strColumnToOrder, String strSortMode )
    {
        return _dao.selectIdOptionalStatusIndexationsList( _plugin,mapFilterCriteria,strColumnToOrder,strSortMode );
    }
    
    /**
     * Load the data of all the optionalStatusIndexation objects and returns them as a referenceList
     *
     * @return the referenceList which contains the data of all the optionalStatusIndexation objects
     */
    public static ReferenceList getOptionalStatusIndexationsReferenceList( )
    {
        return _dao.selectOptionalStatusIndexationsReferenceList( _plugin );
    }
    
    /**
     * Load the data of all the avant objects and returns them as a list
     *
     * @param listIds liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<OptionalStatusIndexation> getOptionalStatusIndexationsListByIds( List<Integer> listIds )
    {
        return _dao.selectOptionalStatusIndexationsListByIds( _plugin, listIds );
    }
    
    /**
     * Load the data of all the optionalQuestionIndexation objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the optionalQuestionIndexation objects
     */
    public static List<OptionalStatusIndexation> getOptionalStatusIndexationListByFormId( int nFormId )
    {
        return _dao.selectOptionalStatusIndexationsListByFormId( nFormId, _plugin );
    }
}

