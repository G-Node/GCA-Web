require(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";

    /**
     * Editor view model.
     *
     *
     * @param confId
     * @param abstrId
     * @returns {EditorViewModel}
     * @constructor
     */
    function EditorViewModel(confId, abstrId) {


        if (! (this instanceof EditorViewModel)) {
            return new EditorViewModel(confId, abstrId);
        }

        var self = this;

        self.abstractSaved = ko.observable(false);
        self.abstract = ko.observable();
        self.conference = ko.observable();


        self.init = function() {

            if (confId) {
                self.getConference(confId);
                self.abstract(models.Abstract());
            } else if (abstrId) {
                self.getAbstract(abstrId);
                self.abstractSaved(true);
            } else {
                throw "Conference id or abstract id must be defined";
            }

            ko.applyBindings(window.editor);
        };


        self.getConference = function(confId) {

            $.ajax({
                async: false,
                url: "/api/conferences/" + confId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json"
            });

            function success(obj, stat, xhr) {
                self.conference(models.Conference.fromObject(obj));
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the conference: uuid = " + confId);
            }

        };

        self.getAbstract = function(abstrId) {

            $.ajax({
                async: false,
                url: "/api/abstracts/" + confId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json"
            });

            function success(obj, stat, xhr) {
                self.conference(models.Abstract.fromObject(obj));
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the abstract: uuid = " + abstrId);
            }

        };

        self.refresh = function() {
            var abstr = self.abstract();

            if (self.abstractSaved()) {
                console.log("save abstract");
            }

            console.log("refresh abstract");
            self.abstract(abstr);
        };
    }

    // start the editor
    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid);
        console.log(data.abstractUuid);

        window.editor = EditorViewModel(data.conferenceUuid, data.abstractUuid);
        window.editor.init();
    });

});
