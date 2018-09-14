var _cacheVersion = "v8";

var resourcesToCache = [
    // Views
    "/",
    "/conferences",
    "/contact",
    "/about",
    "/impressum",
    "/login",
    // Assets
    "/assets/lib/momentjs/moment.js",
    "/assets/lib/bootstrap/js/bootstrap.min.js",
    "/assets/lib/bootstrap/js/bootstrap.js",
    "/assets/stylesheets/g-node-bootstrap.play.css",
    "/assets/lib/jquery/jquery.js",
    "/assets/lib/jquery/jquery.min.js",
    "/assets/lib/jquery-ui/jquery-ui.min.css",
    "/assets/lib/jquery-ui/jquery-ui.js",
    "/assets/stylesheets/layout.css",
    "/assets/javascripts/require.js",
    "/assets/lib/requirejs/require.js",
    "/assets/lib/sammy/sammy.js",
    "/assets/images/favicon.png",
    "/assets/images/bccn.png",
    "/assets/images/gnode_logo.png",
    "/assets/fonts/glyphicons-halflings-regular.eot",
    "/assets/fonts/glyphicons-halflings-regular.svg",
    "/assets/fonts/glyphicons-halflings-regular.ttf",
    "/assets/fonts/glyphicons-halflings-regular.woff",
    "/assets/javascripts/knockout-sortable.min.js",
    // leaflet
    "/assets/javascripts/lib/leaflet/leaflet.css",
    "/assets/javascripts/lib/leaflet/leaflet.js",
    "/assets/javascripts/lib/leaflet/leaflet-src.js",
    "/assets/javascripts/lib/leaflet/images/layers.png",
    "/assets/javascripts/lib/leaflet/images/layers-2x.png",
    "/assets/javascripts/lib/leaflet/images/marker-icon.png",
    "/assets/javascripts/lib/leaflet/images/marker-icon-2x.png",
    "/assets/javascripts/lib/leaflet/images/marker-shadow.png",
    // scheduler
    "/assets/javascripts/lib/scheduler/dhtmlxscheduler.css",
    "/assets/javascripts/lib/scheduler/dhtmlxscheduler.js",
    "/assets/javascripts/lib/scheduler/ext/dhtmlxscheduler_readonly.js",
    // libs
    "/assets/javascripts/lib/accessors.js",
    "/assets/javascripts/lib/astate.js",
    "/assets/javascripts/lib/models.js",
    "/assets/javascripts/lib/msg.js",
    "/assets/javascripts/lib/multi.js",
    "/assets/javascripts/lib/offline.js",
    "/assets/javascripts/lib/owned.js",
    "/assets/javascripts/lib/tools.js",
    "/assets/javascripts/lib/update-storage.js",
    "/assets/javascripts/lib/validate.js",
    // view models
    "/assets/javascripts/abstract-list.js",
    "/assets/javascripts/abstract-viewer.js",
    "/assets/javascripts/browser.js",
    "/assets/javascripts/conference-schedule.js",
    "/assets/javascripts/config.js",
    "/assets/javascripts/editor.js",
    "/assets/javascripts/locations.js",
    "/assets/javascripts/main.js",
    "/assets/javascripts/userdash.js",

    "https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.1/jquery-ui-timepicker-addon.min.js",
    "https://cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-debug.js",
    // Styles
    "/assets/stylesheets/_g-node-bootstrap.less",
    "/assets/stylesheets/g-node-bootstrap.play.less",
    "/assets/stylesheets/layout.less",
    "/assets/stylesheets/Readme.md",
    "/assets/stylesheets/custom/_classes.less",
    "/assets/stylesheets/custom/_classes_conference_scheduler.less",
    "/assets/stylesheets/custom/_colors.less",
    "/assets/stylesheets/custom/_font.less",
    "/assets/stylesheets/custom/bootstrap/_custom-colors.less",
    "/assets/stylesheets/custom/bootstrap/_custom-elements.less",
    "/assets/stylesheets/custom/bootstrap/_custom-fonts.less",
    "/assets/stylesheets/custom/bootstrap/_custom-vars.less"
];

self.addEventListener("install", function(event) {
    event.waitUntil(
        loadDynamicViews().then(function (views) {
            if (views) {
                views.forEach(function (view) {
                    if (view) {
                        resourcesToCache.push(view);
                    }
                })
            }
            return caches.open(_cacheVersion);
        }).then(function(cache) {
            console.log(resourcesToCache);
            return cache.addAll(resourcesToCache);
        })
    );

});

/*
 * Get a promise containing all dynamic conference and abstract views.
 */
