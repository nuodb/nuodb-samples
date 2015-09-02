<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page import="com.nuodb.storefront.servlet.BaseServlet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%
    String tenant = StringEscapeUtils.escapeHtml4((String) request.getAttribute(BaseServlet.ATTR_TENANT));
    request.setAttribute("tenantEsc", tenant);
%>

<t:page showHeader="false">
    <t:messages />

    <h1>Sorry, &ldquo;${tenantEsc}&rdquo; tenant does not exist</h1>
    <p>The tenant may have been shut down.</p>
    <p>&nbsp;</p>    
    <p>
        <a class="btn btn-info" target="_top" href="admin/">Switch to default tenant</a>&nbsp;
        <button class="btn btn-default" onclick="document.location.reload();"><i class="icon-repeat"></i> Retry</button>&nbsp;
        <button class="btn btn-default" onclick="window.top.close();"><i class="icon-remove"></i> Close tab</button>&nbsp;
    </p>

</t:page>
