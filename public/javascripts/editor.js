require(["lib/models", "lib/tools", "lib/msg"], function(models, tools, msg) {
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

        self.textCharacterLimit = 2000;
        self.ackCharacterLimit = 200;

        self.conference = ko.observable(null);
        self.abstract = ko.observable(null);
        self.editedAbstract = ko.observable(null);
        self.originalState = ko.observable(null);

        self.isAbstractSaved = ko.computed(
            function() {
                return self.abstract() && self.abstract().uuid;
            },
            self
        );

        self.hasAbstractFigures = ko.computed(
            function() {
                return self.abstract() && self.abstract().figures().length > 0;
            },
            self
        );

        self.editorTextCharactersLeft = ko.computed(
            function() {
                if (self.editedAbstract() && self.editedAbstract().text()) {
                    return self.textCharacterLimit - self.editedAbstract().text().length;
                } else {
                    return self.textCharacterLimit;
                }
            },
            self
        );

        self.editorAckCharactersLeft = ko.computed(
            function() {
                if (self.editedAbstract() && self.editedAbstract().acknowledgements()) {
                    return self.ackCharacterLimit - self.editedAbstract().acknowledgements().length;
                } else {
                    return self.ackCharacterLimit;
                }
            },
            self
        );

        self.showButtonSave = ko.computed(
            function() {
                if (self.abstract()) {
                    var saved = self.isAbstractSaved(),
                        state = self.originalState();

                    return !saved || !state || state === 'InPreparation';
                } else {
                    return false;
                }
            },
            self
        );

        self.showButtonSubmit = ko.computed(
            function() {
                if (self.abstract()) {
                    var saved = self.isAbstractSaved(),
                        state = self.originalState();

                    return saved && (!state || state === 'InPreparation');
                } else {
                    return false;
                }
            },
            self
        );

        self.showButtonWithdraw = ko.computed(
            function() {
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
            function() {
                var saved = self.isAbstractSaved(),
                    state = self.originalState();

                return saved && (!state || state === 'Withdrawn');
            },
            self
        );


        self.isChangeOk = ko.computed(
            function() {
                var saved = self.isAbstractSaved(),
                    oldState = self.originalState(),
                    newState = self.abstract() ? self.abstract().state() : null,
                    isOk = false;

                if (!saved) {
                    isOk = (newState === 'InPreparation' || newState === 'Submitted');
                } else {
                    switch(oldState) {
                        case 'InPreparation':
                            isOk = (newState === 'InPreparation' || newState === 'Submitted');
                            break;
                        case 'Submitted':
                            isOk = (newState === 'Withdrawn');
                            break;
                        case 'Withdrawn':
                            isOk = (newState === 'InPreparation');
                            break;
                    }
                }

                return isOk;
            },
            self
        );


        self.init = function() {

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


        self.getEditorAuthorsForAffiliation = function(affiliation) {
            var authors = [];

            self.editedAbstract().authors().forEach(function(author) {
                var aff = author.affiliations(),
                    pos = affiliation.position(),
                    found = aff.indexOf(pos);
                if (found >= 0) {
                    authors.push(author);
                }
            });

            return authors;
        };


        self.requestConference = function(confId) {

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
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the conference: uuid = " + confId);
            }

        };


        self.requestAbstract = function(abstrId) {

            $.ajax({
                async: false,
                url: "/api/abstracts/" + abstrId,
                type: "GET",
                success: success,
                error: fail,
                dataType: "json"
            });

            function success(obj, stat, xhr) {
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.originalState(self.abstract().state());
                self.editedAbstract(self.abstract())
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the abstract: uuid = " + abstrId);
            }

        };


        self.figureUpload = function(callback) {

            var json = { name: $("#figure-name").val(), caption: $("#figure-caption").val() },
                files = $("#figure-file").get(0).files,
                data = new FormData();

            if (files.length > 0) {
                data.append('file', files[0]);
                data.append('figure', JSON.stringify(json));

                $.ajax({
                    url: '/api/abstracts/' + self.abstract().uuid + '/figures',
                    type: 'POST',
                    dataType: "json",
                    data: data,
                    processData: false,
                    contentType: false,
                    success: success,
                    error: fail
                });
            } else {
                console.log("No figure data!")
            }

            function success(obj, stat, xhr) {

                $("#figure-name").val(null);
                $("#figure-caption").val(null);
                $("#figure-file").val(null);

                if (callback) {
                    callback(obj, stat, xhr);
                }
            }

            function fail(xhr, stat, msg) {
                console.log("Error while saving the figure: " + msg);
            }
        };


        self.doRemoveFigure = function() {

            if (! self.isChangeOk()) {
                throw "Unable to save abstract: illegal state";
            }

            if (self.hasAbstractFigures()) {
                var figure = self.abstract().figures()[0];

                $.ajax({
                    url: '/api/figures/' + figure.uuid,
                    type: 'DELETE',
                    dataType: "json",
                    success: success,
                    error: fail
                })
            } else {
                console.log("Unable to delete figure: abstract has no figure");
            }

            function success(obj, stat, xhr) {
                $("#figure-name").val(null);
                $("#figure-caption").val(null);
                $("#figure-file").val(null);

                console.log("Figure deleted: " + JSON.stringify(obj));
                self.requestAbstract(self.abstract().uuid);
            }

            function fail(xhr, stat, msg) {
                console.log("Error while deleting the figure: " + msg);
            }
        };


        self.doSaveAbstract = function(abstract) {

            if (! self.isChangeOk()) {
                throw "Unable to save abstract: illegal state";
            }

            if (! (abstract instanceof models.ObservableAbstract)) {
                abstract = self.abstract();
            }

            if (self.isAbstractSaved()) {

                $.ajax({
                    async: false,
                    url: "/api/abstracts/" + self.abstract().uuid,
                    type: "PUT",
                    success: successAbs,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: abstract.toJSON()
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
                    data: abstract.toJSON()
                });

            } else {
                throw "Conference id or abstract id must be defined";
            }

            function successAbs(obj, stat, xhr) {
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.originalState(self.abstract().state());
                self.editedAbstract(self.abstract());

                var doFig = !self.hasAbstractFigures();
                if (doFig) {
                    self.figureUpload(successFig);
                } else {
                    console.log("Abstract has already a figure.");
                }
            }

            function successFig(obj, stat, xhr) {
                self.requestAbstract(self.abstract().uuid)
            }

            function fail(xhr, stat, msg) {
                console.log("Error while saving the abstract");
            }
        };


        self.doSubmitAbstract = function() {
            self.abstract().state('Submitted');
            self.doSaveAbstract(self.abstract())
        };


        self.doWithdrawAbstract = function() {
            self.abstract().state('Withdrawn');
            self.doSaveAbstract(self.abstract())
        };


        self.doReactivateAbstract = function() {
            self.abstract().state('InPreparation');
            self.doSaveAbstract(self.abstract())
        };


        self.doStartEdit = function(editorId) {
            var ed = $(editorId).find("input").first();
            ed.focus();

            var obj = $.extend(true, {}, self.abstract().toObject());
            self.editedAbstract(models.ObservableAbstract.fromObject(obj));
        };


        self.doEndEdit = function(editorId) {
            if (self.isAbstractSaved()) {
                self.doSaveAbstract(self.editedAbstract())
            } else {
                self.abstract(self.editedAbstract());
            }

            //re-do Math typesetting, TODO: do this at a more sensible place
            MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
        };


        self.doEditAddAuthor = function() {
            var author = models.ObservableAuthor();
            author.position(self.editedAbstract().authors().length);
            self.editedAbstract().authors.push(author);
        };


        self.doEditRemoveAuthor = function(author) {
            var position = author.position(),
                authors = self.editedAbstract().authors();

            authors.splice(position, 1);
            authors.forEach(function(a, i) {
                a.position(i);
            });

            self.editedAbstract().authors(authors);
        };


        self.doEditAddAffiliation = function() {
            var affiliation = models.ObservableAffiliation();
            affiliation.position(self.editedAbstract().affiliations().length);
            self.editedAbstract().affiliations.push(affiliation);
        };


        self.doEditRemoveAffiliation = function(affiliation) {
            var position = affiliation.position(),
                affiliations = self.editedAbstract().affiliations(),
                authors = self.editedAbstract().authors();

            affiliations.splice(position, 1);
            affiliations.forEach(function(a, i) {
                a.position(i);
            });

            authors.forEach(function(author) {
                var affiliationPositions = author.affiliations(),
                    removePos = affiliationPositions.indexOf(position);

                if (removePos >= 0) {
                    affiliationPositions.splice(removePos, 1);
                }

                for (var i = 0; i < affiliationPositions.length; i++) {
                    if (affiliationPositions[i] >= removePos) {
                        affiliationPositions[i] = affiliationPositions[i] - 1;
                    }
                }

                author.affiliations(affiliationPositions);
            });

            self.editedAbstract().affiliations(affiliations);
        };


        /**
         * Add an affiliation position to an author.
         * The author information is determined through jQuery by the respective select element.
         *
         * @param affiliation   The affiliation that is added to the author.
         */
        self.doEditAddAuthorToAffiliation = function(affiliation) {
            var authorPosition = $("#author-select-" + affiliation.position()).find("select").val(),
                authors = self.editedAbstract().authors();

            if (authorPosition >= 0 && authorPosition < authors.length) {
                var author = authors[authorPosition],
                    affiliationPosition = affiliation.position();

                if (author.affiliations().indexOf(affiliationPosition) < 0) {
                    author.affiliations.push(affiliation.position());
                    console.log("Add author '" + author.formatName() + "' to affiliation '" + affiliation.format() + "'");
                } else {
                    console.log("Author '" + author.formatName() + "' is already affiliated with '" + affiliation.format() + "'");
                }

            } else {
                throw "Unable to add author to affiliation: " + affiliation.format();
            }

        };


        /**
         * Remove all affiliation positions for the authors affiliations array.
         *
         * @param affiliation   The affiliation to remove.
         * @param author        The author from which to remove the affiliation.
         */
        self.doEditRemoveAffiliationFromAuthor = function(affiliation, author) {
            var affiliationPos = affiliation.position(),
                affiliations = author.affiliations();

            while (affiliations.indexOf(affiliationPos) >= 0) {
                affiliations.splice(affiliations.indexOf(affiliationPos), 1);
                console.log("Remove affiliation '" + affiliation.format() +
                            "' from author '" + author.formatName() + "'");
            }

            author.affiliations(affiliations);
        };


        self.doEditAddReference = function() {
            self.editedAbstract().references.push(models.ObservableReference());
        };


        self.doEditRemoveReference = function(index) {
            var references = self.editedAbstract().references();

            references.splice(index, 1);

            self.editedAbstract().references(references);
        }

    }

    // start the editor
    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data["conferenceUuid"]);
        console.log(data["abstractUuid"]);

        window.editor = EditorViewModel(data["conferenceUuid"], data["abstractUuid"]);
        window.editor.init();
    });

});
