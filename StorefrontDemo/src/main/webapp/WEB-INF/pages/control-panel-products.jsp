<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="control-panel">
        <form method="post" class="btn-group pull-right">
            <a id="btn-refresh" class="btn" href="control-panel-products"><i class="icon icon-refresh"></i> Refresh</a>
            <button id="btn-delete" class="btn btn-danger" name="btn-msg" type="submit" value="Remove All Data">Remove All Data</button>
        </form>

        <h1>Product Report</h1>
        <t:messages />

        <div id="product-info" class="tab-pane"></div>
        <script id="tpl-product-info" type="text/template">
			{{#result}}
				<table class="table table-bordered table-hover">
					<colgroup>
						<col width="30%" />
						<col />
					</colgroup>
					<tr>
						<td class="text-right">{{numberFormat productCount}}</td>
						<td>Products in the store</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat categoryCount}}
						<td>Product categories</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat productReviewCount}}
						<td>Products reviews</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat customerCount}}
						<td>Unique customers</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat activeCustomerCount}}
						<td>Active customers</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat cartItemCount}}
						<td>Products in shopping carts</td>
					</tr>
					<tr>
						<td class="text-right">{{priceFormat cartValue}}
						<td>Shopping cart value</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat purchaseCount}}
						<td>Purchases</td>
					</tr>
					<tr>
						<td class="text-right">{{numberFormat purchaseItemCount}}
						<td>Purchased items</td>
					</tr>
					<tr>
						<td class="text-right">{{priceFormat purchaseValue}}
						<td>Purchased value</td>
					</tr>
				</table>
			{{/result}}
		</script>
    </div>
</t:page>
