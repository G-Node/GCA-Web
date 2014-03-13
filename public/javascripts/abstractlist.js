require(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";


    /**
     * AbstractList view model.
     *
     *
     * @param confId
     * @returns {AbstractListViewModel}
     * @constructor
     */
    function AbstractListViewModel(confId) {

        if (! (this instanceof AbstractListViewModel)) {
            return new AbstractListViewModel(confId);
        }

        var self = this;
        self.conference = ko.observable();
        self.abstracts = ko.observable(null);

        self.init = function() {
            ko.applyBindings(window.abstractList);
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
                self.getAbstracts(self.conference().abstracts);
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the conference: uuid = " + confId);
            }

        };


        self.getAbstracts = function(location) {
            console.log("Getting abstracts at: " + location)
            $.ajax({
                async: false,
                url: location,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json"
            });

            function success(obj, stat, xhr) {
                console.log("getAbstracts");
                var absList = models.Abstract.fromArray(obj);
                self.abstracts(absList);
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the abstracts at " + location);
            }

        };


        // client-side routes
        Sammy(function() {

            this.get('', function() {
                if (confId) {
                    self.getConference(confId);
                } else {
                    throw "Conference id or abstract id must be defined";
                }
            });

        }).run();

    }


    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid);

        window.abstractList = AbstractListViewModel(data.conferenceUuid);
        window.abstractList.init();
    });

});