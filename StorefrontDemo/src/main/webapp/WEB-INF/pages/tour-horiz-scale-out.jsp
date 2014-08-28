<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Horizontal Scale-out</h1>
    <t:messages />
    
    <div class="row-fluid">
        <div class="span2">
            <div class="thumbnail">
                <div class="caption">When users increase...</div>
                <img src="" height="100" />
            </div>
        </div>
        <div class="span1"></div>
        <div class="span2">
            <div class="thumbnail">
                <div class="caption">...you scale out by adding hosts</div>
                <img src="" height="100" />
            </div>        
        </div>
        <div class="span1"></div>
        <div class="span2">
            <div class="thumbnail">
                <div class="caption">Throughput increases,</div>
                <img src="" height="100" />
            </div>        
        </div>
        <div class="span2">
            <div class="thumbnail">
                <div class="caption">...but latency remains steady!</div>
                <img src="" height="100" />
            </div>        
        </div>
    </div>

    <h2>Try it yourself:</h2>
    <ol>
        <li>Increase users using the header above. Each click increases users all types of users by 10 or 10% (whichever is larger).</li>
        <li>Increase hosts using the header above. This starts a transaction engine (TE) on an available host in each region. You are limited only by the number of available hosts. For adding new hosts, see the docs.</li>
        <li>Observe the impact on throughput and latency</li>
    </ol>

</t:page>
