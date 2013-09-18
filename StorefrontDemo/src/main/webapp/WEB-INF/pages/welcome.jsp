<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="welcome">
        <!-- Top nav -->
        <div id="top-bar" class="navbar">
            <label>Welcome to the NuoDB Storefront Demo!</label>
            <div class="navbar-inner">
                <ul class="nav pull-right">
                    <t:admin-link />
                    <li><a href="https://github.com/nuodb/nuodb-samples/tree/master/StorefrontDemo" target="_blank" title="Opens the source code in a new tab (hosted by GitHub)"><i class="icon-github"></i> README</a></li>
                    <li id="lnk-show-ddl"><a href="#" title="Displays the drop and create DDL statements of the Storefront schema"><i class="icon-align-justify"></i> DDL</a></li>
                    <li><a href="http://www.nuodb.com/groups/dev-center/" target="_blank" title="Goes to the NuoDB Dev Center for technical details about this sample and NuoDB"><i class="icon-nuodb"></i> DevCenter</a></li>
                </ul>
            </div>
        </div>
        
        <t:messages />

        <!-- DDL -->
        <div class="row" id="ddl">
            <div class="span12">
                <h2>Storefront DDL</h2>
                <textarea></textarea>
            </div>
        </div>
        <script id="tpl-ddl" type="text/template">{{result}}</script>

        <!-- Carousel -->
        <div id="carousel" class="carousel slide">
            <ol class="carousel-indicators">
                <li data-target="#carousel" data-slide-to="0" class="active"></li>
                <li data-target="#carousel" data-slide-to="1" class=""></li>
                <li data-target="#carousel" data-slide-to="2" class=""></li>
                <li data-target="#carousel" data-slide-to="3" class=""></li>
            </ol>
            <div class="carousel-inner">
                <div class="item active">
                    <img src="img/carousel/cart.png" height="347" />
                    <div class="carousel-caption">
                        <h2>
                            Go on...<br />
                            <b>kick the tires</b>
                        </h2>
                        <h4>
                            Dive in to this <b>practical example</b> of a<br />Java web application &ndash; showcasing the<br />key benefits of NuoDB.
                        </h4>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/flock.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            &nbsp;<br />Elasticity
                        </h3>
                        <h4>
                            <b>Open the floodgates</b>
                        </h4>
                        <p>
                            See how NuoDB reacts to a sudden flock of customers.<br />Use the <b>Simulated User Setup</b> and <b>Node Setup</b> forms below<br />to increase the number of customer workloads and NuoDB nodes.
                        </p>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/bags.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            Multi-<br />Tenancy
                        </h3>
                        <h4>
                            <b>Hang some new shingles</b>
                        </h4>
                        <p>
                            Launch multiple stores with the same NuoDB nodes.<br />Click <b>Show DDL</b> and you&rsquo;ll have the scripts<br />to create your additional databases.
                        </p>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/switch.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            Variable<br />Workloads
                        </h3>
                        <h4>
                            <b>Try a bait &amp; switch</b>
                        </h4>
                        <p>
                            Pull a 180 by changing the mix of OLTP and analytic transactions.<br />Use the <b>Simulated Users</b> form below to change<br />the ratio of back office to shopper workloads.
                        </p>
                    </div>
                </div>
            </div>
            <a class="left carousel-control" href="#carousel" data-slide="prev">‹</a> <a class="right carousel-control" href="#carousel" data-slide="next">›</a>
        </div>
    </div>
    
    <div class="footer">Copyright &copy; 2013 NuoDB, Inc. All rights reserved.</div>
</t:page>
