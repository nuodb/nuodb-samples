/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.FooterBar
 */
Ext.define('App.view.FooterBar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.footerbar',

    border: false,
    cls: 'nuo-footer',
    enableOverflow: false,
    height: 30,
    
    /** @Override */
    initComponent: function() {
        var me = this;
        
        me.items = [];
        me.items.push('->');
        me.addLabelConfig('Categories', 'lblCategories', 'storefrontStats.all.categoryCount', me.formatNumber);
        me.addLabelConfig('Products', 'lblProducts', 'storefrontStats.all.productCount', me.formatNumber);
        me.addLabelConfig('Reviews', 'lblReviews', 'storefrontStats.all.productReviewCount', me.formatNumber);
        me.addLabelConfig('Customers', 'lblCustomers', 'storefrontStats.all.customerCount', me.formatNumber);
        me.addLabelConfig('Purchases', 'lblPurchases', 'storefrontStats.all.purchaseCount', me.formatNumber);
        //me.addLabelConfig('Purchased value', 'lblPurchasedValue', 'storefrontStats.all.purchaseValue', Ext.util.Format.currency);
        me.addLabelConfig('Uptime', 'lblUptime', 'storefrontStats.all.uptimeMs', me.formatDuration);
        me.items.push('->');
        
        me.callParent(arguments);
        
        me.labels = Ext.ComponentQuery.query('label', me);
        App.app.on('statschange', me.onStatsChange, me);
    },
    
    addLabelConfig: function(label, name, metric, formatter) {
        var me = this;
        if (me.items.length > 1) {
            me.items.push('-');
        }
        me.items.push(label);
        me.items.push({
            xtype: 'label',
            padding: '0 10 2 0',
            itemId: name,
            text: '---',
            metric: metric,
            format: formatter
        });
    },

    onStatsChange: function(stats) {
        var me = this;
        for (var i = 0; i < me.labels.length; i++) {
            var label = me.labels[i];
            label.setText(label.format(stats.getLatestValue(label.metric)));
        }
    },
    
    formatNumber: function(value) {
        return Ext.util.Format.number(value, ',');
    },

    formatDuration: function(ms_num) {
        var sec_numb = parseInt(ms_num / 1000);
        var hours = Math.floor(sec_numb / 3600);
        var minutes = Math.floor((sec_numb - (hours * 3600)) / 60);
        var seconds = sec_numb - (hours * 3600) - (minutes * 60);

        if (minutes < 10) {
            minutes = "0" + minutes;
        }
        if (seconds < 10) {
            seconds = "0" + seconds;
        }
        return hours + ':' + minutes + ':' + seconds;
    }
});
