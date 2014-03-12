define(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";

    function EditorViewModel(confId, abstrId) {


        if (! (this instanceof EditorViewModel)) {
            return new EditorViewModel(confId, abstrId);
        }

        var self = this;

        self.abstractSaved = ko.observable(false);
        self.abstract = ko.observable();
        self.conference = ko.observable();

        self.run = function() {
            console.log(confId);
            console.log(abstrId);

            if (confId) {
                self.getConference(confId);
                self.abstract(models.Abstract());
            } else if (abstrId) {
                self.getAbstract(abstrId);
                self.abstractSaved(true);
            } else {
                throw "Conference id or abstract id must be defined";
            }

            ko.applyBindings(self);
        };

        self.getConference = function(confId) {

            $.ajax({
                async: false,
                url: "/conferences/" + confId,
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
                url: "/abstracts/" + confId,
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

    return EditorViewModel;
});
