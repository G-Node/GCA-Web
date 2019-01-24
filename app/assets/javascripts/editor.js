require(["main"], function () {
require(["knockout", "lib/models", "lib/tools", "lib/msg", "lib/validate", "lib/owned", "lib/astate", "ko.sortable"],
function (ko, models, tools, msg, validate, owned, astate) {
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
        if (!(this instanceof EditorViewModel)) {
            return new EditorViewModel(confId, abstrId);
        }

        var self = tools.inherit(this, msg.MessageBox);
        self = tools.inherit(self, owned.Owned);

        self.conference = ko.observable(null);
        self.abstract = ko.observable(null);
        self.editedAbstract = ko.observable(null);
        self.stateLog = ko.observable(null);
        // dirty workaround below
        self.abstrGrChangeWatch = ko.observable(null);

        self.selectedType = ko.observable(null);

        // autosave label
        self.autosave = ko.observable({text: "Loading", css: "label-primary"});

        // required to set displayed modal header
        self.modalHeader = ko.observable(null);
        // required to set displayed modal body
        self.modalBody = ko.observable(null);
        // set default modals footer template
        self.modalFooter = ko.observable("generalModalFooter");

        // only required when a new figure is added
        self.newFigure = {
            file: null
        };

        self.newFigureCaption = ko.observable(null);

        // required to affiliate an author with a department
        self.selectedAffiliationAuthor = 0;

        // just a shortcut
        self.stateHelper = astate.changeHelper;

        self.isAbstractSaved = ko.computed(
            function () {
                return self.abstract() && self.abstract().uuid;
            },
            self
        );

        self.hasAbstractFigures = ko.computed(
            function () {
                return self.abstract() && self.abstract().figures().length > 0;
            },
            self
        );

        self.latexInTitle = ko.computed(
            function () {
                var checkTitle = /.*\$.*\$.*/;
                if (self.editedAbstract() && self.editedAbstract().title() && checkTitle.test(self.editedAbstract().title())) {
                    return "Please avoid using LaTeX code in the abstract title";
                }
                return "";
            },
            self
        );

        self.showAbstractTextCharsLeft = ko.computed(
            function () {
                var textCharLimit = $("#maxLenPrepText").text();
                if (self.editedAbstract() && self.editedAbstract().text() && textCharLimit !== undefined) {
                    return true;
                } else {
                    return textCharLimit !== undefined;
                }
            },
            self
        );

        self.editorTextCharactersLeft = ko.computed(
            function () {
                var textCharLimit = $("#maxLenPrepText").text();
                if (self.editedAbstract() && self.editedAbstract().text() && textCharLimit !== undefined) {
                    return textCharLimit - self.editedAbstract().text().length;
                } else {
                    return textCharLimit;
                }
            },
            self
        );

        self.showAckCharsLeft = ko.computed(
            function () {
                var ackCharLimit = $("#maxLenPrepAck").text();
                if (self.editedAbstract() && self.editedAbstract().acknowledgements() && ackCharLimit !== undefined) {
                    return true;
                } else {
                    return ackCharLimit !== undefined;
                }
            },
            self
        );

        self.editorAckCharactersLeft = ko.computed(
            function () {
                var ackCharLimit = $("#maxLenPrepAck").text();
                if (self.editedAbstract() && self.editedAbstract().acknowledgements() && ackCharLimit !== undefined) {
                    return ackCharLimit - self.editedAbstract().acknowledgements().length;
                } else {
                    return ackCharLimit;
                }
            },
            self
        );

        // Hide edit buttons if the abstract is in any state other than "InPreparation" or "InRevision"
        self.showEditButton = ko.computed(
            function () {
                var ok = ["InPreparation", "InRevision"];
                return !self.isAbstractSaved() || ok.indexOf(self.abstract().state()) > -1;
            },
            self
        );

        // validation
        // If conference has no presentation preferences, suppress messages from validate.js
        self.checkRemovePresPref = function (warnings) {
            if (!self.conference().hasPresentationPrefs) {
                warnings.forEach(function (currWarning) {
                    if (currWarning.match("presentation")) {
                        warnings.splice(warnings.indexOf(currWarning), 1);
                    }
                });
            }
        };

        // If conference has no topics defined, suppress messages from validate.js
        self.checkRemoveTopics = function (warnings) {
            if (self.conference().topics === null || self.conference().topics.length === 0) {
                warnings.forEach(function (currWarning) {
                    if (currWarning.match("topic")) {
                        warnings.splice(warnings.indexOf(currWarning), 1);
                    }
                });
            }
        };

        self.validity = ko.computed(
            function() {
                var abstract = self.abstract();
                if (abstract === null || abstract === undefined) {
                    return {
                        ok: true,
                        isError: false,
                        badgeLevel: "btn-default",
                        badgeText: "N/A",
                        items: [],
                        handler: function() {}
                    };
                }
                var result = validate.abstract(abstract);
                if (result.ok()) {
                    return {
                        ok: true,
                        isError: false,
                        badgeLevel: "btn-success",
                        badgeText: "Ok",
                        items: [],
                        handler: function() {}
                    };
                } else if (result.hasErrors()) {
                    var nerr = result.errors.length;
                    return {
                        ok: false,
                        isError: true,
                        badgeLevel: self.isAbstractSaved() ? "btn-danger" : " btn-default",
                        badgeText: "" + nerr  + " issue" + (nerr > 1 ? "s" : ""),
                        handler: self.presentValidationResults,
                        items: result.errors
                    };
                } else {
                    // Suppress error, if conference has no presentation preferences
                    self.checkRemovePresPref(result.warnings);
                    // Suppress error, if conference has no defined topics
                    self.checkRemoveTopics(result.warnings);
                    var nwarn = result.warnings.length;

                    // Return result Ok, if no warnings are left after preference and
                    // topics warnings have been suppressed.
                    if (nwarn !== null && nwarn !== undefined && nwarn === 0) {
                        return {
                            ok: true,
                            isError: false,
                            badgeLevel: "btn-success",
                            badgeText: "Ok",
                            items: [],
                            handler: function() {}
                        };
                    }

                    return {
                        ok: false,
                        isError: false,
                        badgeLevel: "btn-warning",
                        badgeText: "" + nwarn  + " issue" + (nwarn > 1 ? "s" : ""),
                        handler: self.presentValidationResults,
                        items: result.warnings
                    };
                }
            },
            self
        );

        self.presentValidationResults = function () {
            self.modalHeader("header-validation");
            self.modalBody("body-validation");
            self.modalFooter("footer-validation");
        };

        self.init = function () {
            if (confId) {
                self.requestConference(confId);
            }
            if (abstrId) {
                self.requestAbstract(abstrId);
            } else {
                self.abstract(models.ObservableAbstract());
                self.editedAbstract(self.abstract());
                self.showHelp();
            }

            ko.applyBindings(window.editor);

            // start MathJax
            MathJax.Hub.Configured();
        };

        self.getEditorAuthorsForAffiliation = function (index) {
            var authors = [];

            self.editedAbstract().authors().forEach(function (author) {
                if (author.affiliations().indexOf(index) >= 0) {
                    authors.push(author);
                }
            });

            return authors;
        };

        self.showReason = ko.computed(
            function () {
                // dirty workaround part 2
                var _x = self.abstrGrChangeWatch();
                try {
                    return self.conference().group === "BCCN" &&
                        self.abstract().abstrTypes()[0].name === "Talk or Poster";
                } catch (err) {
                    return false;
                }
            },
            self
        );

        self.requestConference = function (confId) {
            $.ajax({
                async: false,
                url: "/api/conferences/" + confId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json",
                cache: false
            });

            function success(obj) {
                self.conference(models.Conference.fromObject(obj));
            }

            function fail() {
                self.setError("Error", "Unable to request the conference: uuid = " + confId);
            }
        };

        self.requestAbstract = function (abstrId) {
            $.ajax({
                async: false,
                url: "/api/abstracts/" + abstrId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json",
                cache: false
            });

            function success(obj) {
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.editedAbstract(self.abstract());
                self.autosave({text: "Ok", css: "label-success"});
                self.fetchStateLog();
                self.setupOwners("/api/abstracts/" + abstrId + "/owners", self.setError);
                self.loadOwnersData(null);
            }

            function fail() {
                self.autosave({text: "Error", css: "btn-danger"});
                self.setError("Error", "Unable to request the abstract: uuid = " + abstrId);
            }
        };

        self.abstrTypeChanged = function(abstrType) {
            // Workaround as long as we allow only one Abstract type
            self.editedAbstract().abstrTypes().pop();
            self.editedAbstract().abstrTypes().push(abstrType);
            self.abstract().abstrTypes().pop();
            self.abstract().abstrTypes().push(abstrType);
            self.abstrGrChangeWatch.valueHasMutated();
            if (!self.showReason()) {
                self.editedAbstract().reasonForTalk(null);
            }
            // needs to return true to enable default click action on the radio buttons (select)
            return true;
        };

        self.getNewFigure = function(data, event) {
          self.newFigure.file = event.currentTarget.files[0];
        };

        self.figureUpload = function (callback) {
            var json = {caption: self.newFigureCaption()},
                files = self.newFigure.file,
                data = new FormData();

            if (files) {
                var fileName = files.name,
                    fileSize = files.size,
                    splitted = fileName.split("."),
                    ending = splitted[splitted.length - 1].toLowerCase();

                if (["jpeg", "jpg", "gif", "giff", "png"].indexOf(ending) < 0) {
                    self.setError("Error", "Figure file format not supported (only jpeg, gif or png is allowed).");
                    return;
                }

                if (fileSize > 5242880) {
                    self.setError("Error", "Figure file is too large (limit is 5MB).");
                    return;
                }

                data.append("file", files);
                data.append("figure", JSON.stringify(json));

                $.ajax({
                    url: "/api/abstracts/" + self.abstract().uuid + "/figures",
                    type: "POST",
                    dataType: "json",
                    data: data,
                    processData: false,
                    contentType: false,
                    success: success,
                    error: fail,
                    cache: false
                });
            }

            function success(obj, stat, xhr) {
                self.newFigure.file = null;
                self.newFigureCaption(null);

                if (callback) {
                    callback(obj, stat, xhr);
                }
            }

            function fail() {
                self.setError("Error", "Unable to save the figure");
            }
        };

        /**
         * Update an existing figure.
         * At the moment the figure caption is the only part
         * where an update actually takes place.
         */
        self.doUpdateFigure = function () {
            self.abstract().figures().map(function (figure) {
                    $.ajax({
                        url: "/api/figures/" + figure.uuid,
                        type: "PUT",
                        contentType: "application/json",
                        dataType: "json",
                        data: figure.toJSON(),
                        success: success,
                        processData: false,
                        error: fail,
                        cache: false
                    });
                }
            );

            // Fetch abstract to avoid stale figure caption
            function success() {
                self.requestAbstract(self.abstract().uuid);
                self.autosave({text: "Ok", css: "label-success"});
            }

            function fail() {
                self.setError("Error", "Unable to update caption");
            }
        };

        self.doRemoveFigure = function (figure) {
            if (self.hasAbstractFigures()) {
                self.autosave({text: "Saving", css: "label-warning"});

                $.ajax({
                    url: "/api/figures/" + figure.uuid,
                    type: "DELETE",
                    dataType: "json",
                    success: success,
                    error: fail,
                    cache: false
                });
            } else {
                self.setWarning("Error", "Unable to delete figure: abstract has no figure", true);
            }

            function success() {
                self.newFigure.file = null;
                // If a figure was removed, keep the caption around in
                // case a user just wants to update the figure itself.
                self.newFigureCaption($("#figure-update-caption").val());

                self.requestAbstract(self.abstract().uuid);
                self.autosave({text: "Ok", css: "label-success"});
            }

            function fail() {
                self.setError("Error", "Unable to delete the figure");
                self.autosave({text: "Error!", css: "label-danger"});
            }
        };

        self.doSaveAbstract = function (abstract) {
            // Clean slate for new messages
            self.clearMessage();

            if (!(abstract instanceof models.ObservableAbstract)) {
                abstract = self.abstract();
            }

            var result = validate.abstract(abstract);
            if (result.hasErrors()) {
                self.setError("Error", "Unable to save abstract: " + result.errors[0]);
                return;
            }

            self.autosave({text: "Saving", css: "label-warning"});
            if (self.isAbstractSaved()) {
                if (self.hasAbstractFigures()) {
                    self.doUpdateFigure();
                }

                $.ajax({
                    async: false,
                    url: "/api/abstracts/" + self.abstract().uuid,
                    type: "PUT",
                    success: successAbs,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: abstract.toJSON(),
                    cache: false
                });
            } else if (confId) {
                $.ajax({
                    async: false,
                    url: "/api/conferences/" + confId + "/abstracts",
                    type: "POST",
                    success: successAbs,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: abstract.toJSON(),
                    cache: false
                });
            } else {
                self.setError("Error", "Conference id or abstract id must be defined. This is a bug: please report!");
            }

            function successAbs(obj) {
                var firstSave = !self.isAbstractSaved();
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.editedAbstract(self.abstract());

                var hasNoFig = !self.hasAbstractFigures(),
                    hasFigData = self.newFigure.file ? true : false;
                    if (hasFigData) {
                    // successFig is a function callback
                    self.figureUpload(successFig);
                } else {
                    self.autosave({text: "Ok", css: "label-success"});
                }

                if (!self.stateLog()) {
                    self.fetchStateLog();
                }

                self.setupOwners("/api/abstracts/" + self.abstract().uuid + "/owners", self.setError);
                self.loadOwnersData(null);

                if (firstSave) {
                    self.showHelp();
                }
            }

            function successFig() {
                self.requestAbstract(self.abstract().uuid);
                self.autosave({text: "Ok", css: "label-success"});
            }

            function fail() {
                self.setError("Error", "Unable to save abstract!");
                self.autosave({text: "Error!", css: "label-danger"});
            }
        };

        self.doStartEdit = function (editorId) {
            var ed = $(editorId).find("input").first();
            ed.focus();

            var obj = $.extend(true, {}, self.abstract().toObject());
            self.editedAbstract(models.ObservableAbstract.fromObject(obj));

            // load corresponding script for modal header
            self.modalHeader("header-" + editorId.replace("#", ""));
            // load corresponding script for modal body
            self.modalBody("body-" + editorId.replace("#", ""));
            // load corresponding script for modal footer (wow, I am such a useful comment)
            self.modalFooter("generalModalFooter");
        };

        self.doEndEdit = function () {
            if (self.isAbstractSaved()) {
                self.doSaveAbstract(self.editedAbstract());
            } else {
                self.abstract(self.editedAbstract());
            }

            // re-do Math typesetting, TODO: do this at a more sensible place
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
        };

        self.doEditAddAuthor = function () {
            var author = models.ObservableAuthor();
            self.editedAbstract().authors.push(author);
        };

        self.doEditRemoveAuthor = function (index) {
            index = index();
            var authors = self.editedAbstract().authors();
            authors.splice(index, 1);
            self.editedAbstract().authors(authors);
        };

        self.doEditAddAffiliation = function () {
            var affiliation = models.ObservableAffiliation();
            self.editedAbstract().affiliations.push(affiliation);
        };

        self.doEditRemoveAffiliation = function (index) {
            index = index();
            var affiliations = self.editedAbstract().affiliations(),
                authors = self.editedAbstract().authors();

            affiliations.splice(index, 1);

            authors.forEach(function (author) {
                var positions = author.affiliations(),
                    removePos = positions.indexOf(index);

                if (removePos >= 0) {
                    positions.splice(removePos, 1);
                }

                author.affiliations(positions);
            });

            self.editedAbstract().affiliations(affiliations);
        };

        /**
         * Add an affiliation position to an author.
         * The author information is determined through jQuery by the respective select element.
         *
         * @param index   The index of the affiliation that is added to the author.
         */
        self.doEditAddAuthorToAffiliation = function (index) {
            index = index();
            var authorIndex = self.selectedAffiliationAuthor,
                authors = self.editedAbstract().authors();

            if (authorIndex < 0 || authorIndex >= authors.length) {
                self.setError("Error", "Unable to add author to affiliation: invalid index");
                return;
            }

            var author = authors[authorIndex],
                affiliations = author.affiliations();

            if (affiliations.indexOf(index) < 0) {
                affiliations.push(index);
                affiliations.sort();
                author.affiliations(affiliations);
            }

            // reset author select index
            self.selectedAffiliationAuthor = 0;
        };

        /**
         * Remove all affiliation positions for the authors affiliations array.
         *
         * @param index         The index of the affiliation to remove.
         * @param author        The author from which to remove the affiliation.
         */
        self.doEditRemoveAffiliationFromAuthor = function (index, author) {
            index = index();
            var positions = author.affiliations(),
                removePos = positions.indexOf(index);

            if (removePos >= 0) {
                positions.splice(removePos, 1);
            }

            author.affiliations(positions);
        };

        self.doEditAddReference = function () {
            self.editedAbstract().references.push(models.ObservableReference());
        };

        self.doEditRemoveReference = function (index) {
            index = index();
            var references = self.editedAbstract().references();

            references.splice(index, 1);

            self.editedAbstract().references(references);
        };

        // state related functions go here
        self.successStateLog = function(logData) {
            astate.logHelper.formatDate(logData);
            self.stateLog(logData);
        };

        self.fetchStateLog = function() {
            var logUrl = "/api/abstracts/" + self.abstract().uuid + "/stateLog";
            $.getJSON(logUrl, self.successStateLog).error(function(jqxhr, textStatus, error) {
                self.setError("Warning", "Unable to fetch state log: " + error);
            });
        };

        // All state changing should be done via the state endpoint
        self.doChangeState = function(toState) {
            var data = {state: toState};

            if (toState === "Submitted") {
                var result = validate.abstract(self.abstract());

                // Suppress error, if conference has no presentation preferences
                self.checkRemovePresPref(result.warnings);
                // Suppress error, if conference has no defined topics
                self.checkRemoveTopics(result.warnings);
                if (!result.ok()) {
                    self.setError("Error", "Unable to submit: " +
                        (result.hasErrors() ? result.errors[0] : result.warnings[0]));
                    return;
                }
            }

            $.ajax("/api/abstracts/" + self.abstract().uuid + "/state", {
                data: JSON.stringify(data),
                type: "PUT",
                contentType: "application/json",
                success: function(result) {
                    self.abstract().state(toState);
                    self.successStateLog(result);
                    self.showHelp();
                },
                error: function(jqxhr, textStatus, error) {
                    self.setError("Error", "Unable to set abstract state: " + error);
                }
            });
        };

        self.doWithdrawAbstract = function () {
            self.doChangeState("Withdrawn");

            // Cleanup any leftover messages
            self.clearMessage();
        };

        self.action = ko.computed(
            function() {
                var saved = self.isAbstractSaved(),
                    open = self.conference() && self.conference().isOpen;

                if (!saved) {
                    if (open) {
                        return {
                            label: "Save",
                            level: "btn-success",
                            action: self.doSaveAbstract
                        };
                    } else {
                        return false;
                    }
                }

                var current = self.abstract().state();
                var possible = self.stateHelper.getPossibleStatesFor(current, false, !open);

                // For this to work, there must be a single next state,
                // with the exception of Withdrawn, which must *not* be
                // the first (cf. the state map in lib/astate)
                if (possible.length > 0) {
                    var next = possible[0];
                    if (next === "Submitted") {
                        return {
                            label: "Submit",
                            level: "btn-danger",
                            action: function() { self.doChangeState(next); },
                            want: next
                        };
                    } else if (next === "InPreparation") {
                        return {
                            label: "Unlock",
                            level: "btn-danger",
                            action: function() { self.doChangeState(next); },
                            want: next
                        };
                    }
                }

                return false;
            },
            self
        );

        self.showButtonWithdraw = ko.computed(
            function () {
                if (self.abstract() && self.isAbstractSaved()) {
                    var state = self.abstract().state(),
                        open = self.conference() && self.conference().isOpen;
                    return self.stateHelper.canTransitionTo(state, "Withdrawn", false, !open);
                } else {
                    return false;
                }
            },
            self
        );

        self.submittedBefore = function() {
            if (!self.stateLog()) {
                return false;
            }

            var log = self.stateLog();
            for (var i = 0; i < log.length; i++) {
                if (log[i].state === "Submitted") {
                    return true;
                }
            }

            return false;
        };

        // help
        self.showHelp = function() {
            var open = self.conference() && self.conference().isOpen;

            if (!open) {
                if (self.abstract().state() === "InRevision") {
                    self.setInfo(
                        "Abstract revision",
                        "Please revise your abstract and re-submit your abstract by " +
                        "clicking <b>Submit</b>"
                    );
                } else {
                    self.setWarning(
                        "Conference closed",
                        "Conference is closed and the abstract cannot be modified."
                    );
                }
            } if (!self.isAbstractSaved()) {
                self.setInfo(
                    "Welcome to abstract submission",
                    "<ul>" +
                    "<li>Nothing will be stored on the server before the abstract is saved for" +
                    "    the first time, so it is ok to play around and explore this editor.</li>" +
                    "<li>After entering at least the abstract title, click the <b>Save</b> button" +
                    "    to store the abstract on the server. Subsequent changes will then be " +
                    "    saved automatically.</li>" +
                    "<li>The 'Validation' field above indicates if there are issues with the" +
                    "    required content of the abstract. Clicking on the <i>issues</i> button will " +
                    "    bring up a dialog with issue details.</li>" +
                    "</ul>"
                );
            } else if (self.abstract().state() === "InPreparation") {
                if (self.submittedBefore()) {
                    self.setWarning("Abstract unlocked",
                        "<ul>" +
                        "<li>Autosave is again enabled, changes will be stored directly on the server.</li>" +
                        "<li>This abstract is <b>not submitted</b> yet.</li>" +
                        "<li>Abstract must be re-submitted before the deadline. If the abstract is not submitted, " +
                        "    it will not be considered for the conference.</li>" +
                        "</ul>"
                    );
                } else {
                    self.setInfo(
                        "Abstract is saved",
                        "<ul>" +
                        "<li>Autosave is enabled, i.e. changes are stored automatically on the server.</li>" +
                        "<li>This abstract is <b>not submitted</b> yet.</li>" +
                        "<li>Once the Validation field shows 'Ok', you can click the " +
                        "    <b>Submit</b> button to submit it. Submitted abstracts can" +
                        "    be modified until the deadline. </li>" +
                        "<li>Additional abstract owners can be added at the bottom of the page via " +
                        "    <b>Owner Management</b>.</li>" +
                        "</ul>"
                    );
                }
            } else if (self.abstract().state() === "Submitted") {
                self.setInfo(
                    "Abstract is submitted",
                    "<ul>" +
                    "<li>Changes are deactivated. Submitted abstracts can be modified until the" +
                    " deadline.</li>" +
                    "<li>To modify a submitted abstract, use the <b>Unlock</b> button.<br/></li>" +
                    "</ul> <br/>" +
                    "<b> <u> Please note that abstract submission does not replace conference registration!</u> <br/>" +
                    "All abstract submitters also have to register for the conference.</b><br/>" +
                    "To register please go to: <a target=\"_blank\" href=\"" +
                    self.conference().link + "\"><span class=\"glyphicon glyphicon-new-window\"></span> " +
                    self.conference().link + "</a>"
                );
            }
        };
    }

    // start the editor
    $(document).ready(function () {
        var data = tools.hiddenData();

        window.editor = EditorViewModel(data["conferenceUuid"], data["abstractUuid"]);
        window.editor.init();
    });
});
});
