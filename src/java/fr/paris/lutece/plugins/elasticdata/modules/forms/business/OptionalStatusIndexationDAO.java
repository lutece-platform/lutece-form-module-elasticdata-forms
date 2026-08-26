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
import fr.paris.lutece.util.sql.DAOUtil;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for OptionalStatusIndexation objects
 */
@ApplicationScoped
public final class OptionalStatusIndexationDAO implements IOptionalStatusIndexationDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_optional_status_indexation, id_form, id_status FROM elasticdata_forms_optionalstatus WHERE id_optional_status_indexation = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO elasticdata_forms_optionalstatus ( id_form, id_status ) VALUES ( ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM elasticdata_forms_optionalstatus WHERE id_optional_status_indexation = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE elasticdata_forms_optionalstatus SET id_form = ?, id_status = ? WHERE id_optional_status_indexation = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_optional_status_indexation, id_form, id_status FROM elasticdata_forms_optionalstatus";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_optional_status_indexation FROM elasticdata_forms_optionalstatus";
    private static final String SQL_QUERY_SELECTALL_BY_FORM_ID = "SELECT id_optional_status_indexation, id_form, id_status FROM elasticdata_forms_optionalstatus WHERE id_form = ?";
    private static final String SQL_QUERY_SELECT_BY_STATUS_ID = "SELECT id_optional_status_indexation, id_form, id_status FROM elasticdata_forms_optionalstatus WHERE id_status = ?";

    @Override
    public void insert( OptionalStatusIndexation optionalStatusIndexation, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, optionalStatusIndexation.getIdForm( ) );
            daoUtil.setInt( nIndex++, optionalStatusIndexation.getIdStatus( ) );

            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                optionalStatusIndexation.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    @Override
    public OptionalStatusIndexation load( int nKey, Plugin plugin )
    {
        OptionalStatusIndexation optionalStatusIndexation = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                optionalStatusIndexation = loadFromDaoUtil( daoUtil );
            }
        }

        return optionalStatusIndexation;
    }

    @Override
    public OptionalStatusIndexation loadByStatusId( int nStatusId, Plugin plugin )
    {
        OptionalStatusIndexation optionalStatusIndexation = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_STATUS_ID, plugin ) )
        {
            daoUtil.setInt( 1, nStatusId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                optionalStatusIndexation = loadFromDaoUtil( daoUtil );
            }
        }

        return optionalStatusIndexation;
    }

    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void store( OptionalStatusIndexation optionalStatusIndexation, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, optionalStatusIndexation.getIdForm( ) );
            daoUtil.setInt( nIndex++, optionalStatusIndexation.getIdStatus( ) );
            daoUtil.setInt( nIndex, optionalStatusIndexation.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<OptionalStatusIndexation> selectOptionalStatusIndexationsList( Plugin plugin )
    {
        List<OptionalStatusIndexation> optionalStatusIndexationList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                optionalStatusIndexationList.add( loadFromDaoUtil( daoUtil ) );
            }
        }

        return optionalStatusIndexationList;
    }

    @Override
    public List<OptionalStatusIndexation> selectOptionalStatusIndexationsListByFormId( int nFormId, Plugin plugin )
    {
        List<OptionalStatusIndexation> optionalStatusIndexationList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_FORM_ID, plugin ) )
        {
            daoUtil.setInt( 1, nFormId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                optionalStatusIndexationList.add( loadFromDaoUtil( daoUtil ) );
            }
        }

        return optionalStatusIndexationList;
    }

    @Override
    public List<Integer> selectIdOptionalStatusIndexationsList( Plugin plugin )
    {
        List<Integer> optionalStatusIndexationList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                optionalStatusIndexationList.add( daoUtil.getInt( 1 ) );
            }
        }

        return optionalStatusIndexationList;
    }

    /**
     * Build an OptionalStatusIndexation from the current row of a DAOUtil
     * 
     * @param daoUtil
     *            the DAOUtil positioned on the row to read
     * @return the OptionalStatusIndexation
     */
    private OptionalStatusIndexation loadFromDaoUtil( DAOUtil daoUtil )
    {
        OptionalStatusIndexation optionalStatusIndexation = new OptionalStatusIndexation( );
        int nIndex = 1;
        optionalStatusIndexation.setId( daoUtil.getInt( nIndex++ ) );
        optionalStatusIndexation.setIdForm( daoUtil.getInt( nIndex++ ) );
        optionalStatusIndexation.setIdStatus( daoUtil.getInt( nIndex ) );

        return optionalStatusIndexation;
    }
}
