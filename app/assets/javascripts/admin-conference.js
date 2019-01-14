require(["main"], function () {
require(["lib/models", "lib/tools", "lib/owned", "knockout", "ko.sortable", "datetimepicker"],
    function(models, tools, owned, ko) {
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

        var self = tools.inherit(this, owned.Owned);

        self.accId = accId;
        self.isLoading = ko.observable("Loading conference data.");
        self.error = ko.observable(false);
        self.conference = ko.observable(null);
        self.haveChanges = ko.observable(false);
        self.geoContent = ko.observable(null);
        self.scheduleContent = ko.observable(null);
        self.infoContent = ko.observable(null);

        self.otherConfShorts = ko.observable(null);
        self.oldshort = '';

        ko.bindingHandlers.datetimepicker = {
            init: function(element, valueAccessor, allBindingsAccessor) {

                function onSelectHandler(text, obj) {
                    var os = valueAccessor();
                    var dt = $el.datetimepicker("getDate");
                    var dateString = dt.toISOString();
                    os(dateString);
                }

                var $el = $(element);
                var options = { onSelect: onSelectHandler };

                $el.datetimepicker(options);

                //handle disposal (if KO removes by the template binding)
                ko.utils.domNodeDisposal.addDisposeCallback(element, function() {
                    $el.datetimepicker("destroy");
                });

            },
            update: function(element, valueAccessor) {
                var $el = $(element);
                var text = ko.utils.unwrapObservable(valueAccessor());
                var value = new Date(text);

                var current = $el.datetimepicker("getDate");
                if (value - current !== 0) {
                    $el.datetimepicker("setDate", value);
                }
            }
        };

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

            if (confId !== null) {
                self.loadConference(confId);
            } else {
                var conf = models.Conference();
                self.makeConferenceObservable(conf);
                self.conference(conf);
                self.isLoading(false);
            }
            self.loadOtherConferences();

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
            var errobj = $.parseJSON(jqxhr.responseText);

            var details = "";
            if ("message" in errobj) {
                details = "<br>" + errobj.message;
            }

            self.setError("danger", "IO error: " + error + details);
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

        self.makeGroupObservable = function(group) {
            group.makeObservable(['prefix', 'name', 'short']);

            for(var prop in group) {
                if (group.hasOwnProperty(prop)) {
                    var value = group[prop];

                    if (value && tools.functionName(value) === "observable") {
                        value.subscribe(self.changeHandler);
                    }
                }
            }
        };

        self.addGroup = function(data) {
            var name = $("#ngName");
            var prefix = $("#ngPrefix");
            var short = $("#ngShort");
            if(! /^\d+$/.test(prefix.val())){self.setError("danger", "Prefix can only contain numbers!");}
            else if(/^\d+$/.test(name.val())){self.setError("danger", "Name cannot contain only numbers!");}
            else if(/^\d+$/.test(short.val())){self.setError("danger", "Short cannot contain only numbers!");}
            else {
                var grp = models.AbstractGroup(null, prefix.val(), name.val(), short.val());
                self.makeGroupObservable(grp);
                self.conference().groups.push(grp);

                name.val('');
                prefix.val('');
                short.val('');
                self.setError("info", 'New group added, click "Save"!');
            }
        };

        self.removeGroup = function(data) {
          self.conference().groups.remove(data);
        };


        self.makeConferenceObservable = function (conf) {
            conf.makeObservable(["name", "short", "group", "cite", "description", "start", "end", "groups",
                "deadline", "logo", "thumbnail", "link", "isOpen", "isPublished", "isActive", "hasPresentationPrefs",
                "topics", "iOSApp","mAbsLeng","mFigs"]);

            for(var prop in conf) {
                if (conf.hasOwnProperty(prop)) {
                    var value = conf[prop];

                    if (value !== undefined && tools.functionName(value) === "observable") {
                        value.subscribe(self.changeHandler);
                    }
                }
            }

            conf.groups().forEach(self.makeGroupObservable);
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
            self.setupOwners("/api/conferences/" + self.conference().uuid + "/owners", self.setError);
            self.loadOwnersData(function() {
                self.isLoading(false);
            });

            self.requestConfSpecificField(self.conference().geo, "json", self.geoContent);
            self.requestConfSpecificField(self.conference().schedule, "json", self.scheduleContent);
            self.requestConfSpecificField(self.conference().info, "text", self.infoContent);

            self.oldShort = self.conference().short();
        };

        self.requestConfSpecificField = function(url, type, setObservable) {
            $.ajax({
                url: url,
                type: "GET",
                dataType: type,
                success: function(obj) {
                    var setValue = obj;
                    if (type === "json") {
                        setValue = JSON.stringify(obj);
                    }
                    setObservable(setValue);
                },
                error: function(obj) {
                    // Do not display NotFound errors.
                    if (obj.status !== 404) {
                        self.ioFailHandler(obj, obj.statusText, obj.status);
                    }
                }
            });
        };

        self.loadOtherConferences = function() {
            console.log("loadOtherConferences::");

            //now load the data from the server
            var confURL = "/api/conferences";
            $.getJSON(confURL, onOtherConferenceData).fail(self.ioFailHandler);

            //conference data
            function onOtherConferenceData(confObj) {
                var confs = models.Conference.fromArray(confObj);
                var confShorts = Array();
                if (confs !== null) {
                    confs.forEach(function (current) {
                        confShorts.push(current.short);
                    });
                    self.otherConfShorts(confShorts);
                }
            }
        }

        self.saveConference = function() {
            console.log("saveConference::");
            //check fields
            if( Array.isArray(self.conference().mFigs()) ){ self.conference().mFigs(0); }
            if(self.conference().mAbsLeng() == null){ self.conference().mAbsLeng(500); }
            else if(self.conference().mAbsLeng() == 0){
                self.setError("danger", "Max. Abs. Len. has to be larger than 0.");
                return;
            }
            if (self.conference().short() == null ) {
                self.conference().short(self.conference().name().match(/\b(\w)/g).join('').toUpperCase());
            }
            if(self.conference().short().replace(/\s/g,'') == ''){
                self.setError("danger", "Conference short cannot be empty.");
                return;
            }
            if(!(self.oldShort == self.conference().short()) && self.otherConfShorts().indexOf(self.conference().short()) >= 0){
                console.log(self.oldShort + ', NEW: ' + self.conference().short() + ' ' + self.otherConfShorts().indexOf(self.conference().short()));
                self.setError("danger", "Conference short is already in use. Please choose a different one.");
                return;
            }

            var method = self.conference().uuid === null ? "POST" : "PUT";
            var url = "/api/conferences" + (self.conference().uuid === null ? "" : "/" + self.conference().uuid);
            var confData = self.conference().toJSON();
            console.log(confData);
            self.isLoading("Saving conference data.");
            $.ajax(url, {
                data: confData,
                type: method,
                contentType: "application/json",
                success: function (result) {
                    self.onConferenceData(result);
                    self.setError("info", "Changes saved")
                },
                error: self.ioFailHandler
            });

        };

        self.isConferenceSaved = ko.computed(function() {
            if (self.conference() === null) {
                return false;
            } else {
                return self.conference().uuid !== null;
            }
        });

        self.uploadSpecificField = function (url, fieldName, fieldValue, conType, successMsg, errorMsg) {
            if( self.conference().uuid === null ) {
                console.log("Conference does not exist yet.");
                self.setError("danger", "Please create conference before uploading "+ fieldName +" information.");
            } else {
                self.isLoading("Uploading "+ fieldName +" data.");
                $.ajax(url, {
                    data: fieldValue,
                    type: "PUT",
                    contentType: conType,
                    success: function() {
                        self.setError("info", successMsg)
                    },
                    error: function() {
                        self.setError("danger", errorMsg)
                    }
                });
            }
        };

        self.uploadGeo = function() {
            var url = self.conference().geo;
            var fieldName = "geo";
            var fieldValue = self.geoContent();
            var contentType = "application/json";
            var successMsg = "Geo information saved.";
            var errorMsg = "Error uploading geo information. Please ensure that the JSON is well-formed.";

            self.uploadSpecificField(url, fieldName, fieldValue, contentType, successMsg, errorMsg);
        };

        self.uploadSchedule = function() {
            var url = self.conference().schedule;
            var fieldName = "schedule";
            var fieldValue = self.scheduleContent();
            var contentType = "application/json";
            var successMsg = "Schedule information saved.";
            var errorMsg = "Error uploading schedule information. Please ensure that the JSON is well-formed.";

            self.uploadSpecificField(url, fieldName, fieldValue, contentType, successMsg, errorMsg);
        };

        self.uploadInfo = function() {
            var url = self.conference().info;
            var fieldName = "info";
            var fieldValue = self.infoContent();
            var contentType = "text/plain";
            var successMsg = "Info data saved.";
            var errorMsg = "Error uploading info data.";

            self.uploadSpecificField(url, fieldName, fieldValue, contentType, successMsg, errorMsg);
        };
    }

    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid, data.accountUuid);

        window.dashboard = adminConferenceViewModel(data.conferenceUuid, data.accountUuid);
        window.dashboard.init();
    });

});
});
