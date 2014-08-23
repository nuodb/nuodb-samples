<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <h1>Hosts &amp; Processes</h1>
        <t:messages />

		<div id="list" class="tab-pane"></div>
		<script id="tpl-list" type="text/template">
					<table class="table table-bordered table-hover">
						<thead>
							<tr>
								<th>Host</th>
								<th title="Process ID">PID</th>
								<th>Node type</th>
								<th>Status</th>
								<th>Region</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody>
							{{#result}}
								<tr data-uid="{{uid}}">
									<td>{{address}}:{{port}}</td>
									<td>{{pid}}</td>									
									<td><i class="{{icon}}"></i> {{typeName}}</td>
									<td>{{status}}</td>
									<td>{{region}}</td>
									<td><button class="btn btn-danger" {{#unless uid}}title="Feature unavailable without a connection to the AutoConsole API" disabled="disabled"{{/unless}}><i class="icon-off icon-white"></i> Shutdown</td>
								</tr>
							{{/result}}
							{{#unless result}}
								<tr><td colspan="4">Node information is not available.  You may not be connected to a running NuoDB database.</td></tr>                                
							{{/unless}}
						</tbody>
					</table>
		</script>

    </div>

</t:page>
