require(["main"], function () {
require(["lib/models", "lib/tools", "lib/astate", "knockout"], function(models, tools, astate, ko) {
    "use strict";

    /**
     * admin/abstracts view model.
     *
     *
     * @param confId
     * @returns {adminAbstractsViewModel}
     * @constructor
     */
    function adminAbstractsViewModel(confId) {
        if (!(this instanceof adminAbstractsViewModel)) {
            return new adminAbstractsViewModel(confId);
        }

        var self = this;

        self.message = ko.observable(null);
        self.abstractsData = null;
        self.conference = ko.observable(null);
        self.abstracts = ko.observableArray(null);
        self.abstractNumbers = ko.observable({ total: 0, active: 0 });

        self.note = ko.observable(null);

        // State related things
        self.stateHelper = astate.changeHelper;
        self.selectedAbstract = ko.observable(null);
        self.selectableStates = ko.observableArray(["InPreparation", "Submitted", "InReview", "Accepted", "Rejected", "InRevision", "Withdrawn"]);

        self.selectedStates = ko.observableArray(["Submitted", "InReview", "Accepted", "Rejected", "InRevision"]);

        ko.bindingHandlers.bsChecked = {
            init: function(element, valueAccessor) {
                $(element).change(function() {
                    var value = ko.unwrap(valueAccessor());
                    var this_val = $(this).val();
                    // We are doing this before the change
                    var is_active = !$(this.parentElement).hasClass("active");
                    var idx = value.indexOf(this_val);

                    var change = true;
                    if (!is_active && idx > -1) {
                        value.splice(idx, 1);
                    } else if (is_active && idx < 0) {
                        value.push(this_val);
                    } else {
                        change = false;
                    }

                    if (change) {
                        // We don't seem to trigger notifications automatically
                        valueAccessor().notifySubscribers(value, "arrayChange");
                    }
                });
            },
            update: function(element, valueAccessor, allBindings) {
                // First get the latest data that we're bound to
                var value = ko.unwrap(valueAccessor());
                var myval = $(element).val();
                var idx = value.indexOf(myval);

                var should_be_active = idx > -1;
                var is_active = $(element.parentElement).hasClass("active");

                if (should_be_active != is_active) {
                    $(element.parentElement).toggleClass("active");
                }
            }
        };

        self.showAbstractsWithState = function(visibleStates) {
            var new_list = self.abstractsData.filter(function(elm) {
                return visibleStates.indexOf(elm.state()) > -1;
            });

            self.abstracts(new_list);
            self.abstractNumbers({ total: self.abstractsData.length, active: new_list.length });
        };

        self.selectedStates.subscribe(self.showAbstractsWithState, "null", "arrayChange");

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
                self.message({message: text, level: "alert-" + level, desc: description});
            }
        };

        // Helper functions
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
            $.ajax("/api/abstracts/" + abstract.uuid + "/state", {
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
            // Reset the message box
            self.note("");
            $("#state-dialog").modal("show");
            self.selectedAbstract(abstract);
        };

        self.finishStateChange = function() {
            var state = $("#state-dialog").find("select").val();

            self.setState(self.selectedAbstract(), state, self.note());
            self.selectedAbstract();
        };

        self.editorNoteCharactersLeft = ko.computed(
            function () {
                if (self.note()) {
                    return 255 - self.note().length;
                } else {
                    return 255;
                }
            },
            self
        );

        self.mkAuthorList = function(abstract) {
            if (abstract.authors.length < 1) {
                return "";
            }

            var text = abstract.authors[0].lastName;

            if (abstract.authors.length > 1) {
                text += " et. al.";
            }

            return text;
        };

        // Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            self.setError("danger", "Error while fetching data from server: ", err);
        };

        self.ensureData = function() {
            self.isLoading(true);
            if (self.abstractsData !== null) {
                self.isLoading(false);
                return;
            }

            var confURL = "/api/conferences/" + confId;
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            // Conference data
            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
                $.getJSON(confURL + "/allAbstracts", onAbstractData).fail(self.ioFailHandler);
            }

            // Abstract data
            function onAbstractData(absArray) {
                var absList = models.Abstract.fromArray(absArray);

                absList.forEach(function (abstr) {
                    abstr.makeObservable(["state"]);
                    abstr.possibleStates = ko.computed(function () {
                        var fromState = abstr.state();

                        if (fromState === "Saving...") {
                            return [];
                        }

                        return self.stateHelper.getPossibleStatesFor(fromState, true, self.conference().isOpen);
                    });

                    abstr.viewEditCtx = ko.computed(function () {
                        // Only time that admins can make changes is in the InRevision state
                        var canEdit = abstr.state() === "InRevision";

                        return {
                            link: canEdit ? "/myabstracts/" + abstr.uuid + "/edit" : "/abstracts/" + abstr.uuid,
                            label: canEdit ? "Edit" : "View",
                            btn: canEdit ? "btn-danger" : "btn-primary"
                        };
                    });
                });

                self.abstractsData = absList;
                self.showAbstractsWithState(self.selectedStates());
                self.isLoading(false);
            }
        };
    }

    $(document).ready(function() {
        var data = tools.hiddenData();

        window.dashboard = adminAbstractsViewModel(data.conferenceUuid);
        window.dashboard.init();
    });
});
});
