/* Copyright (c) 2013-2014 NuoDB, Inc. */

/**
 * @class App.view.NavBar
 */
Ext.define('App.view.NavBar', {
    extend: 'Ext.Component',
    alias: 'widget.navbar',

    border: false,
    id: 'navbar',
    width: 220,
    autoScroll: true,

    links: [{
        title: 'Demo Overview',
        href: '/welcome',
        icon: 'ico-home.png'
    }, {
        title: 'Storefront Website',
        href: '/store-products',
        icon: 'ico-cart.png'
    }, {
        title: 'Guided Tours',
    }, {
        title: 'Scale Out Performance',
        href: '/tour-scale-out',
        prefix: '1.'
    }, {
        title: 'Continuous Availability',
        href: '/tour-continuous-availability',
        prefix: '2.'
    }, {
        title: 'Geo-Distribution',
        href: '/tour-geo-distribution',
        prefix: '3.'
    }, {
        title: 'Multi-Tenant',
        href: '/tour-multi-tenant',
        prefix: '4.'
    }, {
        title: 'No-Knobs Administration',
        href: '/tour-no-knobs-admin',
        prefix: '5.'
    }, {
        title: 'Control Panel'
    }, {
        title: 'Products',
        href: '/control-panel-products',
        icon: 'ico-product.png'
    }, {
        title: 'Simulated Users',
        href: '/control-panel-users',
        icon: 'ico-users.png'
    }, {
        title: 'Database',
        href: '/control-panel-database',
        icon: 'ico-db.png'
    }, {
        title: 'Hosts & Processes',
        href: '/control-panel-processes',
        icon: 'ico-process.png'
    }, {
        title: 'Regions',
        href: '/control-panel-regions',
        icon: 'ico-pin.png'
    }],

    /** @Override */
    initComponent: function() {
        var me = this;

        var html = ['<ul id="nav-links">'];
        for ( var i = 0; i < me.links.length; i++) {
            var link = me.links[i];
            html.push('<li');
            if (!link.href) {
                html.push(' class="section-title"');
            }
            html.push('>');
            if (link.href) {
                html.push('<a href="', link.href, '">');
            }
            if (link.icon) {
                html.push('<img src="img/', link.icon, '" width="16" height="16" />');
            } else if (link.prefix) {
                html.push('<span>', link.prefix, '</span>');
            }
            html.push(link.title);
            if (link.href) {
                html.push('</a>');
            }
            html.push('</li>');
        }
        html.push('</ul>');
        me.html = html.join('');

        me.callParent(arguments);

        App.app.on('viewchange', function(href) {
            $('a', me.el.dom).removeClass('active').filter('[href="' + href + '"]').addClass('active');
        });
    },

    // @Override
    afterRender: function() {
        var me = this;
        me.callParent(arguments);
        $('a', me.el.dom).click(function(e) {
            App.app.fireEvent('viewchange', $(this).attr('href'));
            return false;
        });
    }
});
