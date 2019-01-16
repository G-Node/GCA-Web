/**
 * @module {lib/multi}
 */
define(function() {
    "use strict";

    // some constants
    var sep = "\r\n",
        sepEnd = "\r\n\r\n",
        stdContentType = "text/plain",
        binContentType = "application/octet-stream";

    // helper function
    function createBoundary() {
        var alpha = "0123456789abcdefghijklmnopqrstuv",
            base  = alpha.length,
            size  = 32,
            bound = "------";

        for (var i = 0; i < size; i++) {
            bound += alpha[Math.floor(Math.random() * base)];
        }

        return bound;
    }

    /**
     * Generates multi part form data request body and header.
     *
     * @returns {MultiPart}
     * @constructor
     * @public
     */
    function MultiPart() {
        if (!(this instanceof MultiPart)) {
            return new MultiPart();
        }

        var self = this;
        var bound = createBoundary(),
            mpContentType = "multipart/form-data; boundary=" + bound,
            body = "";

        /**
         * Append text data.
         *
         * @param {string} name             The name for the text data.
         * @param {string} text             The text data.
         * @param {string} [contentType]    The content type (default 'text/plain;charset=utf-8')
         *
         * @public
         */
        self.appendText = function(name, text, contentType) {
            contentType = contentType || stdContentType;

            body += bound + sep;
            body += 'Content-Disposition: form-data; name="' + name + '"' + sepEnd;

            body += text + sep;
        };

        /**
         * Append file data.
         *
         * @param {string} name          Name for the data.
         * @param {string} fileName      The original file name.
         * @param {string} data          The data as binary string.
         * @param {string} [contentType] The content type (default: 'application/octet-stream')
         */
        self.appendFile = function(name, fileName, data, contentType) {
            contentType = binContentType;

            body += bound + sep;
            body += 'Content-Disposition: form-data; name="' + name + '"; filename="' + fileName + '"' + sep;
            body += "Content-Length: " + data.length + sep;
            body += "Content-Type: " + contentType + sepEnd;

            body += data + sep;
        };

        /**
         * Append file data from an DOM element or jQuery selection that represents
         * an input (type: file).
         *
         * @param {string} name             The name of the element.
         * @param {Element|jQuery} input    The file input element.
         * @param {Function} [success]      Success callback.
         * @param {Function} [error]        Error callback.
         */
        self.appendInput = function(name, input, success, error) {
            if (!FileReader) {
                throw "Browser does not support FileReader";
            }

            if (input instanceof jQuery) {
                input = input.get(0);
            }

            var files = input.files;

            for (var i = 0; i < files.length; i++) {
                var file = files[i],
                    reader = new FileReader();

                reader.onload = onload(file);
                reader.onerror = onerror(file);
                reader.onabort = onerror(file);

                reader.readAsBinaryString(file);
            }

            function onload(f) {
                return function(e) {
                    self.appendFile(name, f.name, e.target.result, f.type);
                    if (success) success(self, f, "Loaded file " + f.name);
                };
            }

            function onerror(f) {
                return function(e) {
                    if (error) error(self, f, "Unable to read file: " + f.name);
                };
            }
        };

        /**
         * Get the content type.
         *
         * @returns {string} The content type.
         */
        self.contentType = function() {
            return mpContentType;
        };

        /**
         * Get the request body.
         *
         * @returns {string} The request body.
         */
        self.body = function() {
            return body + bound + "--" + sep;
        };

        /**
         * Get content type header plus request body.
         *
         * @returns {string} Header and request body as string.
         */
        self.all = function() {
            return "Content-Type: " + self.contentType() + sepEnd + self.body();
        };
    }

    return {
        MultiPart: MultiPart
    };
});
