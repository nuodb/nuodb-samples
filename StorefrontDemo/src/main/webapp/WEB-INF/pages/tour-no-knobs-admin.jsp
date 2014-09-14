<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>No-Knobs Administration</h1>
    
    <div class="row-fluid tour-figure">
        <div class="span2">
            <h3>When network partitions arise</h3>
            <div class="thumbnail">
                <img src="img/tour-disconnect.png" height="80" />
            </div>
        </div>
        <div class="span2">
            <h3>or other problems occur...</h3>
            <div class="thumbnail">
                <img src="img/tour-caution.png" height="80" />
            </div>
        </div>        
        <div class="span1"></div>
        <div class="span4">
            <h3>...the database recovers automatically based on preconfigured templates</h3>
            <div class="row-fluid">
                <div class="span6">
                    <div class="thumbnail">
                        <div class="caption">Throughput</div>
                        <img src="img/tour-sawtooth.png" height="100" />
                    </div>
                </div>
                <div class="span6">
                    <div class="thumbnail">
                        <div class="caption">Latency</div>
                        <img src="img/tour-sawtooth.png" height="100" />
                    </div>
                </div>
            </div>
        </div>
    </div>

    <h3>Try it yourself:</h3>
    <t:messages />
    
    <ol class="tour-steps">
        <li>Open the <a id="lnk-console" target="_blank">Automation Console</a>.</li>
        <li>Create a database using one of NuoDB&rsquo;s managed templates.</li>
        <li>Obsere how NuoDB ensures the template&rsquo;s conditions are met automatically.</li>
    </ol>

    <p>The Storefront also leverages NuoDB&rsquo;s template system to automatically manage NuoDB processes across hosts.<br />For details, see the <a href="control-panel-database">database page</a>.</p>
    
</t:page>
