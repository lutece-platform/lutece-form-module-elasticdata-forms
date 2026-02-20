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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractFilterDao 
{	
	//Maps containing names and types of each databases column associated to a business class attribute 
	protected HashMap<String,String> _mapSql;
	
	//Prefix 
	private final static String PREFIX_GET = "get";
	private final static String PREFIX_IS = "is";
	
	//Constants SQL
	private final static String SQL_WHERE =" WHERE 1 ";
	private final static String SQL_ORDER_BY =" ORDER BY ";
	private final static String SQL_EQUAL =" = ? ";
	private final static String SQL_LIKE =" LIKE ? ";
	private final static String SQL_AND = " AND ";
	private final static String SQL_ASC =" ASC ";
	private final static String SQL_DESC =" DESC ";
	
	//types only allowed for research
	protected final static String TYPE_DATE = "Date";
	protected final static String TYPE_STRING = "String";
	protected final static String TYPE_BOOLEAN = "boolean";
	protected final static String TYPE_INT = "int";

	//List of constraints
	private final static List<String> _listPrefixToRemove = Arrays.asList( PREFIX_GET,PREFIX_IS );
	protected final static List<String> _listTypeAllowedForSearch = Arrays.asList( TYPE_DATE,TYPE_STRING,TYPE_BOOLEAN,TYPE_INT );
	
	/**
	* Preparation of filterStatement
	*
	* @param mapFilterCriteria 
	*			 contains searchbar names/values inputs 
	* @param strColumnToOrder 
	* 			 contains the column name to use for orderBy statement in case of sorting request ( must be null )
	* @param strSortMode 
	*			 contains the sortMode in case of sorting request : ASC or DESC ( must be null )
	* @return a string with the WHERE part and the ORDER BY part of the sql statement
	*/
	protected String prepareSelectStatement( String SQL_QUERY_SELECTALL_ID,Map <String,String> mapFilterCriteria, String strColumnToOrder, String strSortMode ) 
	{			
		StringBuilder builder = new StringBuilder( );

        builder.append( SQL_QUERY_SELECTALL_ID );
        builder.append( addWhereClauses( mapFilterCriteria ) );
        builder.append( addOrderByClause( strColumnToOrder,strSortMode ) );

        return  builder.toString( );		
	}

	/**
	*  add Where clause to the filterStatement
	*
	*  @param mapFilterCriteria 
	*			  contains name and value of each where clause
	*  @return the where part of the filterStatement
	*/	
	protected String addWhereClauses( Map<String, String> mapFilterCriteria ) 
	{
		StringBuilder WhereClauses = new StringBuilder( );
		
		if ( !mapFilterIsEmpty( mapFilterCriteria ) ) 
		{
			WhereClauses.append( SQL_WHERE );
			
			for( Map.Entry<String, String> filter : mapFilterCriteria.entrySet( ) ) 
			{
				//Check if a value was passed for the search 
				if ( StringUtils.isNotBlank( filter.getValue( ) ) ) 
				{
					//Check if the criteria name match with a BDD column name and if the type of this column is allowed for a search
					if ( _mapSql.containsKey( filter.getKey( ) ) && _listTypeAllowedForSearch.contains( _mapSql.get( filter.getKey( ) ) ) ) 
					{
						WhereClauses.append( SQL_AND );
						WhereClauses.append( filter.getKey( ) );
						WhereClauses.append( addWhereClauseOperator( filter.getKey( ) ) );
					}
				}
			}			
		}
		
		return WhereClauses.toString( );
	}; 
	    
	/**
	* add OrderBy columns to the filterStatement
	*
	* @param strColumnToOrder 
	*			  contains the column name to use for orderBy statement in case of sorting request ( must be null )
	* @param strSortMode 
	*			  contains the sortMode in case of sorting request : ASC or DESC ( must be null )
	* @return the orderBy part of the filterStatement
	*/
	protected String addOrderByClause( String strColumnToOrder,String strSortMode ) 
	{		
		if ( StringUtils.isNotBlank( strColumnToOrder ) && _mapSql.containsKey( strColumnToOrder ) ) 
		{	
			StringBuilder orderByClauses = new StringBuilder( );
			
			orderByClauses.append( SQL_ORDER_BY );
			orderByClauses.append( strColumnToOrder );
			orderByClauses.append( strSortMode );				
			
			return orderByClauses.toString( ); 
		}

		return "";
	} 
	
    /**
    * Check if _mapFilter is empty
    *
    * @return boolean
    */   
    private boolean mapFilterIsEmpty( Map<String,String> mapFilterCriteria ) 
    {
    	for ( Map.Entry<String, String> entry : mapFilterCriteria.entrySet( ) ) 
    	{
    		if ( StringUtils.isNotBlank( entry.getValue( ) ) ) 
    		{
    			return false;
    		}
        }
    	
    	return true;
	}
	
	/**
	* add where clause operator to the filterStatement
	*
	* @param strWhereClauseColumn 
	*			  contains one of the column names to use for where clause part
	* @return operator to use for the clause passed in argument
	*/
    private String addWhereClauseOperator( String strWhereClauseColumn ) 
    {
    	if ( _mapSql.containsKey( strWhereClauseColumn ) ) 
    	{	
    		switch ( _mapSql.get( strWhereClauseColumn ) ) 
    		{
    			case TYPE_DATE :
    				return SQL_EQUAL;
    			case TYPE_STRING :
    				return SQL_LIKE;  			
    			case TYPE_BOOLEAN :
    				return SQL_EQUAL;
    			case TYPE_INT :
    				return SQL_EQUAL;
    			default :
    				return SQL_LIKE; //Other types will be managed as strings
    		}
    	}
            	
    	return SQL_LIKE; //Other types will be managed as strings
    }
    
    /**
    * Return name of column in sql database format from getter name of business class exemples : getImageUrl -> image_url , getCost -> cost
    *
    * @return the name of column in sql database   
    */
    private String getFormatedColumnName( String strAttributeName, String strPrefixToCut )
    {
    	//Remove prefix ( get or is ) and lowercase the first character
    	String strRemovePrefix = StringUtils.uncapitalize( strAttributeName.substring( strPrefixToCut.length( ) ) ).toString( );
    	
    	StringBuilder builder = new StringBuilder( );
    	
    	//Change uppercase character to lowercase and add an underscore in front of it. exemple : dateStart -> date_start 
    	for( char c: strRemovePrefix.toCharArray( ) ) 
    	{    		
    		if ( Character.isUpperCase( c ) ) 
    		{
    			builder.append( '_' );
    			builder.append( Character.toLowerCase( c ) );
    		}
    		else 
    		{
    			builder.append( c );
    		}
    	}
    	
    	return builder.toString( );
    }
    
    /**
    * Initialization of mapSql. 
    *
    * mapSql Containing names and types of each databases column associated to a business class attribute.
    */
	protected void initMapSql( Class<?> businessClass ) 
	{		
		_mapSql = new HashMap<>( );
			
		for ( Method method : businessClass.getDeclaredMethods( ) ) 
		{		
			for( String prefix : _listPrefixToRemove ) 
			{
				//Use only getter and is function of business class to infer database name of each attributes
				if ( method.getName( ).startsWith( prefix ) )
				{
					_mapSql.put( getFormatedColumnName( method.getName( ),prefix ),method.getReturnType( ).getSimpleName( ) ); 
				}
			}
		}
	}
}
 
