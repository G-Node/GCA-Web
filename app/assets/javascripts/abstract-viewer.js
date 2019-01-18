require(["main"], function () {
require(["lib/models", "lib/tools", "lib/msg", "lib/astate", "knockout", "lib/offline"], function(models, tools, msg, astate, ko, offline) {
    "use strict";

    function AbstractViewerViewModel(confId, abstrId, isAdmin, isOwner) {
        if (!(this instanceof AbstractViewerViewModel)) {
            return new AbstractViewerViewModel(confId, abstrId, isAdmin, isOwner);
        }

        var self = tools.inherit(this, msg.MessageBox);

        self.selectedAbstract = ko.observable(null);
        self.conference = ko.observable(null);
        self.stateLog = ko.observable(null);

        self.init = function() {
            self.loadAbstract();
            ko.applyBindings(window.viewer);
            // start MathJax
            MathJax.Hub.Configured();
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            self.setError("Error", "Unable to load the abstract with uuid = " + abstrId);
        };

        self.loadAbstract = function() {
            self.isLoading(true);
            var absURL = "/api/abstracts/" + abstrId;
            offline.requestJSON(abstrId, absURL, onAbstractData, self.ioFailHandler);

            // Conference data
            function onAbstractData(abstrObj) {
                var abstr = models.Abstract.fromObject(abstrObj);
                self.selectedAbstract(abstr);
                // Re-render equations
                MathJax.Hub.Queue(["Typeset", MathJax.Hub]);

                self.loadConference();

                if (isAdmin === "true" || isOwner === "true") {
                    var logUrl = "/api/abstracts/" + abstrId + "/stateLog";
                    $.getJSON(logUrl, onLogData).fail(self.ioFailHandler);
                } else {
                    self.isLoading(false);
                }
            }

            function onLogData(logData) {
                astate.logHelper.formatDate(logData);
                self.stateLog(logData);
                self.isLoading(false);
            }
        };

        self.loadConference = function() {
            // We should be reading this from the abstract
            var confUrl = "/api/conferences/" + confId;
            offline.requestJSON(confId, confUrl, onConferenceData, self.ioFailHandler);

            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
            }
        };
    }

    // Start the editor
    $(document).ready(function() {
        var data = tools.hiddenData();
        window.viewer = AbstractViewerViewModel(data["conferenceUuid"], data["abstractUuid"],
                                                data["isAdmin"], data["isOwner"]);
        window.viewer.init();
    });
});
});
