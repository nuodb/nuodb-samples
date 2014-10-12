<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <h1>No-Knobs Administration</h1>

    <p class="tour-text">When a database is created, a Template is applied, which specifies the level of redundancy, number of host and regions required for that database.</p>

    <p class="tour-text">NuoDB monitors the database to ensure that its Template requirements are always being met and takes actions if not.</p>

    <p class="tour-text">For instance, if a host fails, NuoDB will start replacement processes on another host if necessary.</p>
    <p>&nbsp;</p>


    <h3>Try it yourself:</h3>
    <t:messages />

    <ol class="tour-steps">
        <li>Open the <a class="lnk-console" target="_blank">Automation Console</a>.</li>
        <li>Click on &ldquo;Databases&rdquo; (on the left menu bar) and then click on &ldquo;Storefront.&rdquo; Explore the Regions and Processes for the Storefront database. Note the Template in effect. Click on &ldquo;Metrics&rdquo; on the upper right to view various operational statistics for the database.</li>
    </ol>

    <p>Or follow these steps to create a new database:</p>

    <ol class="tour-steps">
        <li>Open the <a class="lnk-console" target="_blank">Automation Console</a>.</li>
        <li>Click on &ldquo;Databases&rdquo; (on the left menu bar)</li>
        <li>Click on &ldquo;Actions&rdquo; (top-right) and then &ldquo;Add Database&rdquo;.</li>
        <li>Enter a name for the new database, the DBA user name and password, and select a template specifying the SLA for this database.</li>
        <li>Select your new database to view its processes and metrics.</li>
        <li>Change the Template in effect with the Actions &gt; Edit option.</li>
    </ol>

    <h3>To learn more:</h3>
    <ul class="tour-links">
        <li>See <a href="http://doc.nuodb.com/display/21V/Templates+and+Automation" target="_blank">NuoDB documentation</a> to learn more about templates</li>
        <li>Storefront also leverages NuoDB&rsquo;s template system to automatically manage NuoDB processes across hosts.<br />For details, see the <a href="control-panel-processes">Hosts &amp; Processes</a> and <a href="control-panel-database">Database</a> sections in the Storefront Control Panel.
        </li>
    </ul>

</t:page>
