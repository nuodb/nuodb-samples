<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@tag description="Page template" pageEncoding="UTF-8" import="com.nuodb.storefront.StorefrontApp,com.nuodb.storefront.model.dto.PageConfig,com.nuodb.storefront.servlet.BaseServlet,com.sun.jersey.api.uri.UriComponent"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="showHeader" required="false" type="java.lang.Boolean" %>
<%
    PageConfig cfg = (PageConfig)request.getAttribute(BaseServlet.ATTR_PAGE_CONFIG);
    String qs = "?tenant=" + UriComponent.encode(cfg.getLocalInstance().getTenantName(), UriComponent.Type.QUERY_PARAM);
    request.setAttribute("qs", qs);
%>
<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <title><%=cfg.getPageTitle()%></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link href="css/bootstrap.min.css" rel="stylesheet" />
    <link href="css/jquery.form.css" rel="stylesheet" />
    <link href="css/rateit.css" rel="stylesheet" />
    <link href="css/Storefront.css?v=2.1.0" rel="stylesheet" />
    <link rel="icon" type="image/png" href="favicon.ico" />
</head>
<body>
    <div class="container">
        <% if (showHeader == null || showHeader == true) { %>
        <!-- Top nav bar -->
        <div id="top-bar" class="navbar">
            <div class="navbar-inner">
                <label><a href="store-products${qs}" id="storefront-name"></a></label>
                
                <form class="navbar-search search text-center" method="GET" action="store-products">
                    <input type="text" id="search" class="search-query" name="search" placeholder="Search" />
                    <div class="search-icon">
                        <i class="search-glass"></i>
                        <a class="search-clear" class="close">&times;</a>
                    </div>
                </form>

                <div class="pull-right">
                    <ul class="nav pull-right">
                        <li class="username"><p class="navbar-text">
                                Hello, <b><%=cfg.getCustomer().getDisplayName()%></b>
                            </p></li>
                        <li class="divider-vertical"></li>
                        <li class="cart"><a href="store-cart${qs}"><i class="icon icon-shopping-cart"></i> Cart <span class="badge badge-info"><%=cfg.getCustomer().getCartItemCount()%></span></a></li>
                    </ul>
                </div>
            </div>
        </div>
        <t:messages />
        <% } %>

        <!-- Page-specific content -->
        <jsp:doBody />

        <% if (showHeader == null || showHeader == true) { %>
        <!-- Footer  -->
        <div class="footer">Copyright &copy; 2013-2015 NuoDB, Inc. All rights reserved.</div>
        <% } %>
    </div>
    
    <div id="progress-container" class="hide">
        <div class="progress progress-striped active">
            <div class="bar" style="width: 0%;"></div>
        </div>
    </div>

    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.form.js"></script>
    <script type="text/javascript" src="js/jquery.rateit.min.js"></script>
    <script type="text/javascript" src="js/bootstrap.min.js"></script>
    <script type="text/javascript" src="js/handlebars.js"></script>
    <script type="text/javascript" src="js/date.format.js"></script>
    <script type="text/javascript" src="js/Storefront.js?v=2.1.0"></script>
    <script type="text/javascript" src="js/Storefront.ControlPanel.Users.js?v=2.1.0"></script>
    <script type="text/javascript" src="js/Storefront.ControlPanel.List.js?v=2.1.0"></script>
    <script type="text/javascript" src="js/Storefront.Helpers.js?v=2.1.0"></script>
    <script type="text/javascript" src="js/Storefront.TemplateMgr.js?v=2.1.0"></script>
    <script>
    	$(document).ready(function() {
			Storefront.init(<%=cfg.toJson()%>);
    	});
	</script>
</body>
</html>
