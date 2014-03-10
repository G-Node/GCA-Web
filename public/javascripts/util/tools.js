/**
 * Module for misc utility functions
 * @module {util/tools}
 */
define(function() {
    "use strict";

    /**
     * Checks if an object is the global object.
     *
     * @param {object} obj The object to test.
     * @returns {boolean}
     * @public
     */
    function isGlobal(obj) {
        return Function('return this;')() === obj;
    }

    /**
     * Implements inheritance via object augmentation.
     * Here is an example for the use of this function:
     *      https://gist.github.com/stoewer/9461273
     *
     * @param obj {object} The object that should inherit from superclass.
     * @param superclass {Function}The superclass to inherit from. Pass further arguments for
     *                   the superclass constructor after this parameter.
     * @returns {object} The augmented object.
     * @public
     */
    function inherit(obj, superclass) {

        if (superclass instanceof Function) {

            var param;
            if (arguments.length > 2) {
                param = Array.prototype.slice.call(arguments, 2);
            } else {
                param = [];
            }

            superclass.apply(obj, param);
        }

        return obj;
    }

    /**
     * Check the type of an object.
     * Source: https://gist.github.com/jonbretman/7259628
     *
     * @param obj The object to test.
     * @returns {string} The type as lowercase string.
     * @public
     */
    function type(obj) {

        if (obj === null) {
            return 'null';
        }

        if (obj && (obj.nodeType === 1 || obj.nodeType === 9)) {
            return 'element';
        }

        var s = Object.prototype.toString.call(obj);
        var type = s.match(/\[object (.*?)\]/)[1].toLowerCase();

        if (type === 'number') {
            if (isNaN(obj)) {
                return 'nan';
            }
            if (!isFinite(obj)) {
                return 'infinity';
            }
        }

        return type;
    }

    /**
     * Match input names like "foo", "foo[]" or "foo[bar]"
     * @type {RegExp}
     * @private
     */
    var _namePattern = /^(\w+)($|\[\]|\[(\w+)\]$)/;

    /**
     * Read data from forms and map them to fields of an object, which is
     * returned as result.
     *
     * Caution: before using this function you should try to solve the problem via
     *          knockouts value binding.
     *
     * The following form:
     *
     *      <input name="foo" />
     *      <input name="bar[]" />
     *      <input name="bar[]" />
     *      <input name="bla[a]" />
     *      <input name="bla[b]" />
     *
     * maps to this output:
     *
     *      {
     *          foo: ??,
     *          bar: [??, ??],
     *          bla: {
     *              a: ??,
     *              b: ??
     *          }
     *      }
     *
     * @param dom {jQuery}       The element surrounding the form fields.
     *
     * @returns {Object}        The parsed form data as object.
     * @public
     */
    function formParse(dom) {

        var data = {};

        var inputs = dom.find("input, textarea, select");
        inputs.each(function() {

            var input = $(this),
                type  = input.attr("type").toLowerCase(),
                name  = input.attr("name").toLowerCase(),
                val   = input.val();

            if (type !== "submit") {

                var match = _namePattern.exec(name);

                if (!match) throw "Unsupported input element name: " + name;

                if (match[2] === "") {
                    data[name] = val;
                } else if (match[2] === "[]" && match[3] === undefined) {
                    if (data.hasOwnProperty(name)) {
                        data[name].append(val);
                    } else {
                        data[name] = [val];
                    }
                } else if (match[3] !== undefined) {
                    if (!data.hasOwnProperty(name)) {
                        data[name] = {}
                    }
                    data[name][match[3]] = val;
                }
            }

        });

        return data;
    }

    return {
        isGlobal: isGlobal,
        inherit: inherit,
        type: type,
        formParse: formParse
    }

});