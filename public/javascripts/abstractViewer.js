require.config({
    baseUrl: '/assets/javascripts/',
    paths: {
        'moment': ['//cdnjs.cloudflare.com/ajax/libs/moment.js/2.5.1/moment.min']
    },
    noGlobal: true
});

require(["lib/models", "lib/tools", "lib/msg", "moment"], function(models, tools, msg, moment) {
    "use strict";

    function AbstractViewerViewModel(confId, abstrId, isAdmin, isOwner) {

        if (!(this instanceof AbstractViewerViewModel)) {
            return new AbstractViewerViewModel(confId, abstrId, isAdmin, isOwner);
        }

        var self = tools.inherit(this, msg.MessageVM);

        self.selectedAbstract = ko.observable(null);
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
            $.getJSON(absURL, onAbstractData).fail(self.ioFailHandler);

            //conference data
            function onAbstractData(abstrObj) {
                var abstr = models.Abstract.fromObject(abstrObj);
                self.selectedAbstract(abstr);

                if(isAdmin === "true" || isOwner === "true") {
                    var logUrl = "/api/abstracts/" + abstrId + "/stateLog";
                    $.getJSON(logUrl, onLogData).fail(self.ioFailHandler);

                } else {
                    self.isLoading(false);
                }
            }

            function onLogData(logData) {

                logData.forEach(function (elm){
                    elm.formattedDate = moment(elm.timestamp).format('MMMM Do YYYY, HH:mm');
                });

                self.stateLog(logData);
                self.isLoading(false);
            }
        }
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