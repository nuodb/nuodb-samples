<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
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
							<td>
								{{address}}:{{port}}
								{{#if appInstances.length}}
									<i class="icon-{{#if currentConnection}}star{{else}}star-empty{{/if}}" title="{{appInstances.length}} Storefront instance(s) connected here{{#if currentConnection}} (including this instance){{/if}}:{{#appInstances}}
- {{this}}{{/appInstances}}"></i>
								{{/if}}
							</td>
							<td>{{pid}}</td>									
							<td><i class="{{icon}}"></i> {{typeName}}</td>
							<td>{{status}}</td>
							<td>{{region}}</td>
							<td><button class="btn btn-danger" {{#unless uid}}title="Feature unavailable without a connection to the AutoConsole API" disabled="disabled"{{/unless}}><i class="icon-off icon-white"></i> Shutdown</button></td>
						</tr>
					{{/result}}
					{{#unless result}}
						<tr><td colspan="4">No processes associated with the Storefront database are currently running.</td></tr>                                
					{{/unless}}
				</tbody>
			</table>
		</script>
    </div>

</t:page>
