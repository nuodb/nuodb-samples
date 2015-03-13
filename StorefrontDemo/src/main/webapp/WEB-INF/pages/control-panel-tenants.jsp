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
                    </tr>
                </thead>
                <tbody>
                    {{#result}}
                        <tr>
                            <td>{{dbName}}</td>
                        </tr>
                    {{/result}}
                </tbody>
            </table>
		</script>
    </div>
</t:page>
