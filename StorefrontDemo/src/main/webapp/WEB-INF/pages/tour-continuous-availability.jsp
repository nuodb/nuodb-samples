<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Continuous Availability</h1>
    
    <div class="row-fluid tour-figure tall">
        <div class="span2">
            <h3>When one or more processes go down...</h3>
            <div class="thumbnail">
                <div class="caption text-right">Processes</div>
                <img src="img/tour-remove.png" height="80" />
            </div>
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3>...users are not impacted</h3>
            <div class="thumbnail">
                <div class="caption text-center">Users</div>
                <img src="img/tour-flat.png" height="100" />
            </div>        
        </div>
        <div class="span2">
            <h3>...and shopping continues uninterrupted</h3>
            <div class="thumbnail">
                <div class="caption text-center">Throughput</div>
                <img src="img/tour-sawtooth.png" height="100" />
            </div>        
        </div>
    </div>
    
    <h3>Try it yourself:</h3>
    <t:messages />

    <ol class="tour-steps">
        <li>Choose one or more processes to shut down using the <a href="control-panel-processes">hosts &amp; processes page</a>.
            Alternatively, you can connect to one of your hosts (via SSH, Remote Desktop, etc.) and kill a NuoDB process directly.</li>
        <li>Observe how throughput recovers to its steady state.</li>
    </ol>    
    
</t:page>
