/**
 * Module for misc utility functions
 * @module {models}
 */
define(["lib/tools", "lib/accessors",  "moment", "knockout"], function(tools, acc, moment, ko) {
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
        if (source === null) {
            return null;
        }

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
     * Convert a string to Boolean or Number if it represents one of them.
     *
     * @param str       The string to convert.
     *
     * @returns {string|Boolean|Number}
     */
    function toType(str) {
        var val = str;

        if (tools.type(str) === "string") {
            str = str.trim();
            if (str === "") {
                val = null;
            } else if (str.match(/^(\+|-)?((\d+(\.\d+)?)|(\.\d+))$/)) {
                val = Number(str);
            } else if (str.match(/^(true|false)$/)) {
                val = (str === "true");
            }
        }

        return val;
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
                       obj[prop] = toType(value);
                   } else if (tools.functionName(value) === "observable") {
                       obj[prop] = toType(self[prop]());
                   }
               }
            }

            return obj;
        };

        /**
         * Makes all properties of the model observable, that are either accessors or
         * arrayAccessors or specified in by properties parameter.
         *
         * @param {Array} [properties]  List of property names to convert into observable
         *                              values, if specified only properties specified
         *                              here are converted.
         */
        self.makeObservable = function(properties) {
            var prop,
                accessors = ["accessor", "arrayAccessor"];

            for (prop in self) {
                if (self.hasOwnProperty(prop)) {
                    var value = self[prop],
                        vname = value ? tools.functionName(value) : null,
                        convert = false;

                    if (properties && properties.indexOf(prop) >= 0) {
                        convert = true;
                    } else if (!properties && accessors.indexOf(vname) >= 0) {
                        convert = true;
                    }

                    if (convert) {
                        if (tools.type(value) === "function") {
                            if (vname === "accessor") {
                                self[prop] = ko.observable(self[prop]());
                            } else if (vname === "arrayAccessor") {
                                self[prop] = ko.observableArray(self[prop]());
                            }
                        } else if (tools.type(value) === "array") {
                            self[prop] = ko.observableArray(value);
                        } else {
                            self[prop] = ko.observable(value);
                        }
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
        self.toJSON = function(indention) {
            if (indention === undefined) {
                indention = indention || 4;
            }
            var obj = self.toObject();
            return JSON.stringify(obj, null, indention);
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

                if (value !== null) {
                    if (tools.type(target[prop]) !== "function") {
                        target[prop] = value;
                    } else if (tools.functionName(target[prop]) === "observable") {
                        target[prop](value);
                    }
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
           created.push(factory(obj));
        });

        return created;
    };

    /**
     * Model for AbstractGroup
     *
     * @param {string} [uuid]
     * @param {number} [prefix]
     * @param {string} [name]
     * @param {string} [short]
     *
     * @returns {AbstractGroup}
     * @constructor
     * @public
     */
    function AbstractGroup(uuid, prefix, name, short) {
        if (!(this instanceof  AbstractGroup)) {
            return new AbstractGroup(uuid, prefix, name, short);
        }

        var self = tools.inherit(this, Model, uuid);

        self.prefix = prefix || 0;
        self.name = name || null;
        self.short = short || null;
    }

    AbstractGroup.fromObject = function(obj) {
        return Model.fromObject(obj, AbstractGroup);
    };

    AbstractGroup.fromArray = function(array) {
        return Model.fromArray(array, AbstractGroup.fromObject);
    };

    /**
     * Model for conference
     *
     * @returns {Conference}
     * @constructor
     * @public
     */
    function Conference(uuid, name, short, group, cite, link, isOpen, isPublished, isActive, hasPresentationPrefs,
                        groups, start, end, deadline, imageUrls, infoTexts, iOSApp, banner, geo, schedule, info, owners, abstracts, topics,
                        mAbsLeng, mFigs) {
        if (!(this instanceof Conference)) {
            return new Conference(uuid, name, short, group, cite, link, isOpen, isPublished, isActive,
                                  hasPresentationPrefs, groups, start, end, deadline, imageUrls, infoTexts, iOSApp,
                                  banner, geo, schedule, info, owners, abstracts, topics, mAbsLeng, mFigs);
        }

        var self = tools.inherit(this, Model, uuid);

        self.name = name || null;
        self.short = short || null;
        self.cite = cite || null;
        self.group = group || null;
        self.link = link || null;
        self.isOpen = isOpen || false;
        self.isPublished = isPublished || false;
        self.isActive = isActive || false;
        self.hasPresentationPrefs = hasPresentationPrefs || false;
        self.groups = groups || [];
        self.start = start || null;
        self.end = end || null;
        self.deadline = deadline || null;
        self.imageUrls = imageUrls || null;
        self.infoTexts = infoTexts || null;
        self.iOSApp = iOSApp || null;
        self.banner = banner || null;
        self.geo = geo || null;
        self.schedule = schedule || null;
        self.info = info || null;
        self.owners = owners || [];
        self.abstracts = abstracts || [];
        self.topics = topics || [];
        self.mAbsLeng = mAbsLeng || null;
        self.mFigs = mFigs || [];

        self.getGroupById = function(groupId) {
            var foundGroup = null;
            for (var i = 0; i < self.groups.length; i++) {
                var curGroup = self.groups[i];
                if (curGroup.prefix === groupId) {
                    foundGroup = curGroup;
                    break;
                }
            }

            return foundGroup;
        };

        self.formatSortId = function(sortId) {
            if (sortId === 0) {
                return "";
            }

            var aid =  sortId & 0xFFFF;
            var gid = (sortId & 0xFFFF0000) >> 16;

            var prefix = "U";
            var g = self.getGroupById(gid);
            if (g !== null) {
                prefix = g.short;
            }

            return prefix + "&nbsp;" + aid;
        };

        self.formatCitation = function(abstract) {
            var year = moment(self.start).year();
            return abstract.formatAuthorsCitation() + " (" + year + ") " + abstract.title + ". " + self.name + ".";
        };

        self.formatCopyright = function(abstract) {
            var year = moment(self.start).year();
            return "© (" + year + ") " + abstract.formatAuthorsCitation();
        };

        self.toObject = function() {
            var prop,
                obj = {};

            for (prop in self) {
                if (self.hasOwnProperty(prop)) {
                    var value = self[prop];
                    var plain = null;

                    // Trim whitespaces before saving
                    var trimValueProperties = ["name", "short", "cite", "group", "link", "description"];
                    if (trimValueProperties.includes(prop) && value() !== null && value !== undefined) {
                        value(value().trim());
                    }

                    if (prop === "topics" && value() !== null && value !== undefined) {
                        var cleanTopics = [];
                        value().forEach(function (model) {
                            var curr = model.trim();
                            if (curr.length > 0) {
                                cleanTopics.push(curr);
                            }
                        });
                        value(cleanTopics);
                    }

                    if (tools.type(value) !== "function") {
                        plain = value;
                    } else if (tools.functionName(value) === "observable") {
                        plain = self[prop]();
                    }

                    if (prop === "groups") {
                        obj.groups = [];
                        plain.forEach(appendGroup);
                    } else if (prop === "iOSApp" && plain) {
                        // don't convert iOSApp app ids, which for now seem
                        //  to be numbers, but leave them as strings
                        obj[prop] = plain;
                    } else if (plain) {
                        obj[prop] = toType(plain);
                    }
                }
            }

            function appendGroup(model) {
                // Cleanup leading and trailing whitespaces
                if (model.short() !== null && model.short() !== undefined) {
                    model.short(model.short().trim());
                }
                if (model.name() !== null && model.name() !== undefined) {
                    model.name(model.name().trim());
                }

                obj.groups.push(model.toObject());
            }

            return obj;
        };
    }

    Conference.fromObject = function(obj) {
        var prop,
            target = new Conference();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, obj);

                switch (prop) {
                    case "groups":
                        target.groups = AbstractGroup.fromArray(value);
                        break;
                    default:
                        if (tools.type(target[prop]) !== "function") {
                            target[prop] = value;
                        }
                }
            }
        }

        return target;
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
        if (!(this instanceof Author)) {
            return new Author(uuid, mail, firstName, middleName, lastName, affiliations);
        }

        var self = tools.inherit(this, Model, uuid);

        self.mail = mail || null;
        self.firstName = firstName || null;
        self.middleName = middleName || null;
        self.lastName = lastName || null;

        self.affiliations = affiliations || [];

        self.formatName = function() {
            var middle = self.middleName ? self.middleName + " " : "";
            return self.firstName + " " + middle + self.lastName;
        };

        self.formatAffiliations = function() {
            var formatted = [];

            self.affiliations.forEach(function(pos, i) {
                formatted[i] = pos + 1;
            });

            return formatted.sort().join(", ");
        };

        self.formatCitation = function() {
          var res = self.lastName + " ";
            res += self.makeInitials(self.firstName);
            res += self.makeInitials(self.middleName);
            return res;
        };

        self.makeInitials = function(name) {
            if (!name) {
                return "";
            }

           return name.split(" ").map(function(x) { return x[0]; }).join("");
        };
    }

    Author.fromObject = function(obj) {
        return Model.fromObject(obj, Author);
    };

    Author.fromArray = function(array) {
        return Model.fromArray(array, Author.fromObject);
    };

    /**
     * Observable model for authors.
     *
     * @param {string} [uuid]
     * @param {string} [mail]
     * @param {string} [firstName]
     * @param {string} [middleName]
     * @param {string} [lastName]
     * @param {string} [affiliations]   Array with affiliation uuids.
     *
     * @returns {ObservableAuthor}
     * @constructor
     * @public
     */
    function ObservableAuthor(uuid, mail, firstName, middleName, lastName, affiliations) {
        if (!(this instanceof ObservableAuthor)) {
            return new ObservableAuthor(uuid, mail, firstName, middleName, lastName, affiliations);
        }

        var self = tools.inherit(this, Model, uuid);

        self.mail = ko.observable(mail || null);
        self.firstName = ko.observable(firstName || null);
        self.middleName = ko.observable(middleName || null);
        self.lastName = ko.observable(lastName || null);

        self.affiliations = ko.observableArray(affiliations || []);

        self.formatName = function() {
            var middle = self.middleName() ? self.middleName() + " " : "";
            return self.firstName() + " " + middle + self.lastName();
        };

        self.formatAffiliations = function() {
            var formatted = [];

            self.affiliations().forEach(function(pos, i) {
                formatted[i] = pos + 1;
            });

            return formatted.sort().join(", ");
        };
    }

    ObservableAuthor.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableAuthor);
    };

    ObservableAuthor.fromArray = function(array) {
        return Model.fromArray(array, ObservableAuthor.fromObject);
    };

    /**
     * Model for affiliation
     *
     * @param {string} [uuid]
     * @param {string} [address]
     * @param {string} [country]
     * @param {string} [department]
     * @param {string} [section]
     *
     * @returns {Affiliation}
     * @constructor
     * @public
     */
    function Affiliation(uuid, address, country, department, section) {
        if (!(this instanceof  Affiliation)) {
            return new Affiliation(uuid, address, country, department, section);
        }

        var self = tools.inherit(this, Model, uuid);

        self.address = address || null;
        self.country = country || null;
        self.department = department || null;
        self.section = section || null;

        self.format = function() {
            var str = (self.department || "")
                .concat(self.section ? ", " + self.section : "")
                .concat(self.address ? ", " + self.address : "")
                .concat(self.country ? ", " + self.country : "");

            if (str.indexOf(", ") === 0) {
                str = str.slice(2, str.length);
            }

            return str;
        };
    }

    Affiliation.fromObject = function(obj) {
        return Model.fromObject(obj, Affiliation);
    };

    Affiliation.fromArray = function(array) {
        return Model.fromArray(array, Affiliation.fromObject);
    };

    /**
     * Obervable model for affiliation
     *
     * @param {string} [uuid]
     * @param {string} [address]
     * @param {string} [country]
     * @param {string} [department]
     * @param {string} [section]
     *
     * @returns {ObservableAffiliation}
     * @constructor
     * @public
     */
    function ObservableAffiliation(uuid, address, country, department, section) {
        if (!(this instanceof  ObservableAffiliation)) {
            return new ObservableAffiliation(uuid, address, country, department, section);
        }

        var self = tools.inherit(this, Model, uuid);

        self.address = ko.observable(address || null);
        self.country = ko.observable(country || null);
        self.department = ko.observable(department || null);
        self.section = ko.observable(section || null);

        self.format = function() {
            var str = (self.department() || "")
                .concat(self.section() ? ", " + self.section() : "")
                .concat(self.address() ? ", " + self.address() : "")
                .concat(self.country() ? ", " + self.country() : "");

            if (str.indexOf(", ") === 0) {
                str = str.slice(2, str.length);
            }

            return str;
        };
    }

    ObservableAffiliation.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableAffiliation);
    };

    ObservableAffiliation.fromArray = function(array) {
        return Model.fromArray(array, ObservableAffiliation.fromObject);
    };

    /**
     * Model for figure.
     *
     * @param {string} [uuid]
     * @param {string} [caption]
     * @param {string} [URL]       URL to the image URL.
     *
     * @returns {Figure}
     * @constructor
     * @public
     */
    function Figure(uuid, caption, URL) {
        if (!(this instanceof Figure)) {
            return new Figure(uuid, caption, URL);
        }

        var self = tools.inherit(this, Model, uuid);

        self.caption = caption || null;
        self.URL = URL || null;
    }

    Figure.fromObject = function(obj) {
        return Model.fromObject(obj, Figure);
    };

    Figure.fromArray = function(array) {
        return Model.fromArray(array, Figure.fromObject);
    };

    /**
     * Observable model for figure.
     *
     * @param {string} [uuid]
     * @param {string} [caption]
     * @param {string} [URL]       URL to the image URL.
     *
     * @returns {ObservableFigure}
     * @constructor
     * @public
     */
    function ObservableFigure(uuid, caption, URL) {
        if (!(this instanceof ObservableFigure)) {
            return new ObservableFigure(uuid, caption, URL);
        }

        var self = tools.inherit(this, Model, uuid);

        self.caption = ko.observable(caption || null);
        self.URL = ko.observable(URL || null);
    }

    ObservableFigure.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableFigure);
    };

    ObservableFigure.fromArray = function(array) {
        return Model.fromArray(array, ObservableFigure.fromObject);
    };

    /**
     * Model for banner.
     *
     * @param {string} [uuid]
     * @param {string} [URL]       URL to the image URL.
     *
     * @returns {Banner}
     * @constructor
     * @public
     */
    function Banner(uuid, URL) {
        if (!(this instanceof Banner)) {
            return new Banner(uuid, URL);
        }

        var self = tools.inherit(this, Model, uuid);

        self.URL = URL || null;
    }

    Banner.fromObject = function(obj) {
        return Model.fromObject(obj, Banner);
    };

    Banner.fromArray = function(array) {
        return Model.fromArray(array, Banner.fromObject);
    };

    /**
     * Observable model for banner.
     *
     * @param {string} [uuid]
     * @param {string} [URL]       URL to the image URL.
     *
     * @returns {ObservableBanner}
     * @constructor
     * @public
     */
    function ObservableBanner(uuid, URL) {
        if (!(this instanceof ObservableBanner)) {
            return new ObservableBanner(uuid, URL);
        }

        var self = tools.inherit(this, Model, uuid);

        self.URL = ko.observable(URL || null);
    }

    ObservableBanner.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableBanner);
    };

    ObservableBanner.fromArray = function(array) {
        return Model.fromArray(array, ObservableBanner.fromObject);
    };

    /**
     * Model for reference.
     *
     * @param {string} [uuid]
     * @param {string} [text]
     * @param {string} [link]
     * @param {string} [doi]
     *
     * @returns {Reference}
     * @constructor
     * @public
     */
    function Reference(uuid, text, link, doi) {
        if (!(this instanceof Reference)) {
            return new Reference();
        }

        var self = tools.inherit(this, Model, uuid);

        self.text = text || null;
        self.link = link || null;
        self.doi = doi || null;

        self.format = function() {
            var text = self.text || self.link;
            var html = self.link ? '<a target="_blank" href="' + self.link  + '">' + text + "</a>" : text;

            if (self.doi) {
                var dx = self.doi;
                html += ', <a target="_blank" href="http://dx.doi.org/' + dx + '">' + dx + "</a>";
            }

            return html;
        };
    }

    Reference.fromObject = function(obj) {
        return Model.fromObject(obj, Reference);
    };

    Reference.fromArray = function(array) {
        return Model.fromArray(array, Reference.fromObject);
    };

    /**
     * Observable model for reference.
     *
     * @param {string} [uuid]
     * @param {string} [text]
     * @param {string} [link]
     * @param {string} [doi]
     *
     * @returns {ObservableReference}
     * @constructor
     * @public
     */
    function ObservableReference(uuid, text, link, doi) {
        if (!(this instanceof ObservableReference)) {
            return new ObservableReference();
        }

        var self = tools.inherit(this, Model, uuid);

        self.text = ko.observable(text || null);
        self.link = ko.observable(link || null);
        self.doi = ko.observable(doi || null);

        self.format = function() {
            var text = self.text() || self.link();
            var html = self.link() ? '<a target="_blank" href="' + self.link()  + '">' + text + "</a>" : text;

            if (self.doi()) {
                var dx = self.doi();
                html += ', <a target="_blank" href="http://dx.doi.org/' + dx + '">' + dx + "</a>";
            }

            return html;
        };
    }

    ObservableReference.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableReference);
    };

    ObservableReference.fromArray = function(array) {
        return Model.fromArray(array, ObservableReference.fromObject);
    };

    /**
     * Model for abstracts.
     *
     * @param {string} [uuid]
     * @param {number} [sortId]
     * @param {string} [title]
     * @param {string} [topic]
     * @param {string} [text]
     * @param {string} [doi]
     * @param {string} [conflictOfInterest]
     * @param {string} [acknowledgements]
     * @param {Boolean} [isTalk]
     * @param {string} [reasonForTalk]
     * @param {string} [owners]     URL to abstract owners.
     * @param {string} [state]
     * @param {Array} [figures]
     * @param {Array} [authors]
     * @param {Array} [affiliations]
     * @param {Array} [references]
     *
     * @returns {Abstract}
     * @constructor
     * @public
     */
    function Abstract(uuid, sortId, title, topic, text, doi, conflictOfInterest,
                      acknowledgements, isTalk, reasonForTalk, owners, state, figures,
                      authors, affiliations, references, abstrTypes) {
        if (!(this instanceof Abstract)) {
            return new Abstract(uuid, sortId, title, topic, text, doi, conflictOfInterest,
                                acknowledgements, isTalk, reasonForTalk, owners, state,
                                figures, authors, affiliations, references, abstrTypes);
        }

        var self = tools.inherit(this, Model, uuid);

        self.sortId = sortId || 0;
        self.title = title || null;
        self.topic = topic || null;
        self.text = text || null;
        self.doi = doi || null;
        self.conflictOfInterest = conflictOfInterest || null;
        self.acknowledgements = acknowledgements || null;
        self.isTalk = isTalk || false;
        self.reasonForTalk = reasonForTalk || null;
        self.owners = owners || null;
        self.state = state || "InPreparation";
        self.figures = figures || [];
        self.authors = authors || [];
        self.affiliations = affiliations || [];
        self.references = references || [];
        self.abstrTypes = abstrTypes || [];

        self.paragraphs = function() {
            var para = [];

            if (self.text) {
                para = self.text.split("\n");
            }

            return para;
        };

        self.formatType = function() {
          return self.isTalk === true ? "Talk" : "Poster";
        };

        self.doiLink = function() {
            return self.doi ? "http://doi.org/" + self.doi : null;
        };

        self.formatAuthorsCitation = function() {
            var res = "";
            for (var i = 0; i < self.authors.length; i++) {
                if (i != 0) {
                    res += ", ";
                }
                res += self.authors[i].formatCitation();
            }
            return res;
        };

        self.toObject = function() {
            var prop,
                obj = {};

            for (prop in self) {
                if (self.hasOwnProperty(prop)) {
                    var value = self[prop];

                    switch (prop) {
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
                        case "abstrTypes":
                            obj.abstrTypes = [];
                            self.abstrTypes.forEach(appendAbstrTypes);
                            break;
                        case "owners":
                            break;
                        case "figures":
                            break;
                        default:
                            if (tools.type(value) !== "function") {
                                obj[prop] = toType(value);
                            }
                    }
                }
            }

            function appendAuthor(model) {
                obj.authors.push(model.toObject());
            }

            function appendAffiliation(model) {
                obj.affiliations.push(model.toObject());
            }

            function appendReference(model) {
                obj.references.push(model.toObject());
            }

            function appendAbstrTypes(model) {
                obj.abstrTypes.push(model.toObject());
            }

            return obj;
        };

        self.hasTypeWuuid = function (uuid) {
            return true;
        };
    }

    Abstract.fromObject = function(obj) {
        var prop,
            target = new Abstract();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, obj);

                switch (prop) {
                    case "figures":
                        target.figures = Figure.fromArray(value);
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
                    case "abstrTypes":
                        target.abstrTypes = AbstractGroup.fromArray(value);
                        break;
                    default:
                        if (tools.type(target[prop]) !== "function") {
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

    /**
     * Observable model for abstracts.
     *
     * @param {string} [uuid]
     * @param {number} [sortId]
     * @param {string} [title]
     * @param {string} [topic]
     * @param {string} [text]
     * @param {string} [doi]
     * @param {string} [conflictOfInterest]
     * @param {string} [acknowledgements]
     * @param {Boolean} [isTalk]
     * @param {string} [reasonForTalk]
     * @param {string} [owners]     URL to abstract owners.
     * @param {string} [state]
     * @param {Array} [figures]
     * @param {Array} [authors]
     * @param {Array} [affiliations]
     * @param {Array} [references]
     *
     * @returns {ObservableAbstract}
     * @constructor
     * @public
     */
    function ObservableAbstract(uuid, sortId, title, topic, text, doi, conflictOfInterest,
                                acknowledgements, isTalk, reasonForTalk, owners, state, figures,
                                authors, affiliations, references, abstrTypes) {
        if (!(this instanceof ObservableAbstract)) {
            return new ObservableAbstract(uuid, sortId, title, topic, text, doi, conflictOfInterest,
                                          acknowledgements, isTalk, reasonForTalk, owners, state,
                                          figures, authors, affiliations, references, abstrTypes);
        }

        var self = tools.inherit(this, Model, uuid);

        self.title = ko.observable(title || null);
        self.topic = ko.observable(topic || null);
        self.text = ko.observable(text || null);
        self.doi = ko.observable(doi || null);
        self.conflictOfInterest = ko.observable(conflictOfInterest || null);
        self.acknowledgements = ko.observable(acknowledgements || null);
        self.isTalk = ko.observable(isTalk || false);
        self.reasonForTalk = ko.observable(reasonForTalk || null);
        self.owners = ko.observable(owners || null);
        self.state = ko.observable(state || "InPreparation");
        self.figures = ko.observableArray(figures || []);
        self.authors = ko.observableArray(authors || []);
        self.affiliations = ko.observableArray(affiliations || []);
        self.references = ko.observableArray(references || []);
        self.abstrTypes = ko.observableArray(abstrTypes || []);

        this.isTalk.computed = ko.computed({
            "read": function() {
                return self.isTalk().toString();
            },
            "write": function(val) {
                val = (val === "true");
                if (!val) {
                    self.reasonForTalk(null);
                }
                self.isTalk(val);
            },
            owner: this
        });

        self.indexedAuthors = ko.computed(
            function() {
                var indexed = [];

                self.authors().forEach(function(author, index) {
                    indexed.push({author: author, index: index});
                });

                return indexed;
            },
            self
        );

        self.paragraphs = function() {
            var para = [];

            if (self.text()) {
                para = self.text().split("\n");
            }

            return para;
        };

        self.toObject = function() {
            var prop,
                obj = {};

            for (prop in self) {
                if (self.hasOwnProperty(prop)) {
                    var value = self[prop];

                    switch (prop) {
                        case "title":
                            if (value() !== undefined && value() !== null) {
                                value(value().trim());
                            }
                            obj[prop] = toType(value());
                            break;
                        case "text":
                            if (value() !== undefined && value() !== null) {
                                value(value().trim());
                            }
                            obj[prop] = toType(value());
                            break;
                        case "acknowledgements":
                            if (value() !== undefined && value() !== null) {
                                value(value().trim());
                            }
                            obj[prop] = toType(value());
                            break;
                        case "authors":
                            obj.authors = [];
                            self.authors().forEach(appendAuthor);
                            break;
                        case "affiliations":
                            obj.affiliations = [];
                            self.affiliations().forEach(appendAffiliation);
                            break;
                        case "references":
                            obj.references = [];
                            self.references().forEach(appendReference);
                            break;
                        case "abstrTypes":
                            obj.abstrTypes = [];
                            self.abstrTypes().forEach(appendAbstrTypes);
                            break;
                        case "owners":
                            break;
                        case "figures":
                            break;
                        default:
                            if (tools.type(value) !== "function") {
                                obj[prop] = toType(value);
                            } else if (tools.functionName(value) === "observable") {
                                obj[prop] = toType(value());
                            }
                    }
                }
            }

            function appendAuthor(model) {
                // Post processing, remove all leading and trailing whitespaces.
                if (model.firstName() !== null && model.firstName() !== undefined) {
                    model.firstName(model.firstName().trim());
                }
                if (model.lastName() !== null && model.lastName() !== undefined) {
                    model.lastName(model.lastName().trim());
                }
                if (model.middleName() !== null && model.middleName() !== undefined) {
                    model.middleName(model.middleName().trim());
                }
                if (model.mail() !== null && model.mail() !== undefined) {
                    model.mail(model.mail().trim());
                }
                obj.authors.push(model.toObject());
            }

            function appendAffiliation(model) {
                // Post processing, remove all leading and trailing whitespaces.
                if (model.address() !== null && model.address() !== undefined) {
                    model.address(model.address().trim());
                }
                if (model.department() !== null && model.department() !== undefined) {
                    model.department(model.department().trim());
                }
                if (model.country() !== null && model.country() !== undefined) {
                    model.country(model.country().trim());
                }
                if (model.section() !== null && model.section() !== undefined) {
                    model.section(model.section().trim());
                }
                obj.affiliations.push(model.toObject());
            }

            function appendReference(model) {
                // Post processing, remove all leading and trailing whitespaces.
                if (model.text() !== null && model.text() !== undefined) {
                    model.text(model.text().trim());
                }
                if (model.link() !== null && model.link() !== undefined) {
                    model.link(model.link().trim());
                }
                if (model.doi() !== null && model.doi() !== undefined) {
                    model.doi(model.doi().trim());
                }

                // Post process doi. Remove any leading doi link parts that hinder rendering later on.
                var doiValue = model.doi();
                if (doiValue !== null && doiValue !== undefined) {
                    // First remove leading http or https
                    doiValue = doiValue.replace(/^https:\/\//, "").replace(/http:\/\//, "");

                    // Then search and replace DOI variant hierarchical URL parts
                    var checkA = /^dx.doi.org\//;
                    var checkB = /^doi.org\//;
                    var checkC = /^doi:/;

                    if (checkA.test(doiValue)) {
                        model.doi(doiValue.replace(checkA, ""));
                    } else if (checkB.test(doiValue)) {
                        model.doi(doiValue.replace(checkB, ""));
                    } else if (checkC.test(doiValue)) {
                        model.doi(doiValue.replace(checkC, ""));
                    }
                }

                obj.references.push(model.toObject());
            }

            function appendAbstrTypes(model) {
                obj.abstrTypes.push(model.toObject());
            }

            return obj;
        };
    }

    ObservableAbstract.fromObject = function(obj) {
        var prop,
            target = new ObservableAbstract();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, obj);

                switch (prop) {
                    case "figures":
                        target.figures(Figure.fromArray(value));
                        break;
                    case "authors":
                        target.authors(
                            ObservableAuthor.fromArray(value)
                        );
                        break;
                    case "affiliations":
                        target.affiliations(
                            ObservableAffiliation.fromArray(value)
                        );
                        break;
                    case "references":
                        target.references(ObservableReference.fromArray(value));
                        break;

                    case "abstrTypes":
                        target.abstrTypes(ObservableAbstractGroup.fromArray(value));
                        break;
                    default:
                        if (tools.type(target[prop]) !== "function") {
                            target[prop] = value;
                        } else if (tools.functionName(target[prop]) === "observable") {
                            target[prop](value);
                        }
                }
            }
        }

        return target;
    };

    ObservableAbstract.fromArray = function(array) {
        return Model.fromArray(array, ObservableAbstract.fromObject);
    };

    /**
     * Observable model for user accounts.
     *
     * @param {string} [uuid]
     * @param {string} [mail]
     *
     * @returns {ObservableAccount}
     * @constructor
     * @public
     */
    function ObservableAccount(uuid, mail, fullName, ctime) {
        if (!(this instanceof ObservableAccount)) {
            return new ObservableAccount(uuid, mail, fullName, ctime);
        }

        var self = tools.inherit(this, Model, uuid);

        self.mail = ko.observable(mail || null);
        self.fullName = ko.observable(fullName || null);
        self.ctime = ko.observable(ctime || null);

        self.formatCtime = function() {
            if (self.ctime) {
                return moment(self.ctime()).format("YY/MM/DD");
            } else {
                return "";
            }
        };
    }

    ObservableAccount.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableAccount);
    };

    ObservableAccount.fromArray = function(array) {
        return Model.fromArray(array, ObservableAccount.fromObject);
    };

    /**
     * Observable model for AbstractGroups.
     *
     * @param {string} [uuid]
     * @param {number} [prefix]
     * @param {string} [name]
     * @param {string} [short]
     *
     * @returns {ObservableAbstractGroup}
     * @constructor
     * @public
     */
    function ObservableAbstractGroup(uuid, prefix, name, short) {
        // Why do i not have a self ???
        if (!(this instanceof ObservableAbstractGroup)) {
            return new ObservableAbstractGroup(uuid, prefix, name, short);
        }

        var self = tools.inherit(this, Model, uuid);

        self.prefix = ko.observable(prefix || null);
        self.name = ko.observable(name || null);
        self.short = ko.observable(short || null);
    }

    ObservableAbstractGroup.fromObject = function(obj) {
        return Model.fromObject(obj, ObservableAbstractGroup);
    };

    ObservableAbstractGroup.fromArray = function(array) {
        return Model.fromArray(array, ObservableAbstractGroup.fromObject);
    };

    /**
     * Model for DHTMLX Scheduler events.
     *
     * @param {string} [id]
     * @param {string} [text]
     * @param {string} [startDate]
     * @param {string} [endDate]
     * @param {object} [baseEvent]

     *
     * @returns {SchedulerEvent}
     * @constructor
     * @public
     */
    function SchedulerEvent (id, text, startDate, endDate, baseEvent) {
        if (!(this instanceof SchedulerEvent)) {
            return new SchedulerEvent(id, text, startDate, endDate, baseEvent);
        }

        var self = this;

        self.id = id || null;
        self.text = text || null;
        self.start_date = startDate || null;
        self.end_date = endDate || null;
        self.baseEvent = baseEvent || null;
        // used for split events to easily collapse them into the parent
        self.parentEvent = null;

        self.isTrack = function () {
            return self.baseEvent.hasOwnProperty("events");
        };

        self.isSession = function () {
            return self.baseEvent.hasOwnProperty("tracks");
        };

        self.isEvent = function () {
            return self.isSession() || self.isTrack();
        };

        self.getSplitEvents = function () {
            var splitEvents = [];
            if (self.isTrack()) {
                splitEvents = SchedulerEvent.fromArray(self.baseEvent.events);
            } else if (self.isSession()) {
                splitEvents = SchedulerEvent.fromArray(self.baseEvent.tracks);
            }
            var childID = [];
            var childIndex = 0;
            self.id.split(":").forEach(function (idComponent) {
                childID.push(parseInt(idComponent));
                // track where the first negative number occurs to use as the new index
                if (parseInt(idComponent) >= 0) {
                    childIndex++;
                }
            });
            // replace the corresponding part of the ID with the new index
            var childIDComponent = 0;
            splitEvents.forEach(function (element) {
              element.parentEvent = self;
              childID[childIndex] = childIDComponent++;
              element.id = childID.join(":");
            });
            return splitEvents;
        };
    }

    function Track (title, subtitle, chair, events) {
        if (!(this instanceof Track)) {
            return new Track(title, subtitle, chair, events);
        }

        var self = this;

        self.title = title || null;
        self.subtitle = subtitle || null;
        self.chair = chair || null;
        self.events = events || null;

        /*
         * Split this track into sub tracks spanning only a single day each.
         */
        self.splitByDays = function () {
          var subtracks = [];
          var start = self.getStart();
          var end = self.getEnd();
          var splitDate = start;
          while (Schedule.isIntermediateDate(start, end, splitDate)) {
              var splitEvents = [];
              self.events.forEach(function (event) {
                  if (Schedule.isSameDate(event.getStart(), splitDate)) {
                      splitEvents.push(event);
                  }
              });
              // do not process empty dates
              if (splitEvents.length > 0) {
                  subtracks.push(new Track(self.title, self.subtitle, self.chair, splitEvents));
              }
              // switch to the next day
              splitDate = new Date(splitDate.getTime() + 24 * 60 * 60 * 1000);
          }
          return subtracks;
        };

        // look at all the events to find the starting date of the track
        self.getStart = function () {
            var startingDate = null;

            self.events.forEach(function (e) {
                if (startingDate === null) {
                    startingDate = e.getStart();
                } else if (startingDate - e.getStart() > 0) {
                    startingDate = e.getStart();
                }
            });

            return startingDate;
        };

        // look at all the events to find the ending date of the track
        self.getEnd = function () {
            var endingDate = null;

            self.events.forEach(function (e) {
                if (endingDate === null) {
                    endingDate = e.getEnd();
                } else if (endingDate - e.getEnd() < 0) {
                    endingDate = e.getEnd();
                }
            });

            return endingDate;
        };
    }

    function Session (title, subtitle, tracks) {
        if (!(this instanceof Session)) {
            return new Session(title, subtitle, tracks);
        }

        var self = this;

        self.title = title || null;
        self.subtitle = subtitle || null;
        self.tracks = tracks || null;

        /*
         * Split this session into sub session spanning only a single day each.
         */
        self.splitByDays = function () {
            var subsessions = [];
            var start = self.getStart();
            var end = self.getEnd();
            var splitDate = start;
            // split all tracks of this session by days
            var preSplitTracks = [];
            self.tracks.forEach(function (track) {
                    track.splitByDays().forEach(function (t) {
                        preSplitTracks.push(t);
                    });
            });
            while (Schedule.isIntermediateDate(start, end, splitDate)) {
                var splitTracks = [];
                preSplitTracks.forEach(function (track) {
                    if (Schedule.isSameDate(track.getStart(), splitDate)) {
                        splitTracks.push(track);
                    }
                });
                // do not process empty dates
                if (splitTracks.length > 0) {
                    subsessions.push(new Session(self.title, self.subtitle, splitTracks));
                }
                // switch to the next day
                splitDate = new Date(splitDate.getTime() + 24 * 60 * 60 * 1000);
            }
            return subsessions;
        };

        // look at all the tracks to find the starting date of the session
        self.getStart = function () {
            var startingDate = null;

            self.tracks.forEach(function (t) {
                if (startingDate === null) {
                    startingDate = t.getStart();
                } else if (startingDate - t.getStart() > 0) {
                    startingDate = t.getStart();
                }
            });

            return startingDate;
        };

        // look at all the tracks to find the ending date of the session
        self.getEnd = function () {
            var endingDate = null;

            self.tracks.forEach(function (t) {
                if (endingDate === null) {
                    endingDate = t.getEnd();
                } else if (endingDate - t.getEnd() < 0) {
                    endingDate = t.getEnd();
                }
            });

            return endingDate;
        };
    }

    function Event (title, subtitle, start, end, date, location, authors, type, abstract) {
        if (!(this instanceof Event)) {
            return new Event(title, subtitle, start, end, date, location, authors, type, abstract);
        }

        var self = this;

        self.title = title || null;
        self.subtitle = subtitle || null;
        self.start = start || null;
        self.end = end || null;
        self.date = date || null;
        self.location = location || null;
        self.authors = authors || null;
        self.type = type || null;
        self.abstract = abstract || null;

        self.getStart = function () {
            // format year-month-day
            var ymd = self.date.split("-");
            // format hour:minute
            var time = ["0", "0"];
            if (self.start && self.start.length > 0) {
                time = self.start.split(":");
            }
            return new Date(parseInt(ymd[0]), parseInt(ymd[1]) - 1, parseInt(ymd[2]), parseInt(time[0]), parseInt(time[1]));
        };

        self.getEnd = function () {
            // format year-month-day
            var ymd = self.date.split("-");
            // format hour:minute
            var time = ["23", "59"];
            if (self.end && self.end.length > 0) {
                time = self.end.split(":");
            }
            return new Date(parseInt(ymd[0]), parseInt(ymd[1]) - 1, parseInt(ymd[2]), parseInt(time[0]), parseInt(time[1]));
        };
    }

    /*
     * Create a DHTMLX Scheduler event from an Event, Track or Session.
     */
    SchedulerEvent.fromObject = function (eventObj) {
        return new SchedulerEvent(null, eventObj.title, eventObj.getStart(), eventObj.getEnd(), eventObj);
    };

    SchedulerEvent.fromArray = function (eventArray) {
      return Model.fromArray(eventArray, SchedulerEvent.fromObject);
    };

    Event.fromObject = function (eventObject) {
        return Model.fromObject(eventObject, Event);
    };

    Event.fromArray = function (eventArray) {
        return Model.fromArray(eventArray, Event.fromObject);
    };

    Track.fromObject = function (trackObject) {
        var prop,
            target = new Track();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, trackObject);

                switch (prop) {
                    case "events":
                        target.events = Event.fromArray(value);
                        break;
                    default:
                        if (tools.type(target[prop]) !== "function") {
                            target[prop] = value;
                        }
                }
            }
        }

        return target;
    };

    Track.fromArray = function (trackArray) {
        return Model.fromArray(trackArray, Track.fromObject);
    };

    Session.fromObject = function (sessionObject) {
        var prop,
            target = new Session();

        for (prop in target) {
            if (target.hasOwnProperty(prop)) {
                var value = readProperty(prop, sessionObject);

                switch (prop) {
                    case "tracks":
                        target.tracks = Track.fromArray(value);
                        break;
                    default:
                        if (tools.type(target[prop]) !== "function") {
                            target[prop] = value;
                        }
                }
            }
        }

        return target;
    };

    Session.fromArray = function (sessionArray) {
        return Model.fromArray(sessionArray, Session.fromObject);
    };

    /**
     * Model for conference schedules.
     *
     * @param {Array} [content]
     *
     * @returns {Schedule}
     * @constructor
     * @public
     */
    function Schedule (content) {
        if (!(this instanceof Schedule)) {
            return new Schedule(content);
        }

        var self = tools.inherit(this, Model);

        self.content = content || [];

        // Get all the events on the specific date.
        self.getDailyEvents = function (date) {
            var dailyEvents = [];
            self.content.forEach(function (event) {
                var start = event.getStart();
                var end = event.getEnd();
                if ((date.getDate() == start.getDate()
                        && date.getMonth() == start.getMonth()
                        && date.getFullYear() == start.getFullYear())
                    || (date.getDate() == end.getDate()
                        && date.getMonth() == end.getMonth()
                        && date.getFullYear() == end.getFullYear())) {
                    dailyEvents.push(event);
                }
            });
            return dailyEvents;
        };

        // Check if the specified date is part of the schedule.
        self.isScheduledDate = function (date) {
            // only compare date not exact time
            return Schedule.isIntermediateDate(self.getStart(), self.getEnd(), date);
        };

        // Get all the events present in the schedule.
        self.getEvents = function () {
            var events = [];
            self.content.forEach(function (c) {
                if (c instanceof Event) {
                    events.push(c);
                }
            });
            return events;
        };

        // Get all the tracks present in the schedule.
        self.getTracks = function () {
            var tracks = [];
            self.content.forEach(function (c) {
                if (c instanceof Track) {
                    tracks.push(c);
                }
            });
            return tracks;
        };

        // Get all the sessions present in the schedule.
        self.getSessions = function () {
            var sessions = [];
            self.content.forEach(function (c) {
                if (c instanceof Session) {
                    sessions.push(c);
                }
            });
            return sessions;
        };

        // look at all the sessions, tracks and events to find the starting date of the session
        self.getStart = function () {
            var startingDate = null;

            self.content.forEach(function (c) {
                if (startingDate === null) {
                    startingDate = c.getStart();
                } else if (startingDate - c.getStart() > 0) {
                    startingDate = c.getStart();
                }
            });

            return startingDate;
        };

        // look at all the sessions, tracks and events to find the ending date of the schedule
        self.getEnd = function () {
            var endingDate = null;

            self.content.forEach(function (c) {
                if (endingDate === null) {
                    endingDate = c.getEnd();
                } else if (endingDate - c.getEnd() < 0) {
                    endingDate = c.getEnd();
                }
            });

            return endingDate;
        };
    }

    /*
     * Check whether the tow dates are on the same date.
     */
    Schedule.isSameDate = function (firstDate, secondDate) {
        if (firstDate !== null && secondDate !== null && firstDate !== undefined && secondDate !== undefined) {
            return firstDate.getFullYear() == secondDate.getFullYear()
                && firstDate.getMonth() == secondDate.getMonth()
                && firstDate.getDate() == secondDate.getDate();
        }
        return false;
    };

    /*
     * Check whether the specified date is during the specified timespan.
     * This is checked on basis of the date, hours and smaller time units are not
     * taken into account.
     */
    Schedule.isIntermediateDate = function (start, end, date) {
        if (start !== null && end !== null && date !== null
            && start !== undefined && end !== undefined && date !== undefined) {
            var startDate = new Date(start.getFullYear(), start.getMonth(), start.getDate());
            var endDate = new Date(end.getFullYear(), end.getMonth(), end.getDate());
            var dateDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            return startDate - dateDate <= 0 && endDate - dateDate >= 0;
        }
        return false;
    };

    /*
     * Actually the data is an array of objects but since fromArray() is used in another context,
     * I guess it is ok to use this term instead.
     */
    Schedule.fromObject = function (scheduleObject) {
        var content = [];

        scheduleObject.forEach(function(entry) {
            if (entry.hasOwnProperty("tracks")) {
                // only sessions have this property
                var session = Session.fromObject(entry);
                session.splitByDays().forEach(function (s) {
                    content.push(s);
                });
            } else if (entry.hasOwnProperty("events")) {
                // only tracks have this property
                var track = Track.fromObject(entry);
                track.splitByDays().forEach(function (t) {
                    content.push(t);
                });
            } else {
                // all the rest are simply events
                content.push(Event.fromObject(entry));
            }
        });

        return new Schedule(content);
    };

    return {
        Conference: Conference,
        Author: Author,
        ObservableAuthor: ObservableAuthor,
        Affiliation: Affiliation,
        ObservableAffiliation: ObservableAffiliation,
        Figure: Figure,
        ObservableFigure: ObservableFigure,
        Reference: Reference,
        ObservableReference: ObservableReference,
        Abstract: Abstract,
        ObservableAbstract: ObservableAbstract,
        ObservableAccount: ObservableAccount,
        AbstractGroup: AbstractGroup,
        ObservableAbstractGroup: ObservableAbstractGroup,
        Schedule: Schedule,
        Event: Event,
        Track: Track,
        Session: Session,
        SchedulerEvent: SchedulerEvent
    };
});
