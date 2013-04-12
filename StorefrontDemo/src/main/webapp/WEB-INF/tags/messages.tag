<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@tag description="Messages" pageEncoding="UTF-8"%>

<!-- Status messages -->
<div class="row">
    <div class="span12" id="messages"></div>
    <script id="tpl-messages" type="text/template">
        {{#if result}}
            <div id="messages"></div>
                {{#result}}
                    <div class="alert alert-block alert-{{lowerCaseFormat severity}}">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                {{message}}
                    </div>
                {{/result}}
            </div>
        {{/if}}
	</script>
</div>
