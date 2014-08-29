<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Geo-distribution</h1>
    
    <div class="row-fluid tour-figure">
        <div class="span2">
            <h3>When you add regions...</h3>
            <div class="thumbnail">
                <div class="caption">Regions</div>
                <img src="img/tour-add.png" height="80" />
            </div>
        </div>
        <div class="span2">
            <h3>and add users to each region...</h3>
            <div class="thumbnail">
                <div class="caption">Users</div>
                <img src="img/tour-add.png" height="100" />
            </div>        
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3>...load is distributed geographically</h3>
            <div class="thumbnail">
                <div class="caption">Throughput</div>
                <img src="img/tour-step-up.png" height="100" />
            </div>        
        </div>
    </div> 
    
    <h3>Try it yourself:</h3>
    <t:messages />
    
    <ol class="tour-steps">
        <li>Increase regions by clicking the up arrow within the &ldquo;Regions&rdquo; block in the header above. Each click activates an additional region.  You are limited only by the number of pre-configured regions.</li>
        <li>Add users in the new regions using the <a href="control-panel-users">simulated users page</a> or by clicking the up arrow within the &ldquo;Users&rdquo; block above.</li>
    </ol>
        
</t:page>
