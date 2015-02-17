<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
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
                        <th class="text-center">Hosts in use</th>
                        <th class="text-center">Transaction Engines</th>
                        <th class="text-center">Storage Managers</th>
                    </tr>
                </thead>
                <tbody>
                    {{#result}}
                        <tr>
                            <td>{{region}}</td>
                            <td class="text-center">{{progressBar usedHostCount hostCount}} &nbsp; {{usedHostCount}} of {{hostCount}}</td>
                            <td class="text-center">{{transactionManagerCount}}</td>
                            <td class="text-center">{{storageManagerCount}}</td>                                    
                        </tr>
                    {{/result}}
                </tbody>
            </table>
		</script>
    </div>
</t:page>
