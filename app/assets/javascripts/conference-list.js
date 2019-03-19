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

                self.banners = ko.observableArray(null);

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
                            var confBanners = [];
                            if (conf.banner.length > 0) {
                                confBanners.push(conf.banner[0].URL);
                            }
                            self.banners(confBanners);
                        }
                    }
                };

                self.loadConferenceBanners = function() {
                    var confURL = "/api/conferences";
                    $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

                    function onConferenceData(confObj) {
                        var confs = models.Conference.fromArray(confObj);
                        if (confs !== null) {
                            var confBanners = [];
                            confs.forEach(function (current) {
                                if (current.isActive) {
                                    if (current.banner.length > 0) {
                                        confBanners.push(current.banner[0].URL);
                                    } else {
                                        confBanners.push("");
                                    }
                                }
                            });
                            self.banners(confBanners);
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

