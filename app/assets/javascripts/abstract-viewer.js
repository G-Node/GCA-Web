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
            MathJax.Hub.Configured(); //start MathJax
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            self.setError("Error", "Unable to load the abstract with uuid = " + abstrId);
        };

        self.loadAbstract = function() {

            self.isLoading(true);
            var absURL ="/api/abstracts/" + abstrId;
            offline.requestJSON(abstrId, absURL, onAbstractData, self.ioFailHandler);

            //conference data
            function onAbstractData(abstrObj) {
                var abstr = models.Abstract.fromObject(abstrObj);
                self.selectedAbstract(abstr);
                MathJax.Hub.Queue(["Typeset",MathJax.Hub]); //re-render equations

                self.loadConference();

                if(isAdmin === "true" || isOwner === "true") {
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
            var confUrl = "/api/conferences/" + confId; // we should be reading this from the abstract
            offline.requestJSON(confId, confUrl, onConferenceData, self.ioFailHandler);

            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
            }

        };
    }

    // start the editor
    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data["conferenceUuid"]);
        console.log(data["abstractUuid"]);
        console.log(data["isAdmin"]);
        console.log(data["isOwner"]);

        window.viewer = AbstractViewerViewModel(data["conferenceUuid"], data["abstractUuid"],
                                                data["isAdmin"], data["isOwner"]);
        window.viewer.init();
    });

});
});