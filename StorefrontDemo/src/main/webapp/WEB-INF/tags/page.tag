<%@tag description="Page template" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
<head>
<title>Storefront Demo</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link href="css/bootstrap.min.css" rel="stylesheet" />
<link href="css/rateit.css" rel="stylesheet" />
<link href="css/app.css" rel="stylesheet" />
<link rel="icon" type="image/png" href="favicon.ico" />
</head>
<body>
    <div class="container">

        <!-- Top nav bar -->
        <div id="top-bar" class="navbar">
            <div class="navbar-inner">
                <a class="brand" href="index.jsp"> <img data-src="holder.js/260x180" alt="Logo" src="img/shop-logo.png" /> <label>Default Storefront</label>
                </a>
                <form class="navbar-search text-center" method="GET" action="index.jsp">
                    <input type="text" id="search" class="search-query" name="search" placeholder="Search" /> <i class="icon-search"></i>
                </form>

                <div class="pull-right">
                    <ul class="nav pull-right">
                        <li><p class="navbar-text">
                                Hello, <b>Customer</b>
                            </p></li>
                        <li class="divider-vertical"></li>
                        <li class="cart"><a href="cart.jsp"><i class="icon icon-shopping-cart"></i> Cart <span class="badge badge-info">0</span></a></li>
                    </ul>
                </div>
            </div>
        </div>

        <jsp:doBody />

        <div class="footer">Copyright &copy; Default Storefront 2013. All rights reserved.</div>
    </div>

    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery.rateit.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src="js/handlebars.js"></script>
    <script src="js/app.js"></script>
</body>
</html>
