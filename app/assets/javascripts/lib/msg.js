/**
 * Module for misc utility functions
 * @module {lib/msg}
 */
define(["lib/tools", "knockout"], function(tools, ko) {
    function MessageBox() {
        if (tools.isGlobalOrUndefined(this)) {
            return new MessageBox();
        }

        var self = this;

        var success = "success",
            info = "info",
            warning = "warning",
            danger = "danger",
            levels = [success, info, warning, danger],
            defaultTimeout = 3000;

        self.message = ko.observable(null);

        self.setMessage = function(level, text, description, timeout) {
            if (levels.indexOf(level) < 0) {
                throw "Not a valid level";
            }

            if (text) {
                self.message({message: text, level: "callout-" + level, desc: description, close: self.clearMessage});
            } else {
                self.clearMessage();
            }

            if (timeout) {
                var t = tools.type(timeout) === "number" ? timeout : defaultTimeout;
                setTimeout(self.clearMessage, t);
            }
        };

        self.clearMessage = function() {
            self.message(null);
        };

        self.setDanger = function(text, description, timeout) {
            self.setMessage(danger, text, description, timeout);
        };

        self.setError = self.setDanger;

        self.setWarning = function(text, description, timeout) {
            self.setMessage(warning, text, description, timeout);
        };

        self.setInfo = function(text, description, timeout) {
            self.setMessage(info, text, description, timeout);
        };

        self.isLoading = function(loading, timeout) {
            if (loading) {
                self.setMessage(info, "Loading data!", "Please wait...", timeout);
            } else {
                self.clearMessage();
            }
        };

        self.setSuccess = function(text, description, timeout) {
            self.setMessage(success, text, description, timeout);
        };

        self.setOk = self.setSuccess;
    }

    return {
        MessageBox: MessageBox
    };
});
