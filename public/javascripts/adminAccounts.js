require(["lib/models", "lib/tools", "lib/msg"], function(models, tools, msg) {
    "use strict";

    function adminAccountsViewModel() {


        if (!(this instanceof adminAccountsViewModel)) {
            return new adminAccountsViewModel();
        }

        var self = tools.inherit(this, msg.MessageVM);

        self.accounts = ko.observableArray(null);
        self.noAccounts = ko.computed(function() {
            return self.accounts().length;
        });

        self.init = function() {
            ko.applyBindings(window.dashboard);
            self.loadAccounts();
        };

        self.loadAccounts = function() {

            var confURL ="/api/user/list";
            $.getJSON(confURL, onAccountData).fail(function() {
                self.setError("Error", "Could not load accounts from server!");
            });

            function onAccountData(accountData) {
                var acc = models.ObservableAccount.fromArray(accountData);
                self.accounts(acc);
            }
        }
    }

    // the show begins
    $(document).ready(function() {

        window.dashboard = adminAccountsViewModel();
        window.dashboard.init();
    });

});
