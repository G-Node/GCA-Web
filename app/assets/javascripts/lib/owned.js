/**
 * Module for managing owned objects
 * @module {lib/owned}
 */
define(["lib/tools", "lib/models", "knockout"], function(tools, models, ko) {
    function Owned() {
        if (tools.isGlobalOrUndefined(this)) {
            return new Owned();
        }

        var self = this;

        self.owners = ko.observableArray([]);
        self.ownersURL = null;

        self.ownersErrorHandler = function(lvl, txt) {
            console.log("[owned.js] Unhandled " + lvl + ": " + txt);
        };

        // private stuff
        function ioFailHandler_(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            self.ownersErrorHandler("danger", "Error while fetching data from server: <br>" + err);
        }

        function onOwnersData(andThenDo) {
            return function(ownersAsJson) {
                var owners = models.ObservableAccount.fromArray(ownersAsJson);
                self.owners(owners);

                if (andThenDo) {
                    andThenDo();
                }
            };
        }

        // public stuff
        self.setupOwners = function(ownersURL, errorHandler) {
            self.ownersURL = ownersURL;
            self.ownersErrorHandler = errorHandler;
        };

        self.loadOwnersData = function(andThenDo) {
            if (self.ownersURL !== null) {
                $.getJSON(self.ownersURL, onOwnersData(andThenDo)).fail(ioFailHandler_);
            }
        };

        self.addOwner = function() {
            var mailInput = $("#ownedEmail");
            var email = mailInput.val();

            var ownersLength = self.owners().length;
            for (var i = 0; i < ownersLength; i++) {
                if (self.owners()[i].mail() === email) {
                    // if we want to add an existing one, don't do anything
                    return;
                }
            }

            var userURL = "/api/users?email=" + email;
            $.getJSON(userURL, onValidateEmail).fail(self.ioFailHandler);

            function onValidateEmail(accountsAsJson) {
                var found = models.ObservableAccount.fromArray(accountsAsJson);

                if (found.length > 0) {
                    self.owners.push(found[0]);
                    mailInput.val("");
                } else {
                    self.ownersErrorHandler("Error", "No user with this email found");
                }
            }
        };

        self.removeOwner = function(account) {
            self.owners.remove(account);
        };

        self.saveOwner = function() {
            var data = $.map(self.owners(), function(item) { return item.toJSON(); });
            $.ajax(self.ownersURL, {
                data: "[" + data.join(",") + "]",
                type: "PUT",
                contentType: "application/json",
                success: onOwnersData(null),
                error: ioFailHandler_
            });
        };
    }

    return {
        Owned: Owned
    };
});
