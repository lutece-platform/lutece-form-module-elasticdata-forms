/*
 * Copyright (c) 2002-2019, Mairie de Paris
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

import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * ManageElasticData JSP Bean abstract class for JSP Bean
 */
@Controller( controllerJsp = "IndexingAppElasticData.jsp", controllerPath = "jsp/admin/plugins/elasticdata/modules/forms", right = "ELASTICDATA_FORMS_MANAGEMENT" )
public class IndexElasticDataJspBean extends MVCAdminJspBean
{
    private static final String TEMPLATE_HOME = "/admin/plugins/elasticdata/modules/forms/forms_elasticdata.html";
    private static final String PROPERTY_PAGE_TITLE = "module.description";
    private static final String VIEW_HOME = "home";
    private static final String ACTION_INDEX = "index";
    private static final String PARAMETER_DATA_SOURCE = "data_source";
    private static final long serialVersionUID = 1L;

    /**
     * View the home of the feature
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_HOME, defaultView = true )
    public String getIndexingElasticData( HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );
        return getPage( PROPERTY_PAGE_TITLE, TEMPLATE_HOME, model );
    }

    /**
     * Process the full indexing of a given data source
     * 
     * @param request
     *            The HTTP request
     * @return The redirected page
     */
    @Action( ACTION_INDEX )
    public String doIndex( HttpServletRequest request ) throws ElasticClientException
    {
        StringBuilder sbLogs = new StringBuilder( );
        String strDataSourceId = request.getParameter( PARAMETER_DATA_SOURCE );
        DataSource source = DataSourceService.getDataSource( strDataSourceId );
        DataSourceService.processFullIndexing( sbLogs, source, false, null );
        addInfo( sbLogs.toString( ) );
        return redirectView( request, VIEW_HOME );
    }
}
