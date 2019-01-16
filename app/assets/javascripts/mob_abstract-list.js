require(["main"], function () {
require(["lib/models", "lib/tools", "knockout", "sammy"], function(models, tools, ko, Sammy) {
    "use strict";

    /**
     * AbstractList view model.
     *
     *
     * @param confId
     * @returns {AbstractListViewModel}
     * @constructor
     */
    function AbstractListViewModel(confId) {
        if (!(this instanceof AbstractListViewModel)) {
            return new AbstractListViewModel(confId);
        }

        var self = this;
        self.abstractsData = null;
        self.isLoading = ko.observable(true);
        self.conference = ko.observable();
        self.abstracts = ko.observableArray(null);
        self.selectedAbstract = ko.observable(null);
        self.groups = ko.observableArray(null);
        self.error = ko.observable(false);

        // maps for uuid -> abstract, doi -> abstract,
        //          neighbours -> prev & next of current list
        self.uuidMap = {};
        self.neighbours = {};

        self.init = function() {
            ko.applyBindings(window.abstractList);
            // start MathJax
            MathJax.Hub.Configured();
        };

        self.setError = function(level, text) {
            self.error({message: text, level: "alert-" + level});
            self.isLoading(false);
        };

        self.makeLink = function(abstract) {
            return "#/uuid/" + abstract.uuid;
        };

        self.getGroupById = function(groupid) {
            var foundGroup = null;
            for (var i = 0; i < self.groups().length; i++) {
                var curGroup = self.groups()[i];
                if (curGroup.prefix === groupid) {
                    foundGroup = curGroup;
                    break;
                }
            }

            return foundGroup;
        };

        self.makeAbstractID = function(abstract) {
            var identifier = abstract.sortId;

            if (identifier === 0) {
                return "";
            }

            var aid =  identifier & 0xFFFF;
            var gid = (identifier & 0xFFFF0000) >> 16;

            var prefix = "U ";
            var g = self.getGroupById(gid);
            if (g !== null) {
                prefix = g.short;
            }

            return prefix + "&nbsp;" + aid;
        };

        self.selectAbstract = function(abstract) {
            window.location = self.makeLink(abstract);
        };

        self.showAbstract = function(abstract) {
            self.abstracts(null);
            self.selectedAbstract(abstract);
            document.title = abstract.title;
            // re-render equations
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
        };

        self.showAbstractByUUID = function(uuid) {
            if (!uuid in self.uuidMap) {
                return;
            }

            self.showAbstract(self.uuidMap[uuid]);
        };

        self.activateGroup = function(groupId) {
        };

        self.showAbstractsByGroup = function(groupId) {
            var selGroup = null;
            for (var i = 0; i < self.groups().length; i++) {
                var curGroup = self.groups()[i];
                if (curGroup.short === groupId) {
                    selGroup = curGroup;
                    // we don't break here because we want to set all the groups 'state' member
                    curGroup.state("active");
                } else {
                    curGroup.state("");
                }
            }

            if (selGroup === null) {
                self.setError("danger", "Internal error [group selection]!");
                self.showAbstractList([]);
                return;
            }

            if (selGroup.short === "A") {
                // "A" means all, no filtering needed
                self.showAbstractList(self.abstractsData);
                return;
            }

            var filtered = self.abstractsData.filter(function (abstract) {
                var gid = (abstract.sortId & 0xFFFF0000) >> 16;
                return selGroup.prefix === gid;
            });

            self.showAbstractList(filtered);
        };

        self.showAbstractList = function(theList) {
            self.selectedAbstract(null);
            self.abstracts(theList);
            document.title = self.conference().name;
            self.neighbours = self.makeNeighboursMap(theList);
        };

        self.buildGroups = function() {
            function mkGroup(_prefix, _name, _short) {
                return {
                    prefix: _prefix,
                    name: _name,
                    short: _short,
                    link: "#/groups/" + _short,
                    state: ko.observable("")
                };
            }

            var theGroups = [mkGroup(~0, "All", "A")];
            theGroups[0].state("active");
            var confGroups = self.conference().groups;
            for (var i = 0; i < confGroups.length; i++) {
                var g = confGroups[i];
                theGroups.push(mkGroup(g.prefix, g.name, g.short));
            }

          self.groups(theGroups);
        };

        // map related stuff
        self.buildMaps = function() {
            // Empty map
            self.uuidMap = {};
            for (var i = 0; i < self.abstractsData.length; i++) {
                var currentAbstract = self.abstractsData[i];
                self.uuidMap[currentAbstract.uuid] = currentAbstract;
            }
        };

        self.makeNeighboursMap = function(objs) {
            var theMap = { };

            if (objs === null) {
                return theMap;
            }

            for (var i = 0; i < objs.length; i++) {
                theMap[objs[i].uuid] = {
                    prev: i > 0 ? self.makeLink(objs[i - 1]) : null,
                    next: i + 1 != objs.length ? self.makeLink(objs[i + 1]) : null
                };
            }

            return theMap;
        };

        self.nextAbstract = function(abstract) {
          var uuid = abstract.uuid;

            if (!uuid in self.neighbours) {
                return null;
            }

            return self.neighbours[uuid].next;
        };

        self.prevAbstract = function(abstract) {
            var uuid = abstract.uuid;

            if (!uuid in self.neighbours) {
                return null;
            }

            return self.neighbours[uuid].prev;
        };

        // Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            self.setError("danger", "Error while fetching data from server: <br\\>" + error);
        };

        self.ensureDataAndThen = function(doAfter) {
            self.isLoading(true);
            if (self.abstractsData !== null) {
                doAfter();
                self.isLoading(false);
                return;
            }
            if (localStorage.getItem(confId) !== null) {
                onConferenceData(JSON.parse(localStorage.getItem(confId)));
                // return;
            }

            var confURL = "/api/conferences/" + confId;
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            // Conference data
            function onConferenceData(confObj) {
                localStorage.setItem(confId, JSON.stringify(confObj));
                var x = localStorage.getItem(confId);
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
                self.buildGroups();
                // Load abstract data
                var abs = localStorage.getItem(confId + "_abs");
                if (abs !==  null) {
                    onAbstractData(JSON.parse(abs));
                    return;
                }
                $.getJSON(conf.abstracts, onAbstractData).fail(self.ioFailHandler);
            }

            // Abstract data
            function onAbstractData(absArray) {
                localStorage.setItem(confId + "_abs", JSON.stringify(absArray));
                var absList = models.Abstract.fromArray(absArray);
                self.abstractsData = absList;
                self.buildMaps();
                self.abstracts(absList);
                self.neighbours = self.makeNeighboursMap(absList);

                doAfter();
                self.isLoading(false);
            }
        };

        // client-side routes
        Sammy(function() {
            this.get("#/uuid/:uuid", function() {
                var uuid = this.params["uuid"];
                self.ensureDataAndThen(function () {
                    self.showAbstractByUUID(uuid);
                });
            });

            this.get("#/groups/:group", function() {
                var group = this.params["group"];
                self.ensureDataAndThen(function () {
                    self.showAbstractsByGroup(group);
                });
            });

            this.get("#/", function() {
                self.ensureDataAndThen(function () {
                    self.showAbstractList(self.abstractsData);
                });
            });
        }).run("#/");
    }

    $(document).ready(function() {
        var data = tools.hiddenData();
        window.abstractList = AbstractListViewModel(data.conferenceUuid);
        window.abstractList.init();
    });
});
});
