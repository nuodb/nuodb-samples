/* Copyright (c) 2013 NuoDB, Inc. */

/**
 * @class App.view.MessageBar
 */
Ext.define('App.view.MessageBar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.messagebar',

    border: false,
    cls: 'message',
    hidden: true,
    lastUpdateMs: 0,

    minLateUpdateDeviation: 0.2,
    maxLateUpdateAgeMs: 10 * 1000,
    minLateUpdatesForWarning: 3,

    /** @Override */
    initComponent: function() {
        var me = this;

        me.lateUpdates = [];

        me.items = ['->', {
            xtype: 'tbtext',
            itemId: 'lblMessage',
            frame: true,
            width: 840,
            text: ''
        }, {
            xtype: 'container',
            width: 100,
            layout: {
                type: 'vbox',
                align: 'right'
            },
            items: {
                xtype: 'button',
                text: '',
                itemId: 'btnAction',
                handler: me.onButtonClick,
                scope: me
            }
        }, '->'];

        me.callParent(arguments);
        me.lblMessage = me.down('[itemId=lblMessage]');
        me.btnAction = me.down('[itemId=btnAction]');

        App.app.on('statsfail', me.onStatsFail, me);
        App.app.on('statschange', me.onStatsChange, me);

    },

    onStatsFail: function(response) {
        var me = this;
        if (response.status == 0) {
            me.setMessage('<b>Unable to connect to the Storefront API</b>.  Verify the web application is still running.  Retries will continue automatically.');
        } else {
            var msg = '';
            try {
                msg = Ext.decode(response.responseText).message;
            } catch (e) {                
            }
            msg = msg || (' HTTP status ' + response.status);
            me.setMessage(Ext.String.format('<b>The Storefront has a problem:</b> &nbsp;{0}.  Retries will continue automatically.', msg));
        }
        me.lastUpdateMs = -1;
    },

    onStatsChange: function() {
        var me = this;
        var updateMs = new Date().getTime();

        if (me.lastUpdateMs <= 0) {
            me.clearMessage();
        } else {
            // Remove expired late times from history
            var maxLateUpdateAge = updateMs - me.maxLateUpdateAgeMs;
            while (me.lateUpdates.length > 0 && me.lateUpdates[0] < maxLateUpdateAge) {
                me.lateUpdates.shift();
            }

            // Add new time to history if it's late
            if (updateMs > me.lastUpdateMs + App.app.refreshFrequencyMs * (1 + me.minLateUpdateDeviation)) {
                me.lateUpdates.push(updateMs);
            }

            // Show/dismiss warning as appropriate
            if (me.lateUpdates.length == 0) {
                me.clearMessage();
            } else if (me.lateUpdates.length >= me.minLateUpdatesForWarning) {
                me.setMessage('<b>The Storefront appears to be under very heavy load</b>.  Consider reducing simulated users or adding nodes to the NuoDB cluster.', 'Stop All');
            }
        }

        me.lastUpdateMs = updateMs;
    },

    clearMessage: function() {
        var me = this;
        me.setMessage(null);
        me.btnAction.setDisabled(false);
    },

    setMessage: function(message, buttonText) {
        me = this;
        me.lblMessage.setText(message);
        me.btnAction.setText(buttonText);
        me.btnAction.setVisible(!!buttonText);
        me.setVisible(!!message);
    },

    onButtonClick: function(btn) {
        var me = this;
        btn.setDisabled(true);
        Ext.getStore('Workloads').each(function(workload) {
            Ext.Ajax.request({
                url: Ext.String.format('{0}/api/simulator/workloads/{1}/workers', App.app.apiBaseUrl, workload.get('name')),
                method: 'PUT',
                params: {
                    minWorkers: 0,
                    limit: 0
                },
                scope: this
            });
        });
        Ext.ComponentQuery.query('viewport')[0].refreshView('welcome');
    }
});
