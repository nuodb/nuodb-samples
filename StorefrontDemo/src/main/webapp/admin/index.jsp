<!-- Copyright (c) 2013-2015 NuoDB, Inc. -->
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-Frame-Options" content="deny">
    <title></title>
    <link rel="shortcut icon" href="favicon.ico" />
    <link rel="stylesheet" type="text/css" href="ext-js/resources/css/ext-all-gray.css" />
    <link rel="stylesheet" type="text/css" href="css/theme.css" />
    <link rel="stylesheet" type="text/css" href="css/app.css" />
    <script type="text/javascript" src="lib-js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="lib-js/jquery.sparkline.min.js"></script>
    <script>
    <% String port = new java.util.Properties(System.getProperties()).getProperty("storefront.dbapi.port", "8888"); %>
    document.write('\x3Cscript type="text/javascript" src="//' + window.location.hostname + ':<%=port%>/header">\x3C/script>');
    </script>
    <script type="text/javascript" src="ext-js/ext-all-dev.js"></script>
    <script type="text/javascript" src="app.js"></script>
</head>
<body>
</body>
</html>
