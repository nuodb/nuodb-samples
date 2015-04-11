<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Welcome to the NuoDB Storefront Demo!</h1>
    <t:messages />
    
    <div class="alert alert-block alert-info hide" id="api-box">
        <p>Before you can use this demo, you must specify domain credentials to connect to NuoDB.<br> &nbsp;</p>
        
        <form class="form-horizontal" method="post">
            <div class="control-group">
                <label class="control-label" for="api-username">Domain username:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="api-username" name="api-username" placeholder="Username">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="api-password">Domain password:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="api-password" name="api-password" placeholder="Password">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="api-url">API URL:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="api-url" name="api-url" placeholder="URL">
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-info" type="submit" value="Connect to API">Connect to API</button>
                </div>
            </div>
            <input type="hidden" name="btn-msg" value="api" />
        </form>
    </div>

    <div class="alert alert-block alert-info hide" id="create-db-box">
        <p>The Storefront database does not exist yet.  Use this form to create it.<br> &nbsp;</p>
        
        <form class="form-horizontal" method="post">
            <div class="control-group">
                <label class="control-label" for="username">Database username:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="username" name="username" placeholder="Username">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="password">Database password:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="password" name="password" placeholder="Password">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="url">Broker URL:</label>
                <div class="controls">
                    <textarea class="input-xxlarge no-resize-x" id="url" name="url" placeholder="URL" rows="4"></textarea>
                    <p><small>Tip: You may change the database name by editing this URL.  You may also specify multiple brokers for failover support.<br />
                        Syntax: <code> jdbc:com.nuodb://{broker1}:{port1},{broker2}:{port2},..,{brokerN}:{portN}/{db-name}?{params}</code></small></p>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-info" type="submit" value="Create database">Create database</button>
                </div>
            </div>
            <input type="hidden" name="btn-msg" value="db" />
        </form>
    </div>

    <div id="welcome">
        <div class="row">
            <div class="span8">
                <br />
                <p>This is an e-commerce web application with a built-in ability to simulate activity from thousands of users. You can use it to see how NuoDB shines under a variety of scenarios.</p>

                <p>Get started with a <a href="./tour-scale-out${qs}">guided tour</a> (see left sidebar). These tours showcase NuoDB’s value propositions in your environment.  Or visit the <a href="./store-products${qs}">Storefront Website</a>.</p>

                <p>
                    The Storefront demo is an open source application written in Java. It uses Hibernate for data access with NuoDB’s JDBC driver and DataSource for connection pooling. <a href="https://github.com/nuodb/nuodb-samples/tree/master/StorefrontDemo" target="_blank">Check it out on GitHub</a>.
                </p>
            </div>
            <div class="span4">
                <img src="img/cart.png" />
            </div>
        </div>
    </div>
</t:page>
