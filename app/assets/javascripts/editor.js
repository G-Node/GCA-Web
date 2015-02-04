require(["main"], function () {
require(["knockout", "lib/models", "lib/tools", "lib/msg", "lib/validate", "lib/owned", "ko.sortable"],
function (ko, models, tools, msg, validate, owned) {
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

        var self = tools.inherit(this, msg.MessageVM);
        self = tools.inherit(self, owned.Owned);

        self.textCharacterLimit = 2000;
        self.ackCharacterLimit = 200;

        self.conference = ko.observable(null);
        self.abstract = ko.observable(null);
        self.editedAbstract = ko.observable(null);
        self.originalState = ko.observable(null);

        // required to set displayed modal body
        self.modalBody = ko.observable(null);
        // set default modals footer template
        self.modalFooter = ko.observable("generalModalFooter");

        // only required when a new figure is added
        self.newFigure = {
            file: null,
            caption: null
        };

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

        self.editorTextCharactersLeft = ko.computed(
            function () {
                if (self.editedAbstract() && self.editedAbstract().text()) {
                    return self.textCharacterLimit - self.editedAbstract().text().length;
                } else {
                    return self.textCharacterLimit;
                }
            },
            self
        );

        self.editorAckCharactersLeft = ko.computed(
            function () {
                if (self.editedAbstract() && self.editedAbstract().acknowledgements()) {
                    return self.ackCharacterLimit - self.editedAbstract().acknowledgements().length;
                } else {
                    return self.ackCharacterLimit;
                }
            },
            self
        );

        self.showButtonSave = ko.computed(
            function () {
                if (self.abstract()) {
                    var saved = self.isAbstractSaved(),
                        state = self.originalState();

                    return !saved || !state || state === 'InPreparation' || state === 'InRevision';
                } else {
                    return false;
                }
            },
            self
        );

        self.showButtonSubmit = ko.computed(
            function () {
                if (self.abstract()) {
                    var saved = self.isAbstractSaved(),
                        state = self.originalState();

                    return saved && (!state || state === 'InPreparation' || state === 'InRevision');
                } else {
                    return false;
                }
            },
            self
        );

        self.showButtonWithdraw = ko.computed(
            function () {
                if (self.abstract()) {
                    var ok = ['Submitted', 'InReview'];
                    return self.isAbstractSaved() && (ok.indexOf(self.originalState()) >= 0);
                } else {
                    return false;
                }
            },
            self
        );

        self.showButtonReactivate = ko.computed(
            function () {
                var saved = self.isAbstractSaved(),
                    state = self.originalState();

                return saved && (!state || state === 'Withdrawn');
            },
            self
        );

        // hide edit buttons, if the abstract is in any state other
        // than "inPreparation" or if its not a new submission
        self.showEditButton = ko.computed(
            function () {
                var saved = self.isAbstractSaved(),
                    state = self.originalState();

                return !saved || (!state || state === 'InPreparation');
            },
            self
        );

        self.init = function () {

            if (confId) {
                self.requestConference(confId);
            }
            if (abstrId) {
                self.requestAbstract(abstrId);
            } else {
                self.abstract(models.ObservableAbstract());
                self.originalState(self.abstract().state());
                self.editedAbstract(self.abstract());
            }

            ko.applyBindings(window.editor);
            MathJax.Hub.Configured(); //start MathJax
        };


        self.isChangeOk = function (abstract) {

            abstract = abstract || self.abstract();

            var saved = self.isAbstractSaved(),
                oldState = self.originalState(),
                newState = abstract ? abstract.state() : null,
                isOk = false;

            if (!saved) {
                isOk = (newState === 'InPreparation' || newState === 'Submitted');
            } else {
                switch (oldState) {
                    case 'InPreparation':
                        isOk = (newState === 'InPreparation' || newState === 'Submitted');
                        break;
                    case 'Submitted':
                        isOk = (newState === 'Withdrawn');
                        break;
                    case 'Withdrawn':
                        isOk = (newState === 'InPreparation');
                        break;
                    case 'InRevision':
                        isOk = (newState === 'InRevision' || newState === 'Submitted');
                        break;
                }
            }

            return isOk;
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
                self.originalState(self.abstract().state());
                self.editedAbstract(self.abstract());
                self.setupOwners("/api/abstracts/" + abstrId + "/owners", self.setError);
                self.loadOwnersData(null);
            }

            function fail() {
                self.setError("Error", "Unable to request the abstract: uuid = " + abstrId);
            }

        };


        self.getNewFigure = function(data, event) {
          self.newFigure.file = event.currentTarget.files[0];
        };


        self.figureUpload = function (callback) {

            var json = {caption: self.newFigure.caption},
                files = self.newFigure.file,
                data = new FormData();

            if (files) {
                var fileName = files.name,
                    fileSize = files.size,
                    splitted = fileName.split('.'),
                    ending = splitted[splitted.length - 1].toLowerCase();

                if (['jpeg', 'jpg', 'gif', 'giff', 'png'].indexOf(ending) < 0) {
                    self.setError("Error", "Figure file format not supported (only jpeg, gif or png is allowed).");
                    return;
                }

                if (fileSize > 5242880) {
                    self.setError("Error", "Figure file is too large (limit is 5MB).");
                    return;
                }

                data.append('file', files);
                data.append('figure', JSON.stringify(json));

                $.ajax({
                    url: '/api/abstracts/' + self.abstract().uuid + '/figures',
                    type: 'POST',
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
                self.newFigure.caption = null;

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

            if (self.hasAbstractFigures()) {
                var figure = self.abstract().figures()[0];

                $.ajax({
                    url: "/api/figures/" + figure.uuid,
                    type: "PUT",
                    contentType: "application/json",
                    dataType: "json",
                    data: figure.toJSON(),
                    processData: false,
                    error: fail,
                    cache: false
                });

            } else {
                self.setWarning("Error", "Unable to update caption: figure not found", true);
            }

            function fail() {
                self.setError("Error", "Unable to update caption");
            }
        };

        self.doRemoveFigure = function () {

            if (!self.isChangeOk()) {
                self.setError("Error", "Unable to save abstract: illegal state");
                return;
            }

            if (self.hasAbstractFigures()) {
                var figure = self.abstract().figures()[0];

                $.ajax({
                    url: '/api/figures/' + figure.uuid,
                    type: 'DELETE',
                    dataType: "json",
                    success: success,
                    error: fail,
                    cache: false
                })
            } else {
                self.setWarning("Error", "Unable to delete figure: abstract has no figure", true);
            }

            function success() {
                $("#figure-update-caption").val(null);
                self.newFigure.file = null;
                self.newFigure.caption = null;

                self.requestAbstract(self.abstract().uuid);

                self.setOk("Ok", "Figure removed from abstract");
            }

            function fail() {
                self.setError("Error", "Unable to delete the figure");
            }
        };


        self.doSaveAbstract = function (abstract) {

            if (!(abstract instanceof models.ObservableAbstract)) {
                abstract = self.abstract();
            }

            if (!self.isChangeOk(abstract)) {
                self.setError("Error", "Unable to save abstract: illegal state");
                return;
            }

            var result = validate.abstract(abstract);

            if (result.hasErrors()) {
                self.setError("Error", "Unable to save abstract: " + result.errors[0]);
                return;
            }

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
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.originalState(self.abstract().state());
                self.editedAbstract(self.abstract());

                var hasNoFig = !self.hasAbstractFigures(),
                    hasFigData = self.newFigure.file ? true : false;

                if (hasNoFig && hasFigData) {
                    self.figureUpload(successFig);
                } else {
                    if (result.hasWarnings()) {
                        self.setInfo("Note", "The abstract was saved but still has issues: " + result.warnings[0]);
                    } else {
                        self.setOk("Ok", "Abstract saved.", true);
                    }
                }

                self.setupOwners("/api/abstracts/" + self.abstract().uuid + "/owners", self.setError);
                self.loadOwnersData(null);
            }

            function successFig() {
                self.requestAbstract(self.abstract().uuid);
                if (result.hasWarnings()) {
                    self.setInfo("Note", "The abstract and figure was saved but still has issues: " + result.warnings[0]);
                } else {
                    self.setOk("Ok", "Abstract and figure saved.", true);
                }
            }

            function fail() {
                self.setError("Error", "Unable to save abstract!");
            }
        };


        self.doSubmitAbstract = function () {
            self.abstract().state('Submitted');
            self.doSaveAbstract(self.abstract())
        };


        self.doWithdrawAbstract = function () {
            self.abstract().state('Withdrawn');
            self.doSaveAbstract(self.abstract())
        };


        self.doReactivateAbstract = function () {
            self.abstract().state('InPreparation');
            self.doSaveAbstract(self.abstract())
        };


        self.doStartEdit = function (editorId) {
            var ed = $(editorId).find("input").first();
            ed.focus();

            var obj = $.extend(true, {}, self.abstract().toObject());
            self.editedAbstract(models.ObservableAbstract.fromObject(obj));

            // ensure correct script for modal body is loaded
            self.modalBody("body-"+ editorId.replace('#',''));
        };


        self.doEndEdit = function () {

            if (self.isAbstractSaved()) {
                self.doSaveAbstract(self.editedAbstract())
            } else {
                var result = validate.abstract(self.editedAbstract());
                if (result.hasErrors()) {
                    self.setWarning("Warning", result.errors[0]);
                } else if (result.hasWarnings()) {
                    self.setInfo("Note", result.warnings[0]);
                } else {
                    self.clearMessage();
                }
                self.abstract(self.editedAbstract());
            }

            //re-do Math typesetting, TODO: do this at a more sensible place
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
            var authorIndex = $("#author-select-" + index).find("select").val(),
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
            } else {
                self.setInfo("Hint", "This author is assigned to this affiliation.");
            }
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
        }

    }

    // start the editor
    $(document).ready(function () {

        var data = tools.hiddenData();

        console.log(data["conferenceUuid"]);
        console.log(data["abstractUuid"]);

        window.editor = EditorViewModel(data["conferenceUuid"], data["abstractUuid"]);
        window.editor.init();
    });

});
});
