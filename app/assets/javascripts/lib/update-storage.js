// Constant.
var _updateKey = "lastUpdated";

// The local storage is checked and ,if necessary, updated every time a page is loaded.
updateStorage();

// Start the service worker.
if ("serviceWorker" in navigator) {
    navigator.serviceWorker.register('/service-worker.js').then(function(reg) {
        console.log("Registered service worker with scope: " + reg.scope);
    }).catch(function(error) {
        console.log("Registering service worker failed with error: " + error);
    });
};

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
    if (confs) {
        confs.forEach(function (conf) {
            localStorage.setItem(conf.uuid, JSON.stringify(conf));
            // TODO: fix exception on emtpy abstracts
            $.getJSON(conf.abstracts, function (confUuid) {
                return function(data) {
                    onAbstracts(confUuid, data);
                };
            }(conf.uuid));
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
function onAbstracts(confUuid, abs) {
    localStorage.setItem(confUuid+"abstracts", JSON.stringify(abs));
    if (abs) {
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