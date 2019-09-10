<jsp:useBean id="manageelasticdataapp" scope="session" class="fr.paris.lutece.plugins.elasticdata.modules.forms.web.IndexElasticDataJspBean" />
<% String strContent = manageelasticdataapp.processController ( request , response ); %>


<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../../../AdminFooter.jsp" %>

http://localhost:8080/elastic/jsp/admin/plugins/elasticdata/modules/forms/IndexingAppElasticData.jsp