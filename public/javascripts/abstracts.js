// utils
var groupMap = { };

for (var i = 0; i < config.groups.length; i++) {
    group = config.groups[i];
    groupMap[group.id] = group.short;
}

function makeGroupLink(gid) {
    return '#/groups/' + gid;
}

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function(prefix) {
    return this.substring(0, prefix.length) === prefix;
};

function makeDoiURL(doi) {
    return '/doi/' + doi.substring(config.doiPref.length + 1, doi.length);
}

function formatInitials(initials) {
    var res = "";
    var names = initials.split(' ');

    for (var i = 0; i < names.length; i++) {
        var name = names[i].trim();
        if (!name.length) {
            continue;
        }

        res += name.substr(0, 1);
    }

    return res;
}

function AbstractsViewModel() {

    // data
    var self = this;
    self.abstracts = null;
    this.selectedAbstract = ko.observable(null);
    this.abstractList = ko.observable(null);
    this.curWarning = ko.observable(null);
    this.isLoading = ko.observable(false);
    this.errorLevel = ko.observable('warning');

    this.setError = function(level, text) {
        this.curWarning(text);
        this.errorLevel('alert-' + level);
    }

    //functions
    this.selectAbstract = function(abstract) {
        location.hash = makeDoiURL(abstract.doi);
    }

    this.showFigure = function(abstract) {
        $('#modalFigure').modal('show');
    }

    this.showAbstract = function(abstract) {
        self.selectedAbstract(abstract);
        self.abstractList(null);
        document.title = abstract.title;
        MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    };

    this.showAbstractList = function(theList) {
        self.selectedAbstract(null);
        self.abstractList(theList);
        document.title = config.title;
        self.setError(null, null);
    }

    this.showAbstractByDOI = function(doi) {
        if (!(doi in self.doiMap)) {
            self.setError('warning', 'Abstract not found');
            self.selectedAbstract(null);
            self.abstractList(null);
            return;
        }

        abstract = self.doiMap[doi];
        self.showAbstract(abstract);
    }

    this.listAbstractsByGroup = function(group) {
        var filtered = self.abstracts.filter(function (abstract) {
            var gid = (abstract.id & 0xFFFF0000) >> 16;
            return groupMap[gid] === group.substring(0, 1);
        });
        self.showAbstractList(filtered);
    }

    this.searchAbstracts = function(forElement) {
        var needle = $('#searchInput').val().toLowerCase();
        var filtered = self.abstracts.filter(function (abstract) {

            for (var i = 0; i < abstract.authors.length; i++) {
                if (abstract.authors[i].name.toLowerCase().indexOf(needle) != -1) {
                    return true;
                }
            }

            return abstract.title.toLowerCase().indexOf(needle) != -1;
        });
        self.showAbstractList(filtered);

        if (filtered == null || filtered.length == 0) {
            self.setError('warning', 'No results!')
        }

        return false;
    }

    this.processAbstracts = function(abstracts) {

        self.doiMap = {};

        for (var i = 0; i < abstracts.length; i++) {
            this.doiMap[abstracts[i]['doi']] = abstracts[i];
            abstracts[i].identifier = function() {
                var aid = this.id & 0xFFFF;
                var gid = (this.id & 0xFFFF0000) >> 16;
                return groupMap[gid] + '&nbsp;' + aid;
            }

            abstracts[i].formatCitation = function() {
                var nameParser = new NameParse();
                var authorLine = "";
                for (var i = 0; i < this.authors.length; i++) {
                    var author = nameParser.parse(this.authors[i].name);

                    if (i != 0) {
                        authorLine += ', ';
                    }

                    authorLine += author.lastName + ' ' + author.firstName.substring(0, 1) + formatInitials(author.initials);
                }

                var titleStr = this.title.trim();
                var citeLine = authorLine + ' (2013) ' + titleStr;
                citeLine += (titleStr.endsWith('.') || titleStr.endsWith('?') || titleStr.endsWith('!')) ? ' ' : '. ';
                citeLine += "<i>" + config.long + "</i>";
                citeLine += '. doi: <a class=\"doilink\" href=\"' + config.doiLink + makeDoiURL(this.doi) + '\" >'
                citeLine += this.doi + '</a>';
                return citeLine;
            }

            abstracts[i].abstractType = function() {
                var groupNameMap = {
                    0 : 'Invited Talk',
                    1 : 'Contributed Talk',
                    2 : 'Poster',
                    3 : 'Poster',
                    4 : 'Workshop'
                };

                var gid = (this.id & 0xFFFF0000) >> 16;
                return groupNameMap[gid];
            }

            abstracts[i].references = abstracts[i].refs.split('\n');
            abstracts[i].text = abstracts[i].abstract.split('\n\n');
            abstracts[i].hasFigure = 'caption' in abstracts[i] && abstracts[i].caption.length > 0;
            if (abstracts[i].hasFigure) {
                abstracts[i].figure = 'figures/' + abstracts[i].figpath;

                if (! abstracts[i].caption.startsWith('Figure')) {
                    abstracts[i].caption = 'Figure 1: ' + abstracts[i].caption;
                }
            }

            //prev & next support
            abstracts[i].prev = i != 0 ? '#' + makeDoiURL(abstracts[i-1.].doi) : null;
            abstracts[i].next = i+1 != abstracts.length ? '#' + makeDoiURL(abstracts[i+1].doi) : null;

        }
    }

    // data IO
    self.loadData = function(doAfter) {

        if (self.abstracts != null) {
            doAfter();
            return;
        }

        self.isLoading(true);

        var jqxhr = $.getJSON(config.data, function(allData) {
            self.abstracts = allData;
            self.processAbstracts(self.abstracts);
            self.isLoading(false);
            doAfter();

        });

        jqxhr.fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
            self.isLoading(false);
            self.setError('danger', 'Error getting data!');
        });
    }

    // client-side routes
    Sammy(function() {

        this.get('#/doi/:doi', function() {
            var doi = config.doiPref + '/' + this.params['doi'];
            self.loadData(function () {
                self.showAbstractByDOI(doi);
            });
        });

        this.get('#/groups/:group', function() {
            var group = this.params['group'];
            self.loadData(function () {
                self.listAbstractsByGroup(group);
            });
        });

        this.get('', function() {
            self.loadData(function () {
                self.showAbstractList(self.abstracts);
            });
        });

    }).run();
}


absmv = new AbstractsViewModel();
ko.applyBindings(absmv);

