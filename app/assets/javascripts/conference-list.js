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

                self.logos = ko.observableArray(null);

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
                        var confLogos = [];
                        if (conf !== null) {
                            if (conf.logo.length > 0) {
                                confLogos.push(conf.logo[0].URL);
                            }
                            self.logos(confLogos);
                        }
                    }
                };

                self.loadConferenceBanners = function() {
                    var confURL = "/api/conferences";
                    $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

                    function onConferenceData(confObj) {
                        var confs = models.Conference.fromArray(confObj);
                        var confLogos = [];
                        if (confs !== null) {
                            confs.forEach(function (current) {
                                if (current.isActive) {
                                    if (current.logo.length > 0) {
                                        confLogos.push(current.logo[0].URL);
                                    } else {
                                        confLogos.push("");
                                    }
                                }
                            });
                            self.logos(confLogos);
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

