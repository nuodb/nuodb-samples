<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>Continuous Availability</h1>

    <div class="row-fluid tour-figure tall">
        <div class="span4">
            <h3><br />When one or more processes<br />go down...</h3>
            <div class="row-fluid">
                <div class="span6 offset3">
                    <div class="thumbnail">
                        <div class="caption text-right">Processes</div>
                        <img src="img/tour-remove.png" height="80" />
                    </div>
                </div>
            </div>
        </div>
        <div class="span1"></div>
        <div class="span4">
            <h3>...NuoDB automatically restarts processes as required, and shopping continues uninterrupted</h3>
            <div class="row-fluid">
                <div class="span6 offset3">
                    <div class="thumbnail">
                        <div class="caption text-center">Throughput</div>
                        <img src="img/tour-sawtooth.png" height="100" />
                    </div>
                </div>
            </div>
        </div>
    </div>

    <h3>Try it yourself:</h3>
    <t:messages />

    <ol class="tour-steps">
        <li>Shut down one or more Transaction Engine (TE) processes using the <a href="control-panel-processes">hosts &amp; processes page</a>. Alternatively, you can connect to one of your hosts (via SSH, Remote Desktop, etc.) and terminate a TE process directly..
        </li>
        <li>Observe how NuoDB detects the loss and automatically starts new TEs as necessary to maintain the workload.</li>
    </ol>

    <h3>To learn more:</h3>
    <ul class="tour-links">
        <li>See <a href="http://doc.nuodb.com/display/21V/Start+and+Stop+NuoDB+Services" target="_blank">NuoDB documentation</a> to learn how to increase the number of available hosts
        </li>
        <li>See <a href="control-panel-processes">Hosts &amp; Processes</a> in the Storefront Control Panel
        </li>
    </ul>

</t:page>
