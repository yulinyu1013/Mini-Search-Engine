$.fn.WikiWidget = function (wikipediaPage) {
    var wikipediaDivision = this;
    var skipTableRows = 4;
    var maxTableRow = 8;
    // loading gif
    wikipediaDivision.append('<div class="loading"><img src="images/loading.gif"></div>');
    $.getJSON('https://en.wikipedia.org/w/api.php?action=parse&format=json&callback=?', { page: wikipediaPage, prop: 'text|images', uselang: 'en' }, function (parsedata) {
        //remove loading image
        wikipediaDivision.find('.loading').remove();
        var parsing = parsedata.parse;
        if (parsing === undefined) {
            wikipediaDivision.append('<div class="wikiError"> Wikipedia doesn\'t have information for ' + wikipediaPage + '. Please check carefully if this wiki query is correct because it is capital sensitive!</div>\n');
        } else {
            var content = $(parsing.text["*"]).wrap('<div></div>').parent();
            // get the table images
            var tableImages = content.find('table a.image img');
            // get wiki description
            var description = content.find('p').first().text();
            // get the info table from wikipedia
            var table = content.find('table.infobox, table.float-right').first();
            // show the title
            wikipediaDivision.append('<div class="wikiTitle"></div>\n').find('.wikiTitle').html(parsedata.parse.title);
            wikipediaDivision.append('<div class="wikiLink"><a href="https://en.wikipedia.org/wiki/' + wikipediaPage + '">Link</a></div>\n');
            // add the first image to the wikipedia division
            wikipediaDivision.append($(tableImages).first().removeAttr('srcset').removeAttr('height').removeAttr('width').wrap('<div class="wikiLogo">\n</div>\n').parent());
            // add description to the wikipedia division
            wikipediaDivision.append('<div class="wikiDesc"></div>\n').find('.wikiDesc').append(description);
            var newInfoTable = $('<table class="wikiInfoTable"></table>');
            // transfer info from the wikipedia table to my own wikipedia info table
            $.each(table.find('tr'), function (idx, element) {
                if (idx > skipTableRows && idx < (skipTableRows + maxTableRow)) {
                    newInfoTable.append(element);
                }
            })
            wikipediaDivision.append(newInfoTable);
        }
    });
};