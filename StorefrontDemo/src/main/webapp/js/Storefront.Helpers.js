/**
 * This file defines custom formatting "helpers" for use with the Handlebars template system.
 */

Number.prototype.format = function (digits) {
    var n = this,
        c = (digits === undefined) ? 2 : digits,
        d = '.',
        t = ',',
        s = n < 0 ? '-' : '',
        i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + '',
        j = (j = i.length) > 3 ? j % 3 : 0;
    return s + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : '');
}

Handlebars.registerHelper('priceFormat', function(price) {
    return (price.format) ? '$' + price.format(2) : price;
});

Handlebars.registerHelper('dateFormat', function(date) {
    return dateFormat(date, 'dddd, mmmm dS, yyyy "at" h:MM tt');
});

Handlebars.registerHelper('numberFormat', function(number) {
    return (number.format) ? number.format(0) : number;
});

Handlebars.registerHelper('lowerCaseFormat', function(str) {
    return (str || '').toLowerCase();
});
