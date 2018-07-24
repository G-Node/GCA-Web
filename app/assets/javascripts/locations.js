require(["main"], function () {
require(["lib/models", "lib/tools", "lib/leaflet/leaflet", "lib/msg", "lib/astate", "knockout"], function(models, tools, msg, leaflet, astate, ko) {
    "use strict";

    function LocationsViewModel(confId) {

        if (!(this instanceof LocationsViewModel)) {
            return new LocationsViewModel(confId);
        }

        var self = tools.inherit(this, msg.MessageBox);

        self.conference = ko.observable(null);
        self.geoContent = ko.observable(null);
        self.stateLog = ko.observable(null);

        self.init = function() {
            self.loadConference(confId);
            ko.applyBindings(window.viewer);
            MathJax.Hub.Configured(); //start MathJax
        };

        self.ioFailHandler = function(jqxhr, textStatus, error) {
            self.setError("Error", "Unable to load the conference with uuid = " + confId);
        };

        self.loadConference = function(confId) {
            var confUrl = "/api/conferences/" + confId; // we should be reading this from the conference

            $.getJSON(confUrl, onConferenceData).fail(self.ioFailHandler);

            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);
                $.getJSON(self.conference().geo, onGeoData).fail(self.ioFailHandler);

                function onGeoData(geojson){
                    console.log(JSON.stringify(geojson[0]));
                    //map
                    $('#map-div').height($('#map-div').width());
                    var confmap = L.map('map-div');
                    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
                        attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
                        maxZoom: 18,
                        id: 'mapbox.satellite',
                        accessToken: 'your.mapbox.access.token'
                    }).addTo(confmap);
                    var texts = '';

                    //floorplan
                    $('#floorplan-div').height($('#floorplan-div').width());
                    var floor = L.map('floorplan-div', {
                        crs: L.CRS.Simple,
                        minZoom: -5
                    });

                    for(var i=0;i< geojson.length; i++) {
                        //get map coordinates
                        var coords = geojson[i].location.position.geojson.coordinates
                        texts += geojson[i].uuid +'\n';
                        //set view only once
                        if(i==0){confmap.setView(coords, 13);}
                        //add markers and descriptions
                        var marker = L.marker(coords).addTo(confmap);
                        marker.bindPopup('<b>Conference location</b><br>' + geojson[i].uuid );

                        //floorplan coordinates (from json later on)
                        var bounds = [[0,0], [1000,1000]];
                        var image = L.imageOverlay('https://wcs.smartdraw.com/floor-plan/img/building-plan-example.png?bn=1510011132', bounds).addTo(floor);
                        floor.fitBounds(bounds);
                        var marker = L.marker([450,250]).addTo(floor);
                        marker.bindPopup('<b>Event Room</b>');

                    }
                    //add addresses as text
                    self.geoContent(texts);

                }
            }

        };

    }

    $(document).ready(function() {

        var data = tools.hiddenData();

        window.viewer = LocationsViewModel(data["conferenceUuid"]);

        window.viewer.init();

    });

});
});