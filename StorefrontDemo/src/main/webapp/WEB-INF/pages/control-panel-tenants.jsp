<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <h1>Tenants</h1>
        <t:messages />

        <div id="list" class="tab-pane"></div>
        <script id="tpl-list" type="text/template">
            <table class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>Tenant</th>
						<th>Database</th>
						<th></th>
                    </tr>
                </thead>
                <tbody>
                    {{#result}}
						<tr>
							<td><a href="admin?tenant={{name}}" target="_blank">{{name}}</a></td>
							<td>{{dbConnInfo.dbName}}</td>
							<td><button class="btn btn-danger" {{#if default}}title="Cannot shut down first tenant" disabled="disabled"{{/if}}><i class="icon-off icon-white"></i> Shutdown</button></td>
						</tr>
                    {{/result}}
                </tbody>
            </table>
		</script>
        
        <h3>Start New Tenant</h3>
        <form class="form-horizontal" method="post">
            <div class="control-group">
                <label class="control-label" for="api-username">Tenant name:</label>
                <div class="controls">
                    <input class="input-xxlarge" type="text" id="api-username" name="tenant-name" placeholder="Tenant name">
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-info" type="submit" value="Connect to API">Create tenant</button>
                </div>
            </div>
        </form>
    </div>
</t:page>
