<%@tag description="Page template" pageEncoding="UTF-8" import="com.nuodb.storefront.model.PageConfig,com.nuodb.storefront.servlet.BaseServlet"%>
<%
    PageConfig cfg = (PageConfig)request.getAttribute(BaseServlet.ATTR_PAGE_CONFIG);
%>
<!DOCTYPE html>

<html>
<head>
<title><%=cfg.getPageTitle()%></title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link href="css/bootstrap.min.css" rel="stylesheet" />
<link href="css/rateit.css" rel="stylesheet" />
<link href="css/Storefront.css" rel="stylesheet" />
<link rel="icon" type="image/png" href="favicon.ico" />
</head>
<body>
    <div class="container">
        <!-- Top nav bar -->
        <div id="top-bar" class="navbar">
            <div class="navbar-inner">
                <a class="brand" href="products"> <img data-src="holder.js/260x180" alt="Logo" src="img/shop-logo.png" /> <label><%=cfg.getStorefrontName()%></label>
                </a>
                <form class="navbar-search search text-center" method="GET" action="products">
                    <input type="text" id="search" class="search-query" name="search" placeholder="Search" />
                    <div class="search-icon">
                        <i class="search-glass"></i>
                        <a class="search-clear" class="close">&times;</a>
                    </div>
                </form>

                <div class="pull-right">
                    <ul class="nav pull-right">
                        <li><p class="navbar-text">
                                Hello, <b><%=cfg.getCustomer().getDisplayName()%></b>
                            </p></li>
                        <li class="divider-vertical"></li>
                        <li class="cart"><a href="cart"><i class="icon icon-shopping-cart"></i> Cart <span class="badge badge-info"><%=cfg.getCustomer().getCartItemCount()%></span></a></li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Status messages -->
        <div class="row">
            <div class="span12" id="messages"></div>
            <script id="tpl-messages" type="text/template">
            	{{#if result}}
	            	<div id="messages"></div>
	            		{{#result}}
	            			<div class="alert alert-block alert-{{lowerCaseFormat severity}}">
	            		    	<button type="button" class="close" data-dismiss="alert">&times;</button>
	            				{{message}}
	            			</div>
	            		{{/result}}
	            	</div>
	            {{/if}}
            </script>
        </div>

        <!-- Page-specific content -->
        <jsp:doBody />

        <!-- Footer  -->
        <div class="footer">Copyright &copy; 2013 <%=cfg.getStorefrontName()%>. All rights reserved.</div>
    </div>

    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery.rateit.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src="js/handlebars.js"></script>
    <script src="js/date.format.js"></script>
    <script src="js/Storefront.js"></script>
    <script src="js/Storefront.Helpers.js"></script>
    <script src="js/Storefront.TemplateMgr.js"></script>
    <script>
    	$(document).ready(function() {
			Storefront.init(<%=cfg.toJson()%>);
    	});
	</script>
</body>
</html>
