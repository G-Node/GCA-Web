/**
 * Module for misc utility functions
 * @module {lib/tools}
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
    function isGlobalOrUndefined(obj) {
        var result = true;

        if (obj !== undefined) {
            result = new Function("return this;")() === obj;
        }

        return result;
    }

    /**
     * Retrieve hidden data form the document as object. The id of hidden data
     * elements is turned into a camel case key and the content into a string or
     * null if empty.
     *
     * @returns {{}} Object with hidden data as key value pairs.
     */
    function hiddenData() {
        var obj = {};

        $(".hidden-data").each(function() {
            $(this).children().each(function() {
                var key = toCamelCase($(this).attr("id")),
                    val = $(this).text().trim();

                if (val !== "") {
                    obj[key] = val;
                } else {
                    obj[key] = null;
                }
            });
        });

        return obj;
    }

    /**
     * Implements inheritance via object augmentation.
     * Here is an example for the use of this function:
     *      https://gist.github.com/stoewer/9461273
     *
     * @param obj {object} The object that should inherit from superclass.
     * @param superclass {Function}The superclass to inherit from. Pass further
     *                   arguments for the superclass constructor after this parameter.
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
        var str,
            typ;

        if (obj === null) {
            return "null";
        }

        if (obj && (obj.nodeType === 1 || obj.nodeType === 9)) {
            return "element";
        }

        str = Object.prototype.toString.call(obj);
        typ = str.match(/\[object (.*?)\]/)[1].toLowerCase();

        if (typ === "number") {
            if (isNaN(obj)) {
                return "nan";
            }
            if (!isFinite(obj)) {
                return "infinity";
            }
        }

        return typ;
    }

    /**
     * Convert from camel case to underscore.
     *
     * @param {string} str The string to translate.
     *
     * @returns {string} The modified string.
     * @public
     */
    function toUnderscore(str) {
        function substitute(char) {
            return "_" + char.toLowerCase();
        }

        return str.replace(/([A-Z])/g, substitute);
    }

    /**
     * Convert '_' or '-' to camel case.
     *
     * @param {string} str The string to translate.
     *
     * @returns {string} The modified string.
     * @public
     */
    function toCamelCase(str) {
        function substitute(char) {
             return char.toUpperCase();
        }

        return str.toLowerCase().replace(/[\-_](.)/g, substitute).replace(/[\-_]/g, "");
    }

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
        var match,
            pattern = /^(\w+)($|\[\]|\[(\w+)\]$)/,
            data = {},
            inputs = dom.find("input, textarea, select");

        inputs.each(function() {
            var input = $(this),
                type  = input.attr("type").toLowerCase(),
                name  = input.attr("name").toLowerCase(),
                val   = input.val();

            if (type !== "submit") {
                match = pattern.exec(name);

                if (!match) {
                    throw "Unsupported input element name: " + name;
                }

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
                        data[name] = {};
                    }
                    data[name][match[3]] = val;
                }
            }
        });

        return data;
    }

    /**
     * Gets the name of a function (also in IE)
     *
     * @param fn {Function}     The function to get the name from.
     *
     * @returns {string}        The function name or null if fn is not a function.
     * @public
     */
    function functionName(fn) {
        if (type(fn) !== "function") {
            return null;
        }

        if (fn.hasOwnProperty("name")) {
            return fn.name;
        } else {
            var parsed = /\W*function\s+([\w\$]+)\(/.exec(fn);
            return parsed && parsed[1] ? parsed[1] : null;
        }
    }

    return {
        isGlobalOrUndefined: isGlobalOrUndefined,
        hiddenData: hiddenData,
        inherit: inherit,
        type: type,
        toUnderscore: toUnderscore,
        toCamelCase: toCamelCase,
        formParse: formParse,
        functionName: functionName
    };
});
