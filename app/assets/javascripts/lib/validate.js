/**
 * Module for abstract validation.
 * @module {lib/validate}
 */
define(["lib/tools"], function (tools) {
    "use strict";

    //
    // Validators receive a certain model object and return a list of warnings and errors
    // like this: [{state: [error|warning], msg: "message"}, ...]
    //

    /**
     * Validates an author.
     *
     * @param author      The author or array of authors to test.
     *
     * @returns {Result} The validation result.
     */
    function validateAuthor(author) {
        return validate(
            author,
            should("firstName", notNothing, "Author is missing a first name"),
            should("lastName", notNothing, "Author is missing a last name"),
            should("mail", notNothing, "Author is missing an email"),
            should("affiliations", notEmpty, "Author has no affiliations")
        );
    }

    /**
     * Validates a whole abstract.
     *
     * @param abstract      The abstract or array of abstracts to test.
     *
     * @returns {Result} The validation result.
     */
    function validateAbstract(abstract) {
        return validate(
            abstract,
            must("title", notNothing, "The abstract has no title"),
            should("abstrTypes", notEmpty, "No presentation type selected"),
            should("authors", notEmpty, "No authors are defined for this abstract"),
            should("authors", useAllAffiliations(getVal(abstract, "affiliations")), "Some affiliations are not used"),
            should("text", notNothing, "The abstract contains no text"),
            should("topic", notNothing, "No topic selected for the abstract")
        ).concat(
            validateAuthor(abstract.authors)
        );
    }

    /**
     * Generic validator
     *
     * @param {Array|Object} objects    The object or object array to test.
     * @param [arguments]               Zero or more conditions.
     *
     * @returns {Result} The validation results.
     */
    function validate(objects) {
        var conditions = Array.prototype.slice.call(arguments, 1),
            result = Result();

        if (tools.type(objects) === "function") {
            objects = objects();
        }

        if (tools.type(objects) !== "array") {
            objects = [objects];
        }

        for (var i = 0; i < objects.length; i++) {
            var testObj = objects[i];
            for (var j = 0; j < conditions.length; j++) {
                result = result.concat(conditions[j](testObj));
            }
        }

        return result;
    }

    //
    // Result class
    //

    /**
     * Validation result.
     *
     * @param [errors]        Array of error messages or a single error message.
     * @param [warnings]      Array of waning messages of a single warning message.
     *
     * @returns {Result}
     * @constructor
     * @public
     */
    function Result(errors, warnings) {
        if (!(this instanceof  Result)) {
            return new Result(errors, warnings);
        }

        var self = this;

        self.errors = errors || [];
        self.warnings = warnings || [];

        if (tools.type(self.errors) !== "array") {
            self.errors = [self.errors];
        }

        if (tools.type(self.warnings) !== "array") {
            self.warnings = [self.warnings];
        }

        self.all = function() {
            return self.errors.concat(self.warnings);
        };

        self.concat = function(result) {
            var errors = self.errors.concat(result.errors),
                warnings = self.warnings.concat(result.warnings);
            return Result(errors, warnings);
        };

        self.ok = function() {
            return self.errors.length === 0 && self.warnings.length === 0;
        };

        self.hasErrors = function() {
            return self.errors.length > 0;
        };

        self.hasWarnings = function() {
            return self.warnings.length > 0;
        };
    }

    //
    // Conditions: conditions create a function that receives an object and
    // returns validation result.
    //

    /**
     * Creates a condition check that produces an error with the given message if
     * the fields value does not pass the test.
     *
     * @param field     The field name to test.
     * @param test      The test itself (e.g. notNothing or notEmpty)
     * @param msg       The message to produce if the test fails.
     *
     * @returns {Function} The created condition check.
     */
    function must(field, test, msg) {
        return function(obj) {
            var val = getVal(obj, field);

            if (!test(val)) {
                return Result(msg);
            } else {
                return Result();
            }
        };
    }

    /**
     * Creates a condition check that produces a warning with the given message if
     * the fields value does not pass the test.
     *
     * @param field     The field name to test.
     * @param test      The test itself (e.g. notNothing or notEmpty)
     * @param msg       The message to produce if the test fails.
     *
     * @returns {Function} The created condition check.
     */
    function should(field, test, msg) {
        return function(obj) {
            var val = getVal(obj, field);

            if (!test(val)) {
                return Result(null, msg);
            } else {
                return Result();
            }
        };
    }

    //
    // Checks
    //

    /**
     * Checks if a value is defined.
     *
     * @param val   The value to check.
     *
     * @returns {boolean}
     */
    function notNothing(val) {
        return val ? true : false;
    }

    /**
     * Checks if the length of a val is larger than zero.
     *
     * @param val   The value to check.
     *
     * @returns {boolean}
     */
    function notEmpty(val) {
        return val ? val.length > 0 : false;
    }

    /**
     * Creates a special test that checks whether all affiliations are used.
     *
     * @param affiliations The affiliations of the abstract.
     *
     * @returns {Function} Returns the actual test function, that gets the author
     *                     list as first and only argument.
     */
    function useAllAffiliations(affiliations) {
        var positions = affiliations.map(function(_, index) {
            return index;
        });

        return function(authors) {
            var used = positions.filter(function(pos) {
                return authors.some(function(author) {
                    var otherPositions = getVal(author, "affiliations");
                    return otherPositions.indexOf(pos) >= 0;
                });
            });
            return used.length === positions.length;
        };
    }

    // Helper
    function getVal(obj, field) {
        var val = obj[field];

        if (tools.type(val) === "function") {
            val = val.apply(obj, []);
        }

        return val;
    }

    // Create and return the module
    return {
        author: validateAuthor,
        abstract: validateAbstract
    };
});
