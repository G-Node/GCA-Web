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

        self.autosave = ko.observable({text: "Loading", css: "label-primary"});

        self.description = ko.observable(null);
        self.notice = ko.observable(null);

        self.logoURL = ko.observable(null);
        self.thumbnailURL = ko.observable(null);
        self.logo = ko.observable(null);
        self.thumbnail = ko.observable(null);
        // only required when a new banner is added
        self.newLogo = {
            file: null
        };
        self.newThumbnail = {
            file: null
        };

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
            conf.makeObservable(["name", "short", "group", "cite", "start", "end", "groups",
                "deadline", "imageUrls", "infoTexts", "link", "isOpen", "isPublished", "isActive", "hasPresentationPrefs",
                "topics", "iOSApp", "banner", "mAbsLeng", "mFigs"]);

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

            var i = 0;
            if (self.conference().infoTexts()) {
                for (i = 0; i < self.conference().infoTexts().length; i++) {
                    if (self.conference().infoTexts()[i].search("description") >= 0) {
                        self.description(self.conference().infoTexts()[i].split(": ")[1]);
                    } else if (self.conference().infoTexts()[i].search("notice") >= 0) {
                        self.notice(self.conference().infoTexts()[i].split(": ")[1]);
                    }
                }
            }

            if (self.conference().imageUrls()) {
                for (i = 0; i < self.conference().imageUrls().length; i++) {
                    if (self.conference().imageUrls()[i].search("logo") >= 0) {
                        self.logoURL(self.conference().imageUrls()[i].split(": ")[1]);
                    } else if (self.conference().imageUrls()[i].search("thumbnail") >= 0) {
                        self.thumbnailURL(self.conference().imageUrls()[i].split(": ")[1]);
                    }
                }
            }

            if (self.conference().banner()) {
                for (i = 0; i < self.conference().banner().length; i++) {
                    if (self.conference().banner()[i].bType === "logo") {
                        self.logo(self.conference().banner()[i]);
                    } else if (self.conference().banner()[i].bType === "thumbnail") {
                        self.thumbnail(self.conference().banner()[i]);
                    }
                }
            }
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
                var confShorts = [];
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

            var imageUrlsChange = [];
            if (self.logoURL() !== null || self.logoURL() !== "") {
                imageUrlsChange.push("logo: " + self.logoURL());
            }
            if (self.thumbnailURL() !== null || self.thumbnailURL() !== "") {
                imageUrlsChange.push("thumbnail: " + self.thumbnailURL());
            }
            if (imageUrlsChange.length > 0) {
                imageUrlsChange.forEach(function (value) {
                    var iuType = value.split(":")[0];
                    for (var i = 0; i < self.conference().imageUrls().length; i++) {
                        if (self.conference().imageUrls()[i].search(iuType) >= 0) {
                            self.conference().imageUrls().splice(i);
                        }
                    }
                    self.conference().imageUrls().push(value);
                });
            }

            var infoTextsChange = [];
            if (self.description() !== null || self.description() !== "") {
                infoTextsChange.push("description: " + self.description());
            }
            if (self.notice() !== null || self.notice() !== "") {
                infoTextsChange.push("notice: " + self.notice());
            }
            if (infoTextsChange.length > 0) {
                infoTextsChange.forEach(function (value) {
                    var iuType = value.split(":")[0];
                    for (var i = 0; i < self.conference().infoTexts().length; i++) {
                        if (self.conference().infoTexts()[i].search(iuType) >= 0) {
                            self.conference().infoTexts().splice(i);
                        }
                    }
                    self.conference().infoTexts().push(value);
                });
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
                    if (self.newLogo.file) {
                        self.uploadBanner("logo");
                    } else if (self.newThumbnail.file) {
                        self.uploadBanner("thumbnail");
                    } else {
                        self.autosave({text: "Ok", css: "label-success"});
                    }
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

        self.getNewBanner = function(data, event) {
            var bType = event.target.name.replace("-file", "");

            if (bType === "logo") {
                self.newLogo.file = event.currentTarget.files[0];
            } else if (bType === "thumbnail") {
                self.newThumbnail.file = event.currentTarget.files[0];
            }
            self.uploadBanner(bType);
        };

        self.uploadBanner = function (bType) {
            var json = {bType: bType},
                file = null,
                data = new FormData();

            if (bType === "logo") {
                file = self.newLogo.file;
            } else if (bType === "thumbnail") {
                file = self.newThumbnail.file;
            }

            if (file) {
                var fileName = file.name,
                    fileSize = file.size,
                    splitted = fileName.split("."),
                    ending = splitted[splitted.length - 1].toLowerCase();

                if (["jpeg", "jpg", "gif", "giff", "png"].indexOf(ending) < 0) {
                    self.setError("danger", "Figure file format for banner not supported (only jpeg, gif or png is allowed).");
                    return;
                }

                if (fileSize > 5242880) {
                    self.setError("danger", "Figure banner file is too large (limit is 5MB).");
                    return;
                }

                data.append("file", file);
                data.append("banner", JSON.stringify(json));

                $.ajax({
                    url: "/api/conferences/" + self.conference().uuid + "/banner",
                    type: "POST",
                    dataType: "json",
                    data: data,
                    processData: false,
                    contentType: false,
                    success: success,
                    error: fail,
                    cache: false
                });
            }

            function success(obj, stat, xhr) {
                if (bType === "logo") {
                    self.newLogo.file = null;
                } else if (bType === "thumbnail") {
                    self.newThumbnail.file = null;
                }
                self.setError("info", "Image saved.");

                self.loadConference(self.conference().uuid);
                self.autosave({text: "Ok", css: "label-success"});
            }

            function fail() {
                self.setError("danger", "Unable to save the banner figure.");
            }
        };

        self.removeBanner = function (data, event) {
            var bType = event.target.name.replace("remove-", "");
            var uuid = null;
            if (self.conference().banner() && self.conference().banner().length > 0) {
                self.autosave({text: "Saving", css: "label-warning"});
                for (var i = 0; i < self.conference().banner().length; i++) {
                    if (self.conference().banner()[i].bType === bType) {
                        uuid = self.conference().banner()[i].uuid;
                        break;
                    }
                }
            }

            if (uuid !== null) {
                $.ajax({
                    url: "/api/banner/" + uuid,
                    type: "DELETE",
                    success: successDel,
                    error: fail,
                    cache: false
                });
            } else {
                self.setError("warning", "Unable to delete image: conference has no " + bType + " file.", true);
            }

            function successDel() {
                if (bType === "logo") {
                    self.newLogo.file = null;
                    self.logo(null);
                } else if (bType === "thumbnail") {
                    self.newThumbnail.file = null;
                    self.thumbnail(null);
                }
                self.setError("info", "Image successfully removed.");

                self.loadConference(self.conference().uuid);
                self.autosave({text: "Ok", css: "label-success"});
            }

            function fail() {
                self.setError("Error", "Unable to delete the image.");
                self.autosave({text: "Error!", css: "label-danger"});
            }
        };
    }

    $(document).ready(function() {
        var data = tools.hiddenData();

        window.dashboard = adminConferenceViewModel(data.conferenceUuid, data.accountUuid);
        window.dashboard.init();
    });
});
});
