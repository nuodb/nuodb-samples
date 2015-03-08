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
						<th class="text-center">Storefront Instances</th>
                    </tr>
                </thead>
                <tbody>
                    {{#result}}
                        <tr>
                            <td>
								{{#if instances}}
									{{#if multiInstance}}
										<div class="dropdown">
											<a data-toggle="dropdown" href="#" title="Shows Storefronts running in this region">{{region}} <b class="caret"></b></a>
											<ul class="dropdown-menu" role="menu"  aria-labelledby="dropdownMenu">
												{{#instances}}											
													<li><a href="{{url}}" target="_blank" title="Opens Storefront running in this region in a new tab">{{url}}</a></li>									
												{{/instances}}
											</ul>
										</div>
									{{else}}
										{{#instances}}
											<a href="{{url}}" target="_blank" title="Opens Storefront running in this region in a new tab">{{region}}</a>
										{{/instances}}
									{{/if}}
								{{else}}
									{{region}}
								{{/if}}
							</td>
                            <td class="text-center">{{progressBar usedHostCount hostCount}} &nbsp; {{usedHostCount}} of {{hostCount}}</td>
                            <td class="text-center">{{transactionManagerCount}}</td>
                            <td class="text-center">{{storageManagerCount}}</td>
							<td class="text-center">{{instances.length}}</td>                                    
                        </tr>
                    {{/result}}
                </tbody>
            </table>
		</script>
    </div>
</t:page>
