require(["main"], function () {
    require(["lib/models", "lib/tools", "knockout", "ko.sortable"],
        function(models, tools, ko) {
            "use strict";

            /**
             * Conference list view model.
             *
             * @returns {ConferenceListViewModel}
             * @constructor
             */
            function ConferenceListViewModel() {
                if (!(this instanceof ConferenceListViewModel)) {
                    return new ConferenceListViewModel();
                }

                var self = tools.inherit(this);

                self.logos = ko.observableArray([]);
                self.thumbnails = ko.observableArray([]);

                self.otherConfShorts = ko.observable(null);

                self.init = function() {
                    if ($("#conference-uuid").length) {
                        self.loadSingleConferenceBanner($("#conference-uuid").text());
                    } else {
                        self.loadConferenceBanners();
                    }

                    ko.applyBindings(window.dashboard);
                };

                self.loadSingleConferenceBanner = function(id) {
                    var confURL = "/api/conferences/" + id;
                    $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

                    function onConferenceData(confObj) {
                        var conf = models.Conference.fromObject(confObj);
                        if (conf !== null) {
                            for (var i = 0; i < conf.banner.length; i++) {
                                if (conf.banner[i].bType === "logo") {
                                    self.logos.push(conf.banner[i]);
                                }
                            }
                        }
                    }
                };

                self.loadConferenceBanners = function() {
                    var confURL = "/api/conferences";
                    $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

                    function onConferenceData(confObj) {
                        var confs = models.Conference.fromArray(confObj);
                        if (confs !== null) {
                            confs.forEach(function (conf) {
                                var banFound = false;
                                if (conf.isActive) {
                                    for (var i = 0; i < conf.banner.length; i++) {
                                        if (conf.banner[i].bType === "logo") {
                                            self.logos.push(conf.banner[i]);
                                            banFound = true;
                                        }
                                    }
                                    if (banFound === false) {
                                        self.logos.push("");
                                    }
                                } else if (conf.isPublished) {
                                    for (i = 0; i < conf.banner.length; i++) {
                                        if (conf.banner[i].bType === "thumbnail") {
                                            self.thumbnails.push(conf.banner[i]);
                                            banFound = true;
                                        }
                                    }
                                    if (banFound === false) {
                                        self.thumbnails.push("");
                                    }
                                }
                            });
                        }
                    }
                };
            }

            $(document).ready(function() {
                window.dashboard = ConferenceListViewModel();
                window.dashboard.init();
            });
        });
});

