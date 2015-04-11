/* Copyright (c) 2013-2015 NuoDB, Inc. */

/**
 * This file defines custom formatting "helpers" for use with the Handlebars template system, as well as other prototype and global methods.
 */

Number.prototype.format = function(digits) {
    var n = this, c = (digits === undefined) ? 2 : digits, d = '.', t = ',', s = n < 0 ? '-' : '', i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + '', j = (j = i.length) > 3 ? j % 3 : 0;
    return s + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : '');
};

function pluralize(val, label, pluralLabel) {
    return val.format(0) + ' ' + ((val == 1) ? label : (pluralLabel || (label + 's')));
}

function compare(a, b) {
    return a < b ? -1 : a == b ? 0 : 1;
}

$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

Handlebars.registerHelper({
    addOne: function(value) {
        return value + 1;
    },
    
    currencyFormat: function(currency) {
        switch (currency) {
            case 'BRITISH_POUND':
                return 'British pound (£)';

            case 'EURO':
                return 'Euro (€)';

            case 'US_DOLLAR':
                return 'U.S. dollar ($)';

            case 'MIXED':
                return 'Mixed';

            default:
                return 'Unknown';
        }
    },

    dateFormat: function(date) {
        return dateFormat(date, 'dddd, mmmm dS, yyyy "at" h:MM tt');
    },
    
    eachInMap: function(map, block) {
        var out = [];
        var keys = [];
        for ( var key in map) {
            keys.push(key);
        }
        keys.sort();
        for ( var i = 0; i < keys.length; i++) {
            key = keys[i];
            out.push(block.fn({
                key: key,
                value: map[key]
            }));
        }
        return out.join('');
    },
    
    lowerCaseFormat: function(str) {
        return (str || '').toLowerCase();
    },

    msFormat: function(number) {
        return ((number || number === 0) && number.format) ? (number / 1000).format(1) + ' sec' : number;
    },
    
    numberFormat: function(number) {
        return ((number || number === 0) && number.format) ? number.format(0) : number;
    },

    numberOrZero: function(number) {
        return ((number || number === 0) && number.format) ? number : '0';
    },

    priceFormat: function(price) {
        if (typeof (price) != 'number') {
            return price;
        }

        var symbol;
        switch (Storefront.currency) {
            case 'BRITISH_POUND':
                symbol = '£';
                price /= 1.5;
                break;

            case 'EURO':
                symbol = '€';
                price /= 1.1;
                break;

            default:
                symbol = '$';
                break;
        }

        return symbol + price.format(2);
    },

    productImage: function(value) {
        return value || 'img/product.png';
    },
    
    progressBar: function(val1, val2) {
        var pct = val1 / val2 * 100;
        return new Handlebars.SafeString('<div class="progress progress-inline"><div class="bar" style="width:' + pct + '%"></div></div>');
    },

    sqrtMsFormat: function(number) {
        return ((number || number === 0) && number.format) ? (Math.sqrt(number) / 1000).format(1) + ' sec' : number;
    },

    urlEncode: function(value) {
        return encodeURIComponent(value);
    }
});
