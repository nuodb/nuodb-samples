<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@tag description="RegionMenu" pageEncoding="UTF-8"%>

<script id="tpl-region-menu" type="text/template">
{{#result}}
	<a data-toggle="dropdown" href="#"><img alt="Logo" id="logo" src="img/shop-logo.png" /><span>
	{{#regions}}
		{{#if selected}}{{storeName}}{{/if}}
	{{/regions}}
	<b class="caret"></b></span></a>
	<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">
    	{{#regions}}
        	<li>
				<a tabindex="-1" href="#" data-region="{{regionName}}">
					{{#if selected}}<b>&bull;</b>{{/if}}
					{{storeName}}
				</a>
			</li>
    	{{/regions}}
  		<li class="divider"></li>
		<li><a tabindex="-1" href="#">Add a region...</a></li>
	</ul>
{{/result}}
</script>
