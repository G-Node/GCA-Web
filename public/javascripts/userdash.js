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

        self.init = function() {
            ko.applyBindings(window.dashboard);
        };

        //Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
        };

        self.ensureDataAndThen = function(doAfter) {
            console.log("ensureDataAndThen::");
            //if (self.conferences() !== null) {
            //    doAfter();
            //    return;
            //}

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
