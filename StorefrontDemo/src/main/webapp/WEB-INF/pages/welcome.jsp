<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Welcome to the NuoDB Storefront Demo!</h1>
    <t:messages />

    <div class="alert alert-block alert-info hide" id="create-db-box">
        <p>The Storefront database does not exist yet.  Use this form to create it.<br> &nbsp;</p>
        
        <form class="form-horizontal" method="post">
            <div class="control-group">
                <label class="control-label" for="username">Username:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="username" name="username" placeholder="Username">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="password">Password:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="password" id="password" name="password" placeholder="Password">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="url">Broker URL:</label>
                <div class="controls">
                    <textarea class="input-xxlarge no-resize-x" id="url" name="url" placeholder="URL" rows="4"></textarea>
                    <p><small>Tip: You may change the database name and specify multiple brokers for failover support. The syntax is:<br /><tt> jdbc:com.nuodb://{broker1}:{port1},{broker2}:{port2},..,{brokerN}:{portN}/{db-name}?{params}</tt></small></p>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-info" name="btn-msg" type="submit" value="Create database">Create database</button>
                </div>
            </div>
        </form>
    </div>

    <div id="welcome">
        <div class="row">
            <div class="span8">
                <br />
                <p>This is an e-commerce web application with a built-in ability to simulate activity from thousands of users. You can use it to see how NuoDB shines under a variety of scenarios.</p>

                <p>Get started with a guided tour (see left sidebar). These tours showcase NuoDB’s value propositions in your environment.</p>

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
