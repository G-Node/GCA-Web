require(["main"], function () {
    require(["lib/models", "lib/tools", "knockout", "sammy"], function (models, tools, ko, Sammy) {
        "use strict";


        /**
         * FavouriteAbstractList view model.
         *
         *
         * @param confId
         * @returns {AbstractListViewModel}
         * @constructor
         */
        function FavouriteAbstractsViewModel() {

            if (!(this instanceof FavouriteAbstractsViewModel)) {
                return new FavouriteAbstractsViewModel();
            }

            var self = this;

            self.conferences = ko.observableArray(null);
            self.isLoading = ko.observable(true);
            self.noFavouriteAbstracts = ko.observable(false);
            self.error = ko.observable(false);


            self.setError = function (level, text) {
                self.error({message: text, level: 'alert-' + level});
                self.isLoading(false);
            };


            self.init = function () {
                ko.applyBindings(window.dashboard);
            };

            self.makeAbstractLink = function (abstract, conference) {
                return "/myabstracts/" + abstract.uuid + "/edit";
            };


            //Data IO
            self.ioFailHandler = function (jqxhr, textStatus, error) {
                if(find(jqxhr.responseText,"No favourite abstracts") != -1){
                    self.noFavouriteAbstracts(true);
                    self.isLoading(false);
                }else {
                    var err = textStatus + ", " + error + ", " + jqxhr.responseText;
                    console.log("Request Failed: " + err);
                    self.setError("danger", "Error while loading data [" + err + "]!");
                }
            };

            self.ensureDataAndThen = function (doAfter) {
                console.log("ensureDataAndThen::");

                //now load the data from the server
                var confURL = "/api/user/self/conffavouriteabstracts";
                $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

                //conference data
                function onConferenceData(confObj) {
                    console.log("+ onConferenceData")
                    var confs = models.Conference.fromArray(confObj);
                    if (confs !== null) {
                        confs.forEach(function (current) {
                            current.abstracts = ko.observableArray(null);
                            console.log(current.short);
                        });

                    }

                    self.conferences(confs);

                    if (confs !== null) {
                        confs.forEach(function (current) {
                            //needs some fix to include "conferences/" + current.uuid
                            var absUrl = "/api/user/self/conferences/" + current.uuid + "/favouriteabstracts";
                            self.currConfShort = current.short;
                            $.getJSON(absUrl, onAbstractData(current)).fail(self.ioFailHandler);
                        });
                    }

                    doAfter();
                }

                function onAbstractData(currentConf) {
                    return function (abstractList) {
                        var absList = models.Abstract.fromArray(abstractList);

                        absList.forEach(function (abstr) {
                            abstr.createLink = ko.computed(function () {
                                return {
                                    absLink: "/conference/" + self.currConfShort + "/abstracts#/uuid/" + abstr.uuid
                                };
                            });
                            /*abstr.viewEditCtx = ko.computed(function () {
                                var confIsOpen = currentConf.isOpen;
                                var canEdit = abstr.state == "InRevision" ||
                                    (confIsOpen && (abstr.state == "InPreparation" ||
                                        abstr.state == "Submitted" ||
                                        abstr.state == "Withdrawn"));

                                return {
                                    link: canEdit ? "/myabstracts/" + abstr.uuid + "/edit" : "/abstracts/" + abstr.uuid,
                                    label: canEdit ? "Edit" : "View",
                                    btn: canEdit ? "btn-danger" : "btn-primary"
                                };
                            });*/
                        });

                        currentConf.abstracts(absList);
                    }
                }
            };


            // client-side routes
            Sammy(function () {

                this.get('#/', function () {
                    console.log('Sammy::get::');
                    self.ensureDataAndThen(function () {
                        self.isLoading(false);
                    });
                });

            }).run('#/');

        }

        $(document).ready(function () {

            var data = tools.hiddenData();


            window.dashboard = FavouriteAbstractsViewModel();
            window.dashboard.init();
        });

    });
});
