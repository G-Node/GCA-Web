require(["main"], function () {
require(["lib/models", "lib/tools", "lib/msg", "knockout"], function(models, tools, msg, ko) {
    "use strict";

    function AdminAccountsViewModel() {
        if (!(this instanceof AdminAccountsViewModel)) {
            return new AdminAccountsViewModel();
        }

        var self = tools.inherit(this, msg.MessageBox);

        self.accounts = ko.observableArray(null);
        self.noAccounts = ko.computed(function() {
            return self.accounts().length;
        });

        self.init = function() {
            ko.applyBindings(window.dashboard);
            self.loadAccounts();
        };

        self.loadAccounts = function() {
            var confURL = "/api/user/list";
            $.getJSON(confURL, onAccountData).fail(function() {
                self.setError("Error", "Could not load accounts from server!");
            });

            function onAccountData(accountData) {
                var acc = models.ObservableAccount.fromArray(accountData);
                self.accounts(acc);
            }
        };
    }

    // the show begins
    $(document).ready(function() {
        window.dashboard = AdminAccountsViewModel();
        window.dashboard.init();
    });
});
});
