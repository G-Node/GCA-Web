require(["main"], function () {
require(["lib/models", "lib/tools", "knockout", "sammy"], function (models, tools, ko, Sammy) {
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

        if (!(this instanceof UserDashViewModel)) {
            return new UserDashViewModel();
        }

        var self = this;

        self.conferences = ko.observableArray(null);
        self.isLoading = ko.observable(true);
        self.error = ko.observable(false);


        self.setError = function (level, text) {
            self.error({message: text, level: 'alert-' + level});
            self.isLoading(false);
        };

        self.setInfo = function (level, text) {
            self.error({message: text, level: 'callout-' + level});
            self.isLoading(false);
        };


        self.init = function () {
            ko.applyBindings(window.dashboard);
        };

        self.makeAbstractLink = function (abstract, conference) {
            return "/myabstracts/" + abstract.uuid + "/edit";
        };


        //Data IO
        self.ioFailHandler = function (jqxhr, textStatus, error) {
            var err = textStatus + ", " + error + ", " + jqxhr.responseText;
            console.log("Request Failed: " + err);
            self.setError("danger", "Error while loading data [" + err + "]!");
        };

        self.ensureDataAndThen = function (doAfter) {
            console.log("ensureDataAndThen::");

            //now load the data from the server
            var confURL = "/api/user/self/conferences";
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            //conference data
            function onConferenceData(confObj) {
                console.log("+ onConferenceData");
                if((typeof confObj === 'string' || confObj instanceof String) && confObj.length == 0){
                    self.setInfo("info","You have no own abstracts created yet. Create one via accessing a conference page.");
                    return;
                }
                var confs = models.Conference.fromArray(confObj);
                if (confs !== null) {
                    confs.forEach(function (current) {
                        current.abstracts = ko.observableArray(null);
                    });

                }

                self.conferences(confs);

                if (confs !== null) {
                    confs.forEach(function (current) {
                        var absUrl = "/api/user/self/conferences/" + current.uuid + "/abstracts";
                        $.getJSON(absUrl, onAbstractData(current)).fail(self.ioFailHandler);
                    });
                }

                doAfter();
            }

            function onAbstractData(currentConf) {
                return function (abstractList) {
                    var absList = models.Abstract.fromArray(abstractList);

                    absList.forEach(function (abstr) {

                        abstr.viewEditCtx = ko.computed(function () {
                            var confIsOpen = currentConf.isOpen;
                            var canEdit = abstr.state == "InRevision" ||
                                (confIsOpen && (abstr.state == "InPreparation" ||
                                abstr.state == "Submitted" ||
                                abstr.state == "Withdrawn"));

                            return {
                                link: canEdit ? "/myabstracts/" + abstr.uuid + "/edit" : "/abstracts/" + abstr.uuid,
                                label: canEdit ? "Edit" : "View",
                                btn: canEdit ? "btn-danger" : "btn-primary"
                            };
                        });
                    });

                    currentConf.abstracts(absList);
                }
            }
        };


        // client-side routes
        Sammy(function () {

            this.get('#/', function () {
                console.log('Sammy::get::');
                self.ensureDataAndThen(function () {
                    self.isLoading(false);
                });
            });

        }).run('#/');

    }

    $(document).ready(function () {

        var data = tools.hiddenData();


        window.dashboard = UserDashViewModel();
        window.dashboard.init();
    });

});
});
