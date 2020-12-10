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

import fr.paris.lutece.plugins.elasticdata.business.AbstractDataObject;

/**
 * FormResponseDataObject
 */
public class FormResponseHistoryDataObject extends AbstractDataObject
{
    private String _strId;
    private String _strFormName;
    private int _nFormId;
    private int _nFormResponseId;
    private String _strActionName;
    private long _lTaskDuration;
    private String _strUnitName;
    
    /**
     * Returns the Form response id
     * 
     * @return The Form response id
     */
    public String getId( )
    {
        return _strId;
    }

    /**
     * Sets the Form response id
     * 
     * @param strId
     *            The Form response id
     */
    public void setId( String strId )
    {
        _strId = strId;
    }

    /**
     * Returns the Form id
     * 
     * @return The Form id
     */
    public int getFormId( )
    {
        return _nFormId;
    }

    /**
     * Sets the Form id
     * 
     * @param nFormId
     *            The Form id
     */
    public void setFormId( int nFormId )
    {
        _nFormId = nFormId;
    }

     /**
     * Returns the Form response id
     * 
     * @return The Form response id
     */
    public int getFormResponseId( )
    {
        return _nFormResponseId;
    }

    /**
     * Sets the Form response id
     * 
     * @param nFormResponseId
     *            The Form response id
     */
    public void setFormResponseId( int nFormResponseId )
    {
        _nFormResponseId = nFormResponseId;
    }

    /**
     * Returns the Duration
     * 
     * @return The Duration
     */
    public long getTaskDuration( )
    {
        return _lTaskDuration;
    }

    /**
     * Sets the Duration
     * 
     * @param lTaskDuration
     *            The Duration
     */
    public void setTaskDuration( long lTaskDuration )
    {
        _lTaskDuration = lTaskDuration;
    }

    /**
     * Returns the Form Name
     * 
     * @return The Form Name
     */
    public String getFormName( )
    {
        return _strFormName;
    }

    /**
     * Sets the Form Name
     * 
     * @param strFormName
     *            The Form Name
     */
    public void setFormName( String strFormName )
    {
        _strFormName = strFormName;
    }

    /**
     * Returns the Unit Name
     * 
     * @return The Unit Name
     */
    public String getUnitName( )
    {
        return _strUnitName;
    }

    /**
     * Sets the Unit Name
     * 
     * @param strUnitName
     *            The Unit Name
     */
    public void setUnitName( String strUnitName )
    {
        _strUnitName = strUnitName;
    }

    /**
     * Returns the action name
     * 
     * @return The Action Name
     */
    public String getActionName( )
    {
        return _strActionName;
    }

    /**
     * Sets the Action Name
     * 
     * @param strActionName
     *            The Action Name
     */
    public void setActionName( String strActionName )
    {
        _strActionName = strActionName;
    }
}