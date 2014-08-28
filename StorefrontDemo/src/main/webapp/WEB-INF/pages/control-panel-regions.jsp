<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <h1>Regions</h1>
        <t:messages />

        <div id="list" class="tab-pane"></div>
        <script id="tpl-list" type="text/template">
                    <table class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>Region</th>
                                <th>Hosts in use</th>
                                <th>Transaction engines</th>
                                <th>Storage managers</th>
                            </tr>
                        </thead>
                        <tbody>
                            {{#result}}
                                <tr>
                                    <td>{{region}}</td>
                                    <td>{{usedHostCount}} of {{hostCount}}</td>
                                    <td>{{transactionManagerCount}}</td>
                                    <td>{{storageManagerCount}}</td>                                    
                                </tr>
                            {{/result}}
                        </tbody>
                    </table>
		</script>
    </div>
</t:page>
