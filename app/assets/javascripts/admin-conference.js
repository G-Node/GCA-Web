require(["main"], function () {
require(["lib/models", "lib/tools", "lib/owned", "knockout", "ko.sortable", "datetimepicker"],
    function(models, tools, owned, ko) {
    "use strict";

    /**
     * Admin conference view model.
     *
     * @param confId
     * @param accId
     * @returns {adminConferenceViewModel}
     * @constructor
     */
    function adminConferenceViewModel(confId, accId) {
        if (!(this instanceof adminConferenceViewModel)) {
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
        self.oldshort = "";
        self.oldmAbsLeng = 0;

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

                // Handle disposal (if KO removes by the template binding)
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

        self.saveButtonText = ko.computed(function() {
            if (self.isLoading()) {
                return "Saving...";
            }

            return self.conference() && self.conference().uuid !== null ? "Save" : "Create";
        });

        self.saveButtonDisabled = ko.computed(function() {
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
            self.error({message: text, level: "alert-" + level});
            self.isLoading(false);

            // Fade out banner on any success info message
            if (level === "info") {
                $(".alert").fadeOut(4000);
            }
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
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
            var index = self.conference().topics.indexOf(data);
            self.conference().topics.splice(index, 1);

            self.haveChanges(true);
        };

        self.addTopic = function(data) {
            var sel = $("#addTopic");
            var text = sel.val();

            // Add topic only if it contains an actual value to avoid empty radio buttons in the submission form.
            if (text !== undefined && text !== null && text !== "") {
                self.conference().topics.push(text);
                sel.val("");

                self.haveChanges(true);
            }
        };

        self.makeGroupObservable = function(group) {
            group.makeObservable(["prefix", "name", "short"]);

            for (var prop in group) {
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

            var checkPref = prefix.val() !== null && prefix.val() !== undefined && prefix.val() !== "";
            var checkShort = short.val() !== null && short.val() !== undefined && short.val() !== "";
            var checkFull =  name.val() !== null && name.val() !== undefined && name.val() !== "";

            if (!checkPref || !checkShort || !checkFull) {
                self.setError("danger", "Prefix, short and long entries have to be provided!");
            } else if (!/^\d+$/.test(prefix.val())) {
                self.setError("danger", "Prefix can only contain numbers!");
            } else if (/^\d+$/.test(name.val())) {
                self.setError("danger", "Name cannot contain only numbers!");
            } else if (/^\d+$/.test(short.val())) {
                self.setError("danger", "Short cannot contain only numbers!");
            } else {
                var grp = models.AbstractGroup(null, prefix.val(), name.val(), short.val());
                self.makeGroupObservable(grp);
                self.conference().groups.push(grp);

                name.val("");
                prefix.val("");
                short.val("");
                self.setError("info", "New group added, click 'Save'!");
            }
        };

        self.removeGroup = function(data) {
          self.conference().groups.remove(data);
        };

        self.ensureNumerical = function (data, e) {
            // Allow backspace etc.
            var allowedkeyCodes = [8, 9, 13, 27, 35, 36, 37, 38, 39, 46];
            if (allowedkeyCodes.includes(e.keyCode)) {
                return true;
            } else if (e.key.match(/[0-9]/g)) {
                return true;
            }
            return false;
        };

        self.checkmAbsLeng = function() {
            var visible = false;
            var currVal = self.conference().mAbsLeng();
            if (currVal !== "" && currVal < self.oldmAbsLeng) {
                visible = true;
            }
            return visible;
        };

        self.makeConferenceObservable = function (conf) {
            conf.makeObservable(["name", "short", "group", "cite", "description", "start", "end", "groups",
                "deadline", "logo", "thumbnail", "link", "isOpen", "isPublished", "isActive", "hasPresentationPrefs",
                "topics", "iOSApp", "mAbsLeng", "mFigs"]);

            for (var prop in conf) {
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
            if (!self.isLoading()) {
                self.isLoading("Loading conference data.");
            }

            var confURL = "/api/conferences/" + id;
            $.getJSON(confURL, self.onConferenceData).fail(self.ioFailHandler);
        };

        self.onConferenceData = function(confObj) {
            var conf = models.Conference.fromObject(confObj);
            self.makeConferenceObservable(conf);
            self.conference(conf);
            self.haveChanges(false);

            self.setupOwners("/api/conferences/" + self.conference().uuid + "/owners", self.setError);
            self.loadOwnersData(function() {
                self.isLoading(false);
            });

            self.requestConfSpecificField(self.conference().geo, "json", self.geoContent);
            self.requestConfSpecificField(self.conference().schedule, "json", self.scheduleContent);
            self.requestConfSpecificField(self.conference().info, "text", self.infoContent);

            self.oldShort = self.conference().short();
            self.oldmAbsLeng = self.conference().mAbsLeng();
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
            var confURL = "/api/conferences";
            $.getJSON(confURL, onOtherConferenceData).fail(self.ioFailHandler);

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
        };

        self.saveConference = function() {
            if (Array.isArray(self.conference().mFigs())) {
                self.conference().mFigs(0);
            }
            if (self.conference().mAbsLeng() === null || self.conference().mAbsLeng() === undefined) {
                self.conference().mAbsLeng(500);
            }
            if (self.conference().mAbsLeng() === 0 || self.conference().mAbsLeng() === "0") {
                self.setError("danger", "Abstract length has to be larger than zero");
                return;
            }
            if (self.conference().short() === null || self.conference().short() === undefined) {
                self.conference().short(self.conference().name().match(/\b(\w)/g).join("").toUpperCase());
            }
            if (self.conference().short().replace(/\s/g, "") === "") {
                self.setError("danger", "Conference short cannot be empty");
                return;
            }

            if (self.conference().groups().length > 0) {
                for (var i = 0; i < self.conference().groups().length; i++) {
                    var curr = self.conference().groups()[i];

                    var checkPref = curr.prefix() !== null && curr.prefix() !== undefined && curr.prefix() !== "";
                    var checkShort = curr.short() !== null && curr.short() !== undefined && curr.short() !== "";
                    var checkFull =  curr.name() !== null && curr.name() !== undefined && curr.name() !== "";

                    if (!checkPref || !checkShort || !checkFull) {
                        self.setError("danger", "Prefix, short and long entries have to be provided!");
                        return;
                    } else if (!/^\d+$/.test(curr.prefix())) {
                        self.setError("danger", "Prefix can only contain numbers!");
                        return;
                    } else if (/^\d+$/.test(curr.name())) {
                        self.setError("danger", "Name cannot contain only numbers!");
                        return;
                    } else if (/^\d+$/.test(curr.short())) {
                        self.setError("danger", "Short cannot contain only numbers!");
                        return;
                    }
                }
            }

            if (!(self.oldShort === self.conference().short()) && self.otherConfShorts().indexOf(self.conference().short()) >= 0) {
                self.setError("danger", "Conference short is already in use. Please choose a different one.");
                return;
            }

            var method = self.conference().uuid === null ? "POST" : "PUT";
            var url = "/api/conferences" + (self.conference().uuid === null ? "" : "/" + self.conference().uuid);
            var confData = self.conference().toJSON();
            self.isLoading("Saving conference data.");
            $.ajax(url, {
                data: confData,
                type: method,
                contentType: "application/json",
                success: function (result) {
                    self.onConferenceData(result);
                    self.setError("info", "Changes saved");
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
            if (self.conference().uuid === null) {
                self.setError("danger", "Please create conference before uploading " + fieldName + " information.");
            } else {
                self.isLoading("Uploading " + fieldName + " data.");
                $.ajax(url, {
                    data: fieldValue,
                    type: "PUT",
                    contentType: conType,
                    success: function() {
                        self.setError("info", successMsg);
                    },
                    error: function() {
                        self.setError("danger", errorMsg);
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

        window.dashboard = adminConferenceViewModel(data.conferenceUuid, data.accountUuid);
        window.dashboard.init();
    });
});
});
