/**
 * Module for misc utility functions
 * @module {util/tools}
 */
define(["util/tools"], function(tools) {
    "use strict";

    /**
     * Get a property value from another object.
     *
     * @param name {string} The name of a property.
     * @param source {object}
     * @returns {*|null}
     * @private
     */
    function valFromProperty(name, source) {
        var name_under = tools.toUnderscore(name),
            value = source[name_under] || source[name] || null;

        if (tools.type(value) === "function") {
            value = null;
        }

        return value;
    }

    /**
     * Base class for models
     *
     * @param {string} [uuid] The uuid of the model.
     * @returns {Model}
     * @constructor
     */
    function Model(uuid) {

        if (tools.isGlobal(this)) {
            return new Model(uuid);
        }

        var self = this;

        self.uuid = uuid || null;

    }

    Model.fromObject = function(source, target) {
        var prop;

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                target[prop] = valFromProperty(prop, source);
            }
        }

        return target;
    };

    Model.fromArray = function(array, factory) {
        var created = [];

        array.forEach(function(obj) {
           created.append(factory(obj));
        });

        return created;
    };

    /**
     * Model for conference
     *
     * @param uuid
     * @param name
     * @param owners
     * @param abstracts
     * @returns {Conference}
     * @constructor
     */
    function Conference(uuid, name, owners, abstracts) {

        if (tools.isGlobal(this)) {
            return new Conference();
        }

        var self = tools.inherit(this, Model, uuid);

        self.name = name || null;
        self.owners = owners || null;
        self.abstracts = abstracts || null;

    }

    Conference.fromObject = function(obj) {
        return Model.fromObject(obj, new Conference());
    };

    Conference.fromArray = function(array) {
        return Model.fromArray(array, Conference.fromObject);
    };

    /**
     * Model for authors.
     *
     * @param uuid
     * @param mail
     * @param firstName
     * @param middleName
     * @param lastName
     * @param affiliations
     * @returns {Author}
     * @constructor
     */
    function Author(uuid, mail, firstName, middleName, lastName, affiliations) {

        if  (tools.isGlobal(this)) {
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
        return Model.fromObject(obj, new Author());
    };

    Author.fromArray = function(array) {
        return Model.fromArray(array, Author.fromObject);
    };

    return {
        Conference: Conference,
        Author: Author
    };
});
