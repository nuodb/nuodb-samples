<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Scale-Out Performance</h1>
    
    <div class="row-fluid tour-figure tall">
        <div class="span2">
            <h3>When user workload increases...</h3>
            <div class="thumbnail">
                <div class="caption">Users</div>
                <img src="img/tour-up.png" height="80" />
            </div>
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3><br />...scale out by adding hosts</h3>
            <div class="thumbnail">
                <div class="caption">Hosts</div>
                <img src="img/tour-up.png" height="100" />
            </div>        
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3><br />Throughput increases</h3>
            <div class="thumbnail">
                <div class="caption">Throughput</div>
                <img src="img/tour-up.png" height="100" />
            </div>        
        </div>
        <div class="span2">
            <h3><br />...and latency decreases</h3>
            <div class="thumbnail">
                <div class="caption text-right">Latency</div>
                <img src="img/tour-down.png" height="100" />
            </div>        
        </div>
    </div>

    <h3>Try it yourself:</h3>
    <t:messages />

    <ol class="tour-steps">
        <li>Increase user workload by clicking the up arrow within &ldquo;Users&rdquo; above. Each click adds 50 users (10 for each kind of workload).</li>
        <li>Increase the number of Transaction Engines (TEs) by moving up the slider within &ldquo;Hosts&rdquo; above. TEs are started on available hosts in each region. The maximum number of TEs is limited only by the number of available hosts.</li>
        <li>Observe the positive impact on throughput and latency</li>
    </ol>
    
    <h3>To learn more:</h3>
    <ul class="tour-links">
        <li>See <a href="http://doc.nuodb.com/display/21V/Start+and+Stop+NuoDB+Services" target="_blank">NuoDB documentation</a> to learn how to increase the number of available hosts</li>
        <li>See <a href="control-panel-processes">Hosts &amp; Processes</a> in the Storefront Control Panel</li>
    </ul>

</t:page>
