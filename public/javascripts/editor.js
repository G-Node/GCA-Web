define(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";

    function EditorViewModel(confId, abstrId) {

        if (tools.isGlobalOrUndefined(this) || this === undefined) {
            return new EditorViewModel(confId, abstrId);
        }

        var self = this;

        self.abstract = ko.observable();
        self.conference = ko.observable();

        self.run = function() {
            $(document).ready(function() {
                console.log(confId);
                console.log(abstrId);

                if (confId) {
                    self.getConference(confId);
                }

                if (abstrId) {

                }
            });
        };

        self.getConference = function(confId) {

            $.ajax({
                async: true,
                url: "/conferences/" + confId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json"
            });

            function success(obj, stat, xhr) {
                self.conference = models.Conference.fromObject(obj);
                var foo = "foo";
                console.log(foo);
                self.abstract = foo;
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the conference: uuid = " + confId);
            }

        };
    }

    return EditorViewModel;
});
