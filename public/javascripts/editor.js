require(["lib/models", "lib/tools"], function(models, tools) {
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

        var self = this;


        self.conference = ko.observable(null);
        self.abstract = ko.observable(null);
        self.editedAbstract = ko.observable(null);

        self.isAbstractSaved = ko.observable(false);

        self.init = function() {

            if (confId) {
                self.getConference(confId);
                self.abstract(models.ObservableAbstract());
            }
            if (abstrId) {
                self.getAbstract(abstrId);
                self.isAbstractSaved(true);
            }

            ko.applyBindings(window.editor);
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
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the conference: uuid = " + confId);
            }

        };

        self.getAbstract = function(abstrId) {

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
                self.editedAbstract(self.abstract())
            }

            function fail(xhr, stat, msg) {
                console.log("Error while requesting the abstract: uuid = " + abstrId);
            }

        };


        self.saveAbstract = function() {

            if (self.isAbstractSaved()) {

                $.ajax({
                    async: false,
                    url: "/api/abstracts/" + self.abstract().uuid,
                    type: "PUT",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: self.abstract().toJSON()
                });

            } else if (confId) {

                $.ajax({
                    async: false,
                    url: "/api/conferences/" + confId + "/abstracts",
                    type: "POST",
                    success: success,
                    error: fail,
                    contentType: "application/json",
                    dataType: "json",
                    data: self.abstract().toJSON()
                });

            } else {
                throw "Conference id or abstract id must be defined";
            }

            function success(obj, stat, xhr) {
                self.abstract(models.ObservableAbstract.fromObject(obj));
                self.isAbstractSaved(true);
            }

            function fail(xhr, stat, msg) {
                console.log("Error while saving the abstract");
            }
        };


        self.doStartEdit = function() {
            var obj = $.extend(true, {}, self.abstract().toObject());
            self.editedAbstract(models.ObservableAbstract.fromObject(obj));
        };


        self.doEndEdit = function() {
            self.abstract(self.editedAbstract());
        };


        self.addAuthor = function() {
            var author = models.ObservableAuthor();
            author.position(self.editedAbstract().authors().length);
            self.editedAbstract().authors.push(author);
        };


        self.removeAuthor = function(author) {
            var position = author.position(),
                authors = self.editedAbstract().authors();

            authors.splice(position, 1);
            authors.forEach(function(a, i) {
                a.position(i);
            });

            self.editedAbstract().authors(authors);
        };


        self.addAffiliation = function() {
            var affiliation = models.ObservableAffiliation();
            affiliation.position(self.editedAbstract().affiliations().length);
            self.editedAbstract().affiliations.push(affiliation);
        };


        self.removeAffiliation = function(affiliation) {
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

        self.authorsForAffiliation = function(affiliation) {
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

        /**
         * Add an affiliation position to an author.
         * The author information is determined through jQuery by the respective select element.
         *
         * @param affiliation   The affiliation that is added to the author.
         */
        self.doAddAuthorToAffiliation = function(affiliation) {
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
        self.doRemoveAffiliationFromAuthor = function(affiliation, author) {
            var affiliationPos = affiliation.position(),
                affiliations = author.affiliations();

            while (affiliations.indexOf(affiliationPos) >= 0) {
                affiliations.splice(affiliations.indexOf(affiliationPos), 1);
                console.log("Remove affiliation '" + affiliation.format() +
                            "' from author '" + author.formatName() + "'");
            }

            author.affiliations(affiliations);
        };

        self.doAddReference = function() {
            self.editedAbstract().references.push(models.ObservableReference());
        };

        self.doRemoveReference = function(index) {
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
