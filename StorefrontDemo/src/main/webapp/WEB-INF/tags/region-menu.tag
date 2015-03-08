<%-- Copyright (c) 2013-2015 NuoDB, Inc. --%>
<%@tag description="RegionMenu" pageEncoding="UTF-8"%>

<script id="tpl-region-menu" type="text/template">
{{#result}}
	<a href="./store-products"><img alt="Logo" id="logo" src="img/shop-logo.png" />
		{{#regions}}
			{{#if selected}}{{storeName}}{{/if}}
		{{/regions}}
	</a>
{{/result}}
</script>
