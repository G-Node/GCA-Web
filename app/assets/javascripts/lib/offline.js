/**
 * Module for offline functionality
 * @module {lib/offline}
 */
define([], function() {
    "use strict";

    // Constant.
    var _updateKey = "lastUpdated";

    // The local storage is checked and ,if necessary, updated every time a page is loaded.
    updateStorage();

    /*
     * Update the local storage and try to store all possible requests
     * in there.
     */
    function updateStorage () {
        var lastUpdate = localStorage.getItem(_updateKey);
        // Check if there has already been an update and if it is older than a day.
        if (lastUpdate === null || lastUpdate === undefined || new Date() - lastUpdate > 86400000) {
            // Reset of the timer happens inside this function to prevent timer reset when offline.
            $.getJSON("/api/conferences", onConferences);
        }
    }

    // Write all conferences to the local storage.
    function onConferences(confs) {
        if (confs !== null) {
            confs.forEach(function (conf) {
                localStorage.setItem(conf.uuid, JSON.stringify(conf));
                $.getJSON(conf.abstracts, onAbstracts);
                $.getJSON(conf.geo, function (confUuid) {
                    return function(data) {
                        onLocation(confUuid, data);
                    };
                }(conf.uuid));
                $.getJSON(conf.schedule, function (confUuid) {
                    return function(data) {
                        onSchedule(confUuid, data);
                    };
                }(conf.uuid));
                $.getJSON(conf.info, function (confUuid) {
                    return function(data) {
                        onInfo(confUuid, data);
                    };
                }(conf.uuid));
            });
        }
        // Reset the update timer.
        localStorage.setItem(_updateKey, (new Date()).getTime().toString());
    }

    // Write all abstracts to the local storage.
    function onAbstracts(abs) {
        if (abs !== null) {
            abs.forEach(function (abstract) {
                localStorage.setItem(abstract.uuid, JSON.stringify(abstract));
            });
        }
    }

    // Write a conference location to the local storage.
    function onLocation(confUuid, loc) {
        localStorage.setItem(confUuid+"geo", JSON.stringify(loc));
    }

    // Write a conference schedule to the local storage.
    function onSchedule(confUuid, schedule) {
        localStorage.setItem(confUuid+"schedule", JSON.stringify(schedule));
    }

    // Write conference information to the local storage.
    function onInfo(confUuid, info) {
        localStorage.setItem(confUuid+"info", JSON.stringify(info));
    }

    /*
     * Request a JSON from a specific url and hand it over to a specific function if
     * successfully retrieved, otherwise call the fail handler.
     * The request will first try to fetch the resource from the server. If this fails the
     * local storage will be searched by the specified fallback key.
     */
    function requestJSON (key, url, successFunction, failHandler) {
        $.getJSON(url, successFunction).fail(function (keyHandover, successFunctionHandover,
                                                       failHandlerHandover) {
            return function (jqxhr, textStatus, error) {
                var stored = localStorage.getItem(keyHandover);
                if (stored !== null && stored !== undefined) {
                    successFunctionHandover(stored);
                } else {
                    failHandlerHandover(jqxhr, textStatus, error);
                }
            }
        }(key, successFunction, failHandler));
    }

    return {
        requestJSON: requestJSON
    };

});