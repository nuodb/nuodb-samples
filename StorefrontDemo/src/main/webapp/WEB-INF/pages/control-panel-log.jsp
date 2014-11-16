<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <form method="post" class="pull-right">
            <button class="btn" type="submit" name="refresh" value="1">Refresh</button>
            <button class="btn" type="submit" name="clear" value="1">Clear</button>
            <a class="btn" href="?download=1"><i class="icon icon-download"></i> Download</a>
        </form>

        <h1>Storefront Log</h1>

        <t:messages />

        <pre class="log">${log}</pre>
    </div>
</t:page>
