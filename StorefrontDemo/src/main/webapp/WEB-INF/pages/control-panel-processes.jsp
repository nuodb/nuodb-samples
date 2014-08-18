<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <h1>Hosts &amp; Processes</h1>
        <t:messages />

		<div id="node-list" class="tab-pane"></div>
		<script id="tpl-node-list" type="text/template">
					<table class="table table-bordered table-hover">
						<thead>
							<tr>
								<th>Region</th>
								<th>Node type</th>
								<th>Address</th>
								<th title="Process ID">PID</th>
								<th>Status</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody>
							{{#result}}
								<tr data-uid="{{uid}}">
									<td>{{region}}</td>
									<td><i class="{{icon}}"></i> {{typeName}}</td>
									<td>{{address}}:{{port}}</td>
									<td>{{pid}}</td>									
									<td>{{status}}</td>
									<td><button class="btn btn-danger" {{#unless uid}}title="Feature unavailable without a connection to the AutoConsole API" disabled="disabled"{{/unless}}><i class="icon-off icon-white"></i> Shutdown</td>
								</tr>
							{{/result}}
							{{#unless result}}
								<tr><td colspan="4">Node information is not available.  You may not be connected to a running NuoDB database.</td></tr>                                
							{{/unless}}
						</tbody>
					</table>
				</script>
         
		<h4>Adding Nodes</h4>       
		<p>To add nodes to your NuoDB cluster, use the NuoDB Console.</p>
		<p id="console-link">If you are running NuoDB locally with default settings, you can find the Console at <a href="http://localhost:8080/console.html" target="_top">http://localhost:8080/console.html</a>.</p>
		<h4>Removing Nodes</h4>
		<p>Try testing fault tolerance by killing any node while the Storefront is running.</p>
    </div>

</t:page>
