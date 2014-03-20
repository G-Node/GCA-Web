require(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";


    /**
     * OwnersList view model.
     *
     *
     * @param confId
     * @returns {OwnersListViewModel}
     * @constructor
     */
    function OwnersListViewModel(confId, accId) {

        if (! (this instanceof OwnersListViewModel)) {
            return new OwnersListViewModel(confId, accId);
        }

        var self = this;
        self.accId = accId;
        self.confId = confId;
        self.isLoading = ko.observable(true);
        self.error = ko.observable(false);
        self.owners = ko.observableArray([]);
        self.newEmail = ko.observable();

        self.init = function() {
            self.loadOwnersData();
            ko.applyBindings(window.ownersList);
        };

        self.setError = function(level, text) {
            self.error({message: text, level: 'alert-' + level});
            self.isLoading(false);
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
            self.setError("danger", "Error while fetching data from server: <br\\>" + error);
        };

        self.updateOwners = function(ownersAsJson) {
            var owners = $.map(ownersAsJson, function(item) { return models.ObservableAccount.fromObject(item) });
            self.owners(owners);
        };

        self.loadOwnersData = function() {
            self.isLoading(true);
            if (self.accId !== null) {
                console.log("loadOwners::");

                var ownersURL ="/api/conferences/" + self.confId + '/owners';
                $.getJSON(ownersURL, self.updateOwners).fail(self.ioFailHandler);

            }
            self.isLoading(false);
        };

        self.addOwner = function() {

            var email = this.newEmail();

            var ownersLength = self.owners().length;
            for (var i = 0; i < ownersLength; i++) {
                if (self.owners()[i].mail() == email) return
            }

            var userURL ="/api/users?email=" + email;
            $.getJSON(userURL, onValidateEmail).fail(self.ioFailHandler);

            function onValidateEmail(accountsAsJson) {
                var found = $.map(accountsAsJson, function(item) { return models.ObservableAccount.fromObject(item) });

                if (found.length > 0) {
                    self.owners.push(found[0]);
                    self.newEmail("");
                } else {
                    self.setError("Not found", "No user with this email found");
                }
            }
        };

        self.removeOwner = function(account) { self.owners.remove(account) };

        self.save = function() {

            var data = $.map(self.owners(), function(item) { return item.toJSON()});
            $.ajax("/api/conferences/" + self.confId + '/owners', {
                data: "[" + data.join(",") + "]",
                type: "PUT",
                contentType: "application/json",
                success: function(result) { self.updateOwners(result) },
                error: self.ioFailHandler
            });
        };
    }
        
        
    $(document).ready(function() {
    
        var data = tools.hiddenData();
    
        console.log(data.conferenceUuid);
    
        window.ownersList = OwnersListViewModel(data.conferenceUuid, data.accountUuid);
        window.ownersList.init();
    });

});