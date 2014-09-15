<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Welcome to the NuoDB Storefront Demo!</h1>
    <t:messages />

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
