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
    function adminConferenceViewModel(confId, accId) {

        if (! (this instanceof adminConferenceViewModel)) {
            return new adminConferenceViewModel(confId, accId);
        }

        var self = this;
        self.accId = accId;
        self.isLoading = ko.observable("Loading conference data.");
        self.error = ko.observable(false);
        self.owners = ko.observableArray([]);
        self.newEmail = ko.observable();
        self.conference = ko.observable(null);
        self.haveChanges = ko.observable(false);

        self.saveButtonText = ko.computed(function(){

            if (self.isLoading()) {
                return "Saving...";
            }

            return self.conference() && self.conference().uuid !== null ? "Save" : "Create";
        });

        self.saveButtonDisabled = ko.computed(function(){
            return !self.haveChanges();
        });

        self.init = function() {

            if (confId != null) {
                self.loadConference(confId);
            } else {
                var conf = models.Conference();
                self.makeConferenceObservable(conf);
                self.conference(conf);
                self.isLoading(false);
            }

            ko.applyBindings(window.dashboard);
        };

        self.setError = function(level, text) {
            self.error({message: text, level: 'alert-' + level});
            self.isLoading(false);

            //remove info automatically after 1 second
            if(level === "info") {
                window.setTimeout(function(){ $(".alert").alert("close"); }, 1000);
            }
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
            self.setError("danger", "Error while fetching data from server: <br\\>" + error);
        };

        self.changeHandler = function(newValue) {
            self.haveChanges(true);
            return true;
        };

        self.removeTopic = function(data) {
            console.log("Remove" + data);

            var index = self.conference().topics.indexOf(data);
            self.conference().topics.splice(index, 1);

            self.haveChanges(true);
        };

        self.addTopic = function(data) {
            var sel = $("#addTopic");
            var text = sel.val();
            self.conference().topics.push(text);
            sel.val("");

            self.haveChanges(true);
        };

        self.makeConferenceObservable = function (conf) {
            conf.makeObservable(["name", "short", "cite", "start", "end",
                "deadline", "logo", "thumbnail", "link", "isOpen", "topics"]);

            for(var prop in conf) {
                if (conf.hasOwnProperty(prop)) {
                    var value = conf[prop];

                    if (value && value.name === "observable") {
                        value.subscribe(self.changeHandler);
                    }
                }
            }
        };

        self.loadConference = function(id) {
            console.log("loadConference::");
            if(!self.isLoading()) {
                self.isLoading("Loading conference data.");
            }

            //now load the data from the server
            var confURL ="/api/conferences/" + id;
            $.getJSON(confURL, self.onConferenceData).fail(self.ioFailHandler);
        };

        //conference data
        self.onConferenceData = function(confObj) {
            console.log("Got conference data");
            var conf = models.Conference.fromObject(confObj);
            self.makeConferenceObservable(conf);
            self.conference(conf);
            self.haveChanges(false);
            //now that we have the conference, get the owners
            self.loadOwnersData();
        };

        self.saveConference = function() {
            console.log("saveConference::");
            var method = self.conference().uuid === null ? "POST" : "PUT";
            var url = "/api/conferences" + (self.conference().uuid === null ? "" : "/" + self.conference().uuid);
            var confData = self.conference().toJSON(4);
            console.log(confData);
            self.isLoading("Saving conference data.");
            $.ajax(url, {
                data: confData,
                type: method,
                contentType: "application/json",
                success: function(result) {
                    self.onConferenceData(result);
                    self.setError("info", "Changes saved")
                },
                error: self.ioFailHandler
            });
        };


        self.updateOwners = function(ownersAsJson) {
            var owners = $.map(ownersAsJson, function(item) { return models.ObservableAccount.fromObject(item) });
            self.owners(owners);
        };

        self.loadOwnersData = function() {
            self.isLoading("Loading owner data");
            if (self.conference().uuid !== null) {
                console.log("loadOwners::");

                var ownersURL ="/api/conferences/" + self.conference().uuid + '/owners';
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

        self.saveOwner = function() {

            var data = $.map(self.owners(), function(item) { return item.toJSON()});
            $.ajax("/api/conferences/" + self.conference().uuid + '/owners', {
                data: "[" + data.join(",") + "]",
                type: "PUT",
                contentType: "application/json",
                success: function(result) {
                    self.updateOwners(result);
                    self.setError("owners_update", "Changes saved")
                },
                error: self.ioFailHandler
            });
        };
    }


    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid, data.accountUuid);

        window.dashboard = adminConferenceViewModel(data.conferenceUuid, data.accountUuid);
        window.dashboard.init();
    });

});