require(["lib/models", "lib/tools", "lib/astate"], function(models, tools, astate) {
    "use strict";


    /**
     * admin/abstracts view model.
     *
     *
     * @param confId
     * @returns {OwnersListViewModel}
     * @constructor
     */
    function adminAbstractsViewModel(confId) {

        if (! (this instanceof adminAbstractsViewModel)) {
            return new adminAbstractsViewModel(confId);
        }

        var self = this;

        self.message = ko.observable(null);
        self.abstractsData = null;
        self.conference = ko.observable(null);
        self.abstracts = ko.observableArray(null);

        //state related things
        self.stateHelper = new astate.StateChangeHelper();
        self.selectedAbstract = ko.observable(null);

        self.init = function() {
            self.ensureData();
            ko.applyBindings(window.dashboard);
        };

        self.isLoading = function(status) {
            if (status === false) {
                self.message(null);
            } else {
                self.message({message: "Loading data!", level: "alert-info", desc: "Please wait..."});
            }
        };

        self.setError = function(level, text, description) {
            if (text === null) {
                self.message(null);
            } else {
                self.message({message: text, level: 'alert-' + level, desc: description});
            }
        };


        //helper functions
        self.makeAbstractLink = function(abstract) {
            return "/myabstracts/" + abstract.uuid + "/edit";
        };

        self.setState = function(abstract, state, note) {
            var oldState = abstract.state();

            var data = {state: state};
            if (note) {
                data.note = note;
            }

            abstract.state("Saving...");
            $.ajax("/api/abstracts/" + abstract.uuid + '/state', {
                data: JSON.stringify(data),
                type: "PUT",
                contentType: "application/json",
                success: function(result) {
                    abstract.state(state);
                },
                error: function(jqxhr, textStatus, error) {
                    abstract.state(oldState);
                    self.setError("danger", "Error while updating the state", error);
                }
            });
        };

        self.beginStateChange = function(abstract) {
            $('#note').val(""); //reset the message box
            $('#state-dialog').modal('show');
            self.selectedAbstract(abstract);
        };

        self.finishStateChange = function() {
            var note = $('#note').val();
            var state = $('#state-dialog').find('select').val();

            self.setState(self.selectedAbstract(), state, note);
            self.selectedAbstract();
        };

        self.mkAuthorList = function(abstract) {

            if(abstract.authors.length < 1) {
                return "";
            }

            var text = abstract.authors[0].lastName;

            if (abstract.authors.length > 1) {
                text += " et. al.";
            }

            return text;
        };

        //Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
            self.setError("danger", "Error while fetching data from server", error);
        };

        self.ensureData = function() {
            console.log("ensureDataAndThen::");
            self.isLoading(true);
            if (self.abstractsData !== null) {
                self.isLoading(false);
                return;
            }

            //now load the data from the server
            var confURL ="/api/conferences/" + confId;
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            //conference data
            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
                //now load the abstract data
                $.getJSON(confURL + "/allAbstracts", onAbstractData).fail(self.ioFailHandler);
            }

            //abstract data
            function onAbstractData(absArray) {
                var absList = models.Abstract.fromArray(absArray);


                absList.forEach(function (abstr) {

                    abstr.makeObservable(['state']);
                    abstr.possibleStates = ko.computed(function () {
                        var fromState = abstr.state();

                        if (fromState === 'Saving...') {
                            return [];
                        }

                        return self.stateHelper.getPossibleStatesFor(fromState, true, self.conference().isOpen);
                    });

                    abstr.viewEditCtx = ko.computed(function () {
                        //only time that admins can make changes is in the InRevision state
                        var canEdit = abstr.state() == "InRevision";

                        return {
                            link: canEdit ? "/myabstracts/" + abstr.uuid + "/edit" : "/abstracts/" + abstr.uuid,
                            label: canEdit ? "Edit" : "View",
                            btn: canEdit ? "btn-danger" : "btn-primary"
                        };

                    });
                });

                self.abstractsData = absList;
                self.abstracts(absList);

                self.isLoading(false);
            }
        };

    }

    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid);

        window.dashboard = adminAbstractsViewModel(data.conferenceUuid);
        window.dashboard.init();
    });


});