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
    function UserDashViewModel() {

        if (! (this instanceof UserDashViewModel)) {
            return new UserDashViewModel();
        }

        var self = this;

        self.conferences = ko.observableArray(null);
        self.isLoading = ko.observable(true);
        self.error = ko.observable(false);


        self.setError = function(level, text) {
            self.error({message: text, level: 'alert-' + level});
            self.isLoading(false);
        };


        self.init = function() {
            ko.applyBindings(window.dashboard);
        };

        self.selectAbstractForConference = function(abstract, conference) {
            console.log("Select abtract: " + abstract.uuid + " for conference: + " + conference.uuid);
        };


        //Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
            self.setError("danger", "Error while lading data [" + err + "]!");
        };

        self.ensureDataAndThen = function(doAfter) {
            console.log("ensureDataAndThen::");

            //now load the data from the server
            var confURL = "/api/user/self/conferences";
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            //conference data
            function onConferenceData(confObj) {
                console.log("+ onConferenceData")
                var confs = models.Conference.fromArray(confObj);
                if (confs !== null) {
                    confs.forEach(function(current){
                        current.abstracts = ko.observableArray(null);
                    });

                }

                self.conferences(confs);

                if (confs !== null) {
                    confs.forEach(function(current){
                        var absUrl = "/api/user/self/conferences/" + current.uuid + "/abstracts";
                        $.getJSON(absUrl, onAbstractData(current)).fail(self.ioFailHandler);
                    });
                }

                doAfter();
            }

            function onAbstractData(currentConf) {
                return function (abstractList) {
                    var abs = models.Abstract.fromArray(abstractList);
                    currentConf.abstracts(abs);
                }
            }
        };


        // client-side routes
        Sammy(function() {

            this.get('#/', function() {
                console.log('Sammy::get::');
                self.ensureDataAndThen(function () {
                    self.isLoading(false);
                });
            });

        }).run('#/');

    }

    $(document).ready(function() {

        var data = tools.hiddenData();


        window.dashboard = UserDashViewModel();
        window.dashboard.init();
    });

});
