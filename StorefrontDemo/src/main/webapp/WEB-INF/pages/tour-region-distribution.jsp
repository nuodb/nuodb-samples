<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Region Distribution</h1>
    
    <div class="row-fluid tour-figure tall">
        <div class="span2">
            <h3><br />When you add regions...</h3>
            <div class="thumbnail">
                <div class="caption">Regions</div>
                <img src="img/tour-add.png" height="80" />
            </div>
        </div>
        <div class="span2">
            <h3><br />and add users to each region...</h3>
            <div class="thumbnail">
                <div class="caption">Users</div>
                <img src="img/tour-add.png" height="100" />
            </div>        
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3>...load is distributed regionally</h3>
            <div class="thumbnail">
                <div class="caption">Throughput</div>
                <img src="img/tour-step-up.png" height="100" />
            </div>        
        </div>
    </div> 
    
    <h3>Try it yourself:</h3>
    <t:messages />
    
    <ol class="tour-steps">
        <li>Verify at least one Storefront is running on a host in each region.  You must explicitly visit the URL of a Storefront in each region to start them.  Use the App Menu of the Automation Console on a host in each region, and choose the Storefront app.  This way, the simulated user load is originating from multiple regions and targeted to specific regions.  If you proceed without additional Storefronts running, you'll see a warning message in the header above telling you which region(s) are not covered.</li>  
        <li>Verify you&rsquo;ve configured all participating hosts with the setting <code>balancer=RegionBalancer</code> in NuoDB&rsquo;s <code>default.properties</code> file.  Without this setting, Storefront instances are not confined to communicating with specific regions, which will skew the metrics you see.</li>
        <li>Increase the number of regions by clicking the up arrow within &ldquo;Regions&rdquo; above. Each click activates an additional region.  You are limited only by the number of pre-configured regions.</li>
        <li>Add users in the new regions using the <a href="control-panel-users${qs}">Simulated Users page</a> or by clicking the up arrow within &ldquo;Users&rdquo; above.</li>
    </ol>
    
    <h3>To learn more:</h3>
    <ul class="tour-links">
        <li>See <a href="http://doc.nuodb.com/display/21V/Start+and+Stop+NuoDB+Services" target="_blank">NuoDB documentation</a> to learn how to increase the number of available hosts</li>
        <li>See <a href="control-panel-regions${qs}">Regions</a> in the Storefront Control Panel</li>
    </ul>    
        
</t:page>
