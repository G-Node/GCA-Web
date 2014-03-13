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
        self.abstract = ko.observable(null);
        self.conference = ko.observable(null);


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
                url: "/api/abstracts/" + abstrId,
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


        self.saveAbstract = function() {

            if (self.abstractSaved()) {

                $.ajax({
                    async: false,
                    url: "/api/abstracts/" + self.abstract().uuid,
                    type: "PUT",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: self.abstract().toJSON()
                });

            } else if (confId) {

                $.ajax({
                    async: false,
                    url: "/api/conferences/" + confId + "/abstracts",
                    type: "POST",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: self.abstract().toJSON()
                });

            } else {
                throw "Conference id or abstract id must be defined";
            }

            function success(obj, stat, xhr) {
                self.abstract(models.Abstract.fromObject(obj));
                self.abstractSaved(true);
            }

            function fail(xhr, stat, msg) {
                console.log("Error while saving the abstract");
            }
        };


        self.refresh = function() {

            if (self.abstractSaved()) {
                console.log("save abstract");
                self.saveAbstract();
            } else {
                console.log("refresh abstract");
                self.abstract(self.abstract());
            }
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