function loadDynamicViews () {
    return new Promise(function (resolve, reject) {
        var dynamicConferences = [];
        var accordingAbstracts = [];
        fetch("/api/conferences").then(function (response) {
            return response.json();
        }).then(function (conferences) {
            conferences.forEach(function (conf) {
                if (conf) {
                    dynamicConferences.push("/conference/" + conf.short);
                    dynamicConferences.push("/conference/" + conf.short + "/schedule");
                    dynamicConferences.push("/conference/" + conf.short + "/submission");
                    dynamicConferences.push("/conference/" + conf.short + "/floorplans");
                    dynamicConferences.push("/conference/" + conf.short + "/locations");
                    dynamicConferences.push("/conference/" + conf.short + "/abstracts");
                    accordingAbstracts.push(loadDynamicAbstracts(conf.abstracts));
                }
            });
            Promise.all(accordingAbstracts).then(function (allAbstracts) {
                allAbstracts.forEach(function (abstracts) {
                    if (abstracts) {
                        abstracts.forEach(function (abs) {
                            if (abs) {
                                dynamicConferences.push(abs);
                            }
                        });
                    }
                });
                resolve(dynamicConferences);
            });
        }).catch(function (reason) {
            reject(reason);
        });
    });
}

// Get a promise containing all abstracts for the given URL.
function loadDynamicAbstracts (abstractsURL) {
    return new Promise(function (resolve, reject) {
        var dynamicAbstracts = [];
        fetch(abstractsURL).then(function (response) {
            return response.json();
        }).then(function (abstracts) {
            abstracts.forEach(function (abs) {
                if (abs) {
                    dynamicAbstracts.push("/abstracts/" + abs.uuid);
                }
            });
            resolve(dynamicAbstracts);
        }).catch(function (reason) {
            console.log("Could not fetch " + abstractsURL + " for the following reason:"
            + JSON.stringify(reason, null, 4));
            // This must resolve anyhow and not get rejected for the rest of the code to work.
            resolve(false);
        });
    });
}

self.addEventListener("fetch", function(event) {
    canConnectToServer().then(function (connected) {
        if (connected) {
            console.log("Online");
            caches.match(event.request).then(function(response) {
                var responseClone = response.clone();
                if (!responseClone) {
                    caches.open(_cacheVersion).then(function (cache) {
                        cache.put(event.request, responseClone);
                    });
                }
            });
            return event.response;
        } else {
            event.respondWith(caches.match(event.request).then(function(response) {
                var responseClone = response.clone();
                console.log("Offline!");
                console.log(JSON.stringify(responseClone,null,4));
                if (responseClone) {
                    console.log(JSON.stringify(responseClone.url,null,4));
                    console.log(JSON.stringify(responseClone.redirected,null,4));
                    if (responseClone.redirected) { // Fix redirected links.
                        console.log("Redirect");
                        return unredirect(responseClone);
                    }else {
                        console.log("Normal response");
                        return responseClone;
                    }
                } else { // The resource could not be loaded.
                    console.log("Failed!");
                    return caches.match("/conferences");
                }
            }));
        }

    });



    // Normally load the stuff from the server and only fallback to cache if loading is not working.
    // if (event.response && event.response.ok) {
    //     return event.response;
    // } else {
    //     event.respondWith(caches.match(event.request).then(function(response) {
    //         if (response) {
    //             if (response.redirected) { // Fix redirected links.
    //                 return unredirect(response);
    //             }else {
    //                 return response;
    //             }
    //         } else { // The resource could not be loaded.
    //             return event.response;
    //             // return fetch(event.request).then(function (response) {
    //             //
    //             //     var responseClone = response.clone();
    //             //
    //             //     caches.open(_cacheVersion).then(function (cache) {
    //             //         cache.put(event.request, responseClone);
    //             //     });
    //             //     return response;
    //             // }).catch(function () {
    //             //     console.log("Retrieval of " + JSON.stringify(event.request,null,4) + " from cache failed.");
    //             //     return caches.match("/conferences");
    //             // });
    //         }
    //     }));
    // }
});

// Helper function to clean redirected responses.
function unredirect(response) {
    return response.text().then(function (text) {
        return new Response(text, {
            headers: response.headers,
            status: response.status,
            statusText: response.statusText
        });
    });
}

// Check if a connection to the server can be established.
function canConnectToServer () {
    return new Promise(function (resolve, reject) {
        fetch("/").then(function (response) {
            resolve(true);
        }).catch(function (reason) {
            resolve(false);
        });
    });
}
