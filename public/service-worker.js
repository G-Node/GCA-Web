self.addEventListener("install", function(event) {
    event.waitUntil(
        caches.open('v1').then(function(cache) {
            return cache.addAll([
                // Views
                "/",
                "/conferences",
                "/contact",
                "/about",
                "/impressum",
                // Assets
                "/assets/lib/bootstrap/js/bootstrap.min.js",
                "/assets/stylesheets/g-node-bootstrap.play.css",
                "/assets/lib/jquery/jquery.min.js",
                "/assets/lib/jquery-ui/jquery-ui.min.css",
                "/assets/stylesheets/layout.css",
                "/assets/javascripts/require.js",
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
                "/assets/javascripts/lib/scheduler/sources/dhtmlxscheduler.js.map",
                "/assets/javascripts/lib/scheduler/sources/ext/dhtmlxscheduler_readonly.js.map",
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
            ]);
        })
    );
});

self.addEventListener('fetch', function(event) {
    console.log("in");
    event.respondWith(caches.match(event.request).then(function(response) {
        if (response) {
            return response;
        } else {
            return fetch(event.request).then(function (response) {
                var responseClone = response.clone();
        //         if (responseClone.redirected) {
        //             responseClone = cleanResponse(responseClone);
        //         }
                caches.open("v1").then(function (cache) {
                    cache.put(event.request, responseClone);
                });
                return response;
            }).catch(function () {
                console.log("Retrieval of " + event.request + " from cache failed.");
                return caches.match("/conferences");
            });
        }
    }));
});