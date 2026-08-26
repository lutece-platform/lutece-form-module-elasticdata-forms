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

import java.util.List;

/**
 * IOptionalStatusIndexationDAO Interface
 */
public interface IOptionalStatusIndexationDAO
{
    /**
     * Insert a new record in the table.
     * 
     * @param optionalStatusIndexation
     *            instance of the OptionalStatusIndexation object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( OptionalStatusIndexation optionalStatusIndexation, Plugin plugin );

    /**
     * Update the record in the table
     * 
     * @param optionalStatusIndexation
     *            the reference of the OptionalStatusIndexation
     * @param plugin
     *            the Plugin
     */
    void store( OptionalStatusIndexation optionalStatusIndexation, Plugin plugin );

    /**
     * Delete a record from the table
     * 
     * @param nKey
     *            The identifier of the OptionalStatusIndexation to delete
     * @param plugin
     *            the Plugin
     */
    void delete( int nKey, Plugin plugin );

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data from the table
     * 
     * @param nKey
     *            The identifier of the OptionalStatusIndexation
     * @param plugin
     *            the Plugin
     * @return The instance of the OptionalStatusIndexation
     */
    OptionalStatusIndexation load( int nKey, Plugin plugin );

    /**
     * Load the data of the OptionalStatusIndexation associated to a workflow status
     * 
     * @param nStatusId
     *            The identifier of the workflow status
     * @param plugin
     *            the Plugin
     * @return The instance of the OptionalStatusIndexation
     */
    OptionalStatusIndexation loadByStatusId( int nStatusId, Plugin plugin );

    /**
     * Load the data of all the optionalStatusIndexation objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the optionalStatusIndexation objects
     */
    List<OptionalStatusIndexation> selectOptionalStatusIndexationsList( Plugin plugin );

    /**
     * Load the data of all the optionalStatusIndexation objects of a given form and returns them as a list
     * 
     * @param nFormId
     *            The identifier of the form
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the optionalStatusIndexation objects
     */
    List<OptionalStatusIndexation> selectOptionalStatusIndexationsListByFormId( int nFormId, Plugin plugin );

    /**
     * Load the id of all the optionalStatusIndexation objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the id of all the optionalStatusIndexation objects
     */
    List<Integer> selectIdOptionalStatusIndexationsList( Plugin plugin );
}
