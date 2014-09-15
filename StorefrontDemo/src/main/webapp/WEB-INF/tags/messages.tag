<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@tag description="Messages" pageEncoding="UTF-8"%>

<!-- Status messages -->
<div class="row">
    <div class="span12" id="messages"></div>
    <script id="tpl-messages" type="text/template">
        {{#if result}}
            {{#result}}
				{{#if buttons}}
					<form method="POST">
				{{/if}}

                <div class="alert alert-block alert-{{lowerCaseFormat severity}}">
					{{#unless buttons}}
	       	        <button type="button" class="close" data-dismiss="alert">&times;</button>
					{{/unless}}
    	            <p>{{message}}</p>

					{{#if buttons}}
						<p>
							{{#buttons}}
								<button class="btn btn-{{lowerCaseFormat ../severity}}" name="btn-msg" type="submit" value="{{this}}">{{this}}</button>
							{{/buttons}}
						</p>
					{{/if}}

					{{#if links}}
						<p>
							{{#links}}
								<a class="btn btn-{{lowerCaseFormat ../severity}}" href="{{href}}" target="_blank">{{text}}</a>
							{{/links}}
						</p>
					{{/if}}
                </div>

				{{#if buttons}}
					</form>
				{{/if}}
            {{/result}}
        {{/if}}
	</script>
</div>
