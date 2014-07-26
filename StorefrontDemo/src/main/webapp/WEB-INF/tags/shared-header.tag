<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@tag description="Shared header" pageEncoding="UTF-8"%>

<script>
<% String port = new java.util.Properties(System.getProperties()).getProperty("storefront.dbapi.port", "8888"); %>
document.write('\x3Cscript type="text/javascript" src="//' + window.location.hostname + ':<%=port%>/header">\x3C/script>');
</script>
