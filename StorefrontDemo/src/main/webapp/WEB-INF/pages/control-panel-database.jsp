<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <div class="pull-right">
            <a id="btn-console" class="btn" target="_blank">Open in Automation Console</a>
            <a id="btn-explorer" class="btn" target="_blank"><i class="icon icon-search"></i> Open in SQL Explorer</a>
        </div>
    
        <h1>Storefront Database</h1>
        <t:messages />

        <div id="db-info" class="tab-pane"></div>
        
        <h3>Database schema</h3>
        <img src="img/schema.png" width="745" height="759" />
        
        <h3>DDL</h3>
        <textarea id="ddl" class="no-resize-x" readonly="readonly"></textarea>
        
        <script id="tpl-db-info" type="text/template">
            {{#result}}
                <table class="table table-bordered table-hover">
    				<colgroup>
        				<col width="30%" />
        				<col />
        			</colgroup>                
                    <tbody>
    					<tr>
        					<td>Database name:
        					<td>{{dbConnInfo.dbName}}</td>
        				</tr>
    					<tr>
        					<td>Status:</td>
        					<td><span class="label label-{{dbStatusColor}}">{{#if db.active}}{{db.status}}{{else}}INACTIVE{{/if}}</span></td>
        				</tr>
    					<tr>
        					<td>JDBC URL:</td>
        					<td>{{dbConnInfo.url}}</td>
        				</tr>
						<tr>
        					<td>JDBC username:</td>
        					<td>{{dbConnInfo.username}}</td>
        				</tr>
						<tr>
        					<td>JDBC password:</td>
        					<td>{{dbConnInfo.password}}</td>
        				</tr>
    					<tr>
        					<td>API URL:</td>
        					<td><a href="{{apiConnInfo.url}}/databases/{{db.name}}" target="_blank">{{apiConnInfo.url}}</a></td>
        				</tr>
						<tr>
        					<td>API username:</td>
        					<td>{{apiConnInfo.username}}</td>
        				</tr>
        			</tbody>
        		</table>

				{{#if db.template}}
    				<h3>Database template</h3>
    				<p>The Storefront reconfigures the template name and variables based on the number of hosts and regions you select.</p>
                    <table class="table table-bordered table-hover">
        				<colgroup>
            				<col width="30%" />
            				<col />
            			</colgroup>
                        <tbody>        				
        					<tr>
            					<td>Active template:</td>
            					<td>{{db.template.name}}</td>
            				</tr>
            				<tr>
            					<td>Template description:</td>
            					<td>{{db.template.summary}}</td>
            				</tr>
    						<tr>
            					<td>Requirements met:</td>
            					<td>
            						{{#if db.ismet}}<span class="label label-success">Yes</span>{{else}}<span class="label label-important">No</span>{{/if}}
            					</td>
            				</tr>
        					{{#eachInMap db.variables}}
        						<tr>
        							<td>{{key}}:</td>
        							<td>{{value}}</td>
        						</tr>
        					{{/eachInMap}}
        				</tbody>
        			</table>    
				{{/if}}            
            {{/result}}
		</script>
    </div>
</t:page>
