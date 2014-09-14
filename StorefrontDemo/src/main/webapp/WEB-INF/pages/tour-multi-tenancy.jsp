<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Multi-tenancy</h1>

    <div class="row-fluid tour-figure">
        <div class="span2">
            <h3>You can add other databases...</h3>
            <div class="thumbnail">
                <div class="caption">Databases</div>
                <img src="img/tour-add.png" height="80" />
            </div>
        </div>
        <div class="span1"></div>
        <div class="span4">
            <h3>...yet your Storefront database remains isolated and stable</h3>
            <div class="row-fluid">
                <div class="span6">
                    <div class="thumbnail">
                        <div class="caption text-center">Throughput</div>
                        <img src="img/tour-flat.png" height="100" />
                    </div>
                </div>
                <div class="span6">
                    <div class="thumbnail">
                        <div class="caption text-center">Latency</div>
                        <img src="img/tour-flat.png" height="100" />
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <h3>Try it yourself:</h3>
    <t:messages />
    
    <ol class="tour-steps">
        <li>Create a second database on the same hosts using the <a id="lnk-console" target="_blank">Automation Console</a>.</li>
        <li>Alter the schema and run queries on that database using <a id="lnk-explorer" target="_blank">SQL Explorer</a>.</li>
        <li>Observe that the throughput and latency of the Storefront database remains stable.</li>
    </ol>
    
</t:page>
