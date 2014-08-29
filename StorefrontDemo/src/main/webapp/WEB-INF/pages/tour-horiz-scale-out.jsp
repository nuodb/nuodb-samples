<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Horizontal Scale-out</h1>
    
    <div class="row-fluid tour-figure">
        <div class="span2">
            <h3>When users increase...</h3>
            <div class="thumbnail">
                <div class="caption">Users</div>
                <img src="img/tour-up.png" height="80" />
            </div>
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3>...you scale out by adding hosts</h3>
            <div class="thumbnail">
                <div class="caption">Hosts</div>
                <img src="img/tour-up.png" height="100" />
            </div>        
        </div>
        <div class="span1"></div>
        <div class="span2">
            <h3>Throughput increases</h3>
            <div class="thumbnail">
                <div class="caption">Throughput</div>
                <img src="img/tour-up.png" height="100" />
            </div>        
        </div>
        <div class="span2">
            <h3>...but latency remains steady!</h3>
            <div class="thumbnail">
                <div class="caption">Latency</div>
                <img src="img/tour-flat.png" height="100" />
            </div>        
        </div>
    </div>

    <h3>Try it yourself:</h3>
    <t:messages />

    <ol class="tour-steps">
        <li>Increase users by clicking the up arrow within the &ldquo;Users&rdquo; block in the header above. Each click adds 50 users (10 per workload).</li>
        <li>Increase hosts by moving up the slider within the &ldquo;Hosts&rdquo; block in the header above. This starts a transaction engine (TE) on an available host in each region. You are limited only by the number of available hosts. For adding new hosts, see the docs.</li>
        <li>Observe the impact on throughput and latency</li>
    </ol>

</t:page>
