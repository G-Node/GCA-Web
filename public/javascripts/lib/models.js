/**
 * Module for misc utility functions
 * @module {models}
 */
define(["lib/tools"], function(tools) {
    "use strict";

    /**
     * Get a property value from another object. Tries to retrieve the underscore
     * variant of name first.
     *
     * @param {string} name     The name of a property.
     * @param {object} source   The source object from where to read the
     *                          value.
     * @returns {*|null}
     * @private
     */
    function readProperty(name, source) {
        var value = null,
            name_under = tools.toUnderscore(name);

        if (source.hasOwnProperty(name_under)) {
            value = source[name_under];
        } else if (source.hasOwnProperty(name)) {
            value = source[name];
        }

        return value;
    }

    /**
     * Base class for models.
     *
     * @param {string} [uuid]   The uuid of the model.
     *
     * @returns {Model}
     * @constructor
     */
    function Model(uuid) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Model(uuid);
        }

        var self = this;

        self.uuid = uuid || null;


        self.toObject = function() {
            var prop,
                obj = {};

            for (prop in self) {
               if (self.hasOwnProperty(prop)) {
                   var value = self[prop];
                   if (tools.type(value) !== "function") {
                       obj[prop] = value;
                   }
               }
            }
        };

        /**
         * Turns the model object into a json string.
         *
         * @param {number|null} [indention] The indention level, null for no pretty
         *                                  print.
         *
         * @returns {string} JSON string.
         */
        self.toString = function(indention) {
            if (indention === undefined) {
                indention = indention || 4;
            }
            return JSON.stringify(self.toObject(), null, indention);
        };

    }

    /**
     * Create a model from a regular object.
     *
     * @param {object} source           The source object from which to create the model.
     * @param {function} constructor    Constructor of the model class.
     *
     * @returns The created model object.
     */
    Model.fromObject = function(source, constructor) {
        var prop,
            target = constructor();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, source);

                if (tools.type(value) !== "function") {
                    target[prop] = readProperty(prop, source);
                }
            }
        }

        return target;
    };

    /**
     * Create an array of model objects from an array of ordinary objects.
     *
     * @param {Array} array         The source array.
     * @param {function} factory    A factory function that creates a model from an
     *                              object.
     *
     * @returns {Array} Array with created model objects.
     */
    Model.fromArray = function(array, factory) {
        array = array || [];
        var created = [];

        array.forEach(function(obj) {
           created.append(factory(obj));
        });

        return created;
    };

    /**
     * Model for conference.
     *
     * @param {string} [uuid]   The uuid of the conference.
     * @param {string} [name]   The name of the conference.
     * @param {string} [owners] URL to all abstract owners.
     * @param {string} [abstracts] The URL to all abstracts.
     *
     * @returns {Conference}
     * @constructor
     * @public
     */
    function Conference(uuid, name, owners, abstracts) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Conference(uuid, name, owners, abstracts);
        }

        var self = tools.inherit(this, Model, uuid);

        self.name = name || null;
        self.owners = owners || null;
        self.abstracts = abstracts || null;

    }

    Conference.fromObject = function(obj) {
        return Model.fromObject(obj, Conference);
    };

    Conference.fromArray = function(array) {
        return Model.fromArray(array, Conference.fromObject);
    };

    /**
     * Model for authors.
     *
     * @param {string} [uuid]
     * @param {string} [mail]
     * @param {string} [firstName]
     * @param {string} [middleName]
     * @param {string} [lastName]
     * @param {string} [affiliations]   Array with affiliation uuids.
     *
     * @returns {Author}
     * @constructor
     * @public
     */
    function Author(uuid, mail, firstName, middleName, lastName, affiliations) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Author(uuid, mail, firstName, middleName, lastName, affiliations);
        }

        var self = tools.inherit(this, Model, uuid);

        self.mail = mail || null;
        self.firstName = firstName || null;
        self.middleName = middleName || null;
        self.lastName = lastName || null;

        self.affiliations = affiliations || null;
    }

    Author.fromObject = function(obj) {
        return Model.fromObject(obj, Author);
    };

    Author.fromArray = function(array) {
        return Model.fromArray(array, Author.fromObject);
    };

    /**
     * Model for affiliation
     *
     * @param {string} [uuid]
     * @param {string} [address]
     * @param {string} [country]
     * @param {string} [department]
     * @param {string} [name]
     * @param {string} [section]
     *
     * @returns {Affiliation}
     * @constructor
     * @public
     */
    function Affiliation(uuid, address, country, department, name, section) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Affiliation(uuid, address, country, department, name, section);
        }

        var self = tools.inherit(this, Model, uuid);

        self.address = address || null;
        self.country = country || null;
        self.department = department || null;
        self.name = name || null;
        self.section = section || null;

    }

    Affiliation.fromObject = function(obj) {
        return Model.fromObject(obj, Affiliation);
    };

    Affiliation.fromArray = function(array) {
        return Model.fromArray(array, Affiliation.fromObject);
    };

    /**
     * Model for figure.
     *
     * @param {string} [uuid]
     * @param {string} [name]
     * @param {string} [caption]
     * @param {string} [file]       URL to the image file.
     *
     * @returns {Figure}
     * @constructor
     * @public
     */
    function Figure(uuid, name, caption, file) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Figure(uuid, name, caption, file);
        }

        var self = tools.inherit(this, Model, uuid);

        self.name = name || null;
        self.caption = caption || null;
        self.file = caption || null;

    }

    Figure.fromObject = function(obj) {
        return Model.fromObject(obj, Figure);
    };

    Figure.fromArray = function(array) {
        return Model.fromArray(array, Figure.fromObject);
    };

    /**
     * Model for reference.
     *
     * @param {string} [uuid]
     * @param {string} [authors]
     * @param {string} [title]
     * @param {string} [year]
     * @param {string} [doi]
     *
     * @returns {Reference}
     * @constructor
     * @public
     */
    function Reference(uuid, authors, title, year, doi) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Reference();
        }

        var self = tools.inherit(this, Model, uuid);

        self.authors = authors || null;
        self.title = title || null;
        self.year = year || null;
        self.doi = doi || null;

    }

    Reference.fromObject = function(obj) {
        return Model.fromObject(obj, Reference);
    };

    Reference.fromArray = function(array) {
        return Model.fromArray(array, Reference.fromObject);
    };


    /**
     * Model for abstracts.
     *
     * @param {string} [uuid]
     * @param {string} [title]
     * @param {string} [topic]
     * @param {string} [text]
     * @param {string} [doi]
     * @param {string} [conflictOfInterest]
     * @param {string} [acknowledgements]
     * @param {string} [owners]     URL to abstract owners.
     * @param {boolean} [approved]
     * @param {boolean} [published]
     * @param {Figure} [figure]
     * @param {Array} [authors]
     * @param {Array} [affiliations]
     * @param {Array} [references]
     *
     * @returns {Abstract}
     * @constructor
     * @public
     */
    function Abstract(uuid, title, topic, text, doi, conflictOfInterest, acknowledgements,
                      owners, approved, published, figure, authors, affiliations,
                      references) {

        if (tools.isGlobalOrUndefined(this)) {
            return new Abstract(uuid, title, topic, text, doi, conflictOfInterest,
                                acknowledgements, approved, published, owners, figure,
                                authors, affiliations, references);
        }

        var self = tools.inherit(this, Model, uuid);

        self.title = title || null;
        self.topic = topic || null;
        self.text = text || null;
        self.doi = doi || null;
        self.conflictOfInterest = conflictOfInterest || null;
        self.acknowledgements = acknowledgements || null;
        self.owners = owners || null;
        self.approved = approved || false;
        self.published = published || false;
        self.figure = figure || null;
        self.authors = authors || [];
        self.affiliations = affiliations || [];
        self.references = references || [];


        self.toObject = function() {
            var prop,
                obj = {};

            for (prop in self) {
                if (self.hasOwnProperty(prop)) {
                    var value = self[prop];

                    switch(prop) {
                        case "figure":
                            obj[prop] = self.figure.toObject();
                            break;
                        case "authors":
                            obj.authors = [];
                            self.authors.forEach(appendAuthor);
                            break;
                        case "affiliations":
                            obj.affiliations = [];
                            self.affiliations.forEach(appendAffiliation);
                            break;
                        case "references":
                            obj.references = [];
                            self.references.forEach(appendReference);
                            break;
                        default:
                            if (tools.type(value) !== "function") {
                                obj[prop] = value;
                            }
                    }
                }
            }

            function appendAuthor(model) {
                obj.authors.append(model.toObject());
            }

            function appendAffiliation(model) {
                obj.affiliations.append(model.toObject());
            }

            function appendReference(model) {
                obj.references.append(model.toObject());
            }
        };

    }

    Abstract.fromObject = function(obj) {
        var prop,
            target = new Abstract();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, obj);

                switch(prop) {
                    case "figure":
                        target.figure = Figure.fromObject(value);
                        break;
                    case "authors":
                        target.authors = Author.fromArray(value);
                        break;
                    case "affiliations":
                        target.affiliations = Affiliation.fromArray(value);
                        break;
                    case "references":
                        target.references = Reference.fromArray(value);
                        break;
                    default:
                        if (tools.type(value) !== "function") {
                            target[prop] = value;
                        }
                }
            }
        }

        return target;
    };

    Abstract.fromArray = function(array) {
        return Model.fromArray(array, Abstract.fromObject);
    };



    return {
        Conference: Conference,
        Author: Author,
        Affiliation: Affiliation,
        Figure: Figure,
        Reference: Reference,
        Abstract: Abstract
    };
});
