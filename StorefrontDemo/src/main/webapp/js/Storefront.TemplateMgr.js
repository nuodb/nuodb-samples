/* Copyright (c) 2013-2015 NuoDB, Inc. */

/**
 * This file provides a lightweight wrapper of the Handlebars templating system, leveraging jQuery.
 * It ensures each client-side template is compiled no more than once.
 */

Storefront.TemplateMgr = {
    templates: {},

    getTemplate: function(name) {
        var me = this;
        template = me.templates[name];
        if (template === undefined) {
            var template = $('#' + name);
            if (!template.length) {
                template = null;
            } else {
                template = Handlebars.compile(template.html());
            }
        }
        me.templates[name] = template;
        return template;
    },
    
    hasTemplate: function(name) {
        return !!this.getTemplate(name);
    },
    
    applyTemplateStr: function(name, data) {
        var template = this.getTemplate(name);
        if (data && !data.result) {
            data = {
                result: data
            };
        }
        return template(data);
    },

    applyTemplate: function(name, selector, data, append) {
        var html = this.applyTemplateStr(name, data);        
        var el = $(selector);
        if (append) {
            el.append(html);
        } else {
            el.html(html);
        }
        
        // Initialize star ratings just inserted dynamically
        $('.rateit', el).rateit();        
    },
    
    autoFillTemplate: function(name, url, qsData, callback, append) {
        var me = this;
        var templateName = 'tpl-' + name;
        var el = $('#' + name);

        if (!me.hasTemplate(templateName)) {
            return null;
        }
        
        if (!append) {
            el.html('<div class="loading"></div>');
        }

        return $.ajax(url, {
            cache: false,
            data: qsData,
            dataType: 'json',
            traditional: true
        }).done(function(responseData) {
            me.applyTemplate(templateName, el, responseData, append);

            if (callback) {
                callback(responseData);
            }
        });
    }    
};
