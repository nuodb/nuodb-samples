<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <h1>Products</h1>
        <t:messages />
        

		<div id="product-info" class="tab-pane"></div>
		<script id="tpl-product-info" type="text/template">
				{{#result}}
					{{#if hasData}}
						<form method="post" id="product-info">
							<p>There are currently {{numberFormat productCount}} products across {{numberFormat categoryCount}} categories.</p>
							<p><button class="btn btn-danger" name="btn-msg" type="submit" value="Remove All Data">Remove All Data</button>
						</form>
					{{else}}
						<p>There are no products in the database.</p>
					{{/if}}
				{{/result}}
		</script>
    </div>
</t:page>
