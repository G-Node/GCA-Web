require(["main"], function () {
require(["lib/models", "lib/tools", "knockout", "sammy", "lib/offline"], function(models, tools, ko, Sammy, offline) {
    "use strict";

    /**
     * AbstractList view model.
     *
     *
     * @param confId, loggedIn
     * @returns {AbstractListViewModel}
     * @constructor
     */
    function AbstractListViewModel(confId, loggedIn) {
        if (!(this instanceof AbstractListViewModel)) {
            return new AbstractListViewModel(confId, loggedIn);
        }

        var self = this;
        self.abstractsData = null;
        self.isLoading = ko.observable(true);
        self.conference = ko.observable();
        self.abstracts = ko.observableArray(null);
        self.selectedAbstract = ko.observable(null);
        self.isFavouriteAbstract = ko.observable(false);
        self.groups = ko.observableArray(null);
        self.error = ko.observable(false);
        self.messageSuccess = ko.observable(false);
        self.favs = ko.observableArray(null);
        self.favAbsArr = [];

        // maps for uuid -> abstract, doi -> abstract,
        //          neighbours -> prev & next of current list
        self.uuidMap = {};
        self.neighbours = {};

        self.init = function() {
            ko.applyBindings(window.abstractList);
            // start MathJax
            MathJax.Hub.Configured();
        };

        self.localConferenceLink = ko.computed(function() {
            if (self.conference() === null || self.conference() === undefined) {
                return "";
            }
            return "/conference/" + self.conference().short + "/abstracts";
        });

        // Duplicate code with setError. Implement MessageBox instead and remove code from here
        self.setInfo = function(text) {
            self.messageSuccess({message: text});
            self.isLoading(false);
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
            var isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
            if (isMobile) {
                // Mobile figure hotfix, adjusting URL
                for (const fig of abstract.figures) {
                    fig.URL = fig.URL + "mobile";
                }
            }
            self.selectedAbstract(abstract);
            document.title = abstract.title;
            // if user is not logged in
            if (loggedIn.indexOf("true") >= 0) {
                self.isFavourite(abstract);
            }
            // re-render equations
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
        };

        self.favourAbstract = function () {
            var selectedAbstract = self.selectedAbstract();
            if (selectedAbstract !== null && selectedAbstract !== undefined) {
                $.ajax({
                    data: JSON.stringify(selectedAbstract.uuid),
                    async: false,
                    url: "/api/abstracts/" + selectedAbstract.uuid + "/addfavuser",
                    type: "PUT",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    cache: false
                });

                function success(obj) {
                    // Reload the abstract view to refresh the favourite status
                    self.favAbsArr.push(obj);
                    self.showAbstractByUUID(obj);
                    self.setInfo("Abstract has been added to the favourite abstracts list");
                }

                function fail() {
                    self.setError("Error", "Unable to add abstract to the favourite abstracts list");
                }
            }
        };

        self.disfavourAbstract = function () {
            var selectedAbstract = self.selectedAbstract();
            if (selectedAbstract !== null && selectedAbstract !== undefined) {
                $.ajax({
                    data: JSON.stringify(selectedAbstract.uuid),
                    async: false,
                    url: "/api/abstracts/" + selectedAbstract.uuid + "/removefavuser",
                    type: "DELETE",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    cache: false
                });

                function success(obj) {
                    // Reload the abstract view to refresh the favourite status
                    self.favAbsArr.splice(self.favAbsArr.indexOf(obj), 1);
                    self.showAbstractByUUID(obj);
                    self.setInfo("Abstract has been removed from the favourite abstracts list");
                }

                function fail() {
                    self.setError("Error", "Unable to remove abstract from the favourite abstracts list");
                }
            }
        };

        self.showAbstractByUUID = function(uuid) {
            if (!(uuid in self.uuidMap)) {
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
                    // We don't break here because we want to set all the groups 'state' member
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

        // Map related stuff
        self.buildMaps = function() {
            // Empty the map
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

            if (!(uuid in self.neighbours)) {
                return null;
            }

            return self.neighbours[uuid].next;
        };

        self.prevAbstract = function(abstract) {
            var uuid = abstract.uuid;

            if (!(uuid in self.neighbours)) {
                return null;
            }

            return self.neighbours[uuid].prev;
        };

        self.isFavourite = function(abstract) {
            self.isFavouriteAbstract(self.favAbsArr.includes(abstract.uuid, 0));
        };

        self.getFavourites = function() {
            var favUsersUrl = "/api/user/self/conferences/" + self.conference().uuid + "/favabstractuuids";
            $.get(favUsersUrl, onFavouriteData).fail(self.ioFailHandler);

            function onFavouriteData(absList) {
                absList.forEach(function(obj) {
                    self.favAbsArr.push(obj);
                });
                for (var i = 0; i < self.abstractsData.length; i++) {
                    var isFav = self.favAbsArr.includes(self.abstractsData[i].uuid, 0);
                    self.favs.push(isFav);
                }

                if (self.selectedAbstract()) {
                    self.isFavouriteAbstract(self.favAbsArr.includes(self.selectedAbstract().uuid, 0));
                }
            }
        };

        // Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            self.setError("danger", "Error while fetching data from server: <br\\>" + err);
        };

        self.ensureDataAndThen = function(doAfter) {
            self.isLoading(true);
            if (self.abstractsData !== null) {
                doAfter();
                self.isLoading(false);
                return;
            }

            // Now load the data from the server
            var confURL = "/api/conferences/" + confId;
            offline.requestJSON(confId, confURL, onConferenceData, self.ioFailHandler);

            // Conference data
            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
                self.buildGroups();
                // Now load the abstract data
                offline.requestJSON(conf.uuid + "abstracts", conf.abstracts, onAbstractData, self.ioFailHandler);
            }

            // Abstract data
            function onAbstractData(absArray) {
                var absList = models.Abstract.fromArray(absArray);
                self.abstractsData = absList;
                self.buildMaps();
                self.abstracts(absList);
                self.neighbours = self.makeNeighboursMap(absList);
                if (loggedIn.indexOf("true") >= 0) {
                    self.getFavourites(absList);
                }

                doAfter();
                self.isLoading(false);
            }
        };

        // Client-side routes
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

        window.abstractList = AbstractListViewModel(data.conferenceUuid, data.loggedIn);
        window.abstractList.init();
    });
});
});
