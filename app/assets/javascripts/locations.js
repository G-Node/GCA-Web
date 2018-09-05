require(["main"], function () {
require(["lib/models", "lib/tools", "lib/leaflet/leaflet", "lib/msg", "lib/astate", "knockout"], function(models, tools, msg, leaflet, astate, ko) {
    "use strict";

    function LocationsViewModel(confId, mapType) {

        if (!(this instanceof LocationsViewModel)) {
            return new LocationsViewModel(confId, mapType);
        }

        var self = tools.inherit(this, msg.MessageBox);

        self.mapType = mapType;

        self.conference = ko.observable(null);
        self.geoContent = ko.observable(null);
        self.stateLog = ko.observable(null);

        //observables as iterative list

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

                function onGeoData(geojson) {
                    //depending on whether locations or floor plan page
                    if (self.mapType == "locations") {
                        //map
                        $('#map-div').height(0.75*$('#map-div').width());
                        var confmap = L.map('map-div');
                        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
                            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
                            maxZoom: 20,
                            id: 'mapbox.streets',
                            accessToken: 'pk.eyJ1IjoiZ25vZGUiLCJhIjoiY2prOGFnbzY2MWlmNzN3bzRhY205N2oxZCJ9.-j7b1aziK9nUNjgHQh0ojw'
                        }).addTo(confmap);
                        var texts = '';

                        for (var i = 0; i < geojson.length; i++) {
                            //get map coordinates
                            var coords = [geojson[i].point.lat,geojson[i].point.long]
                            texts += geojson[i].name + '\n';
                            //set view only once
                            if (i == 0) {
                                confmap.setView(coords, 13);
                            }
                            //add markers and descriptions
                            var marker = L.marker(coords).addTo(confmap);
                            marker.bindPopup('<b>' + geojson[i].name + '</b><br>' + geojson[i].description).openPopup();
                        }
                    } else if (self.mapType == "floorplans") {
                        //load all floorplans on one page
                        for (var i = 0; i < geojson.length; i++) {
                            //load floorplans, if they're included
                            if(geojson[i].floorplans) {
                                $('#floorplans-wrap').append('<h2>' + geojson[i].name + '</h2><p>' + geojson[i].description + '</p>');

                                for(var j=0; j<geojson[i].floorplans.length; j++) {

                                    $('#floorplans-wrap').append('<div id="floorplan-div-' + i + + j + '" class="floorplan-divs" style="margin-bottom: 1em;"></div>');

                                    //create image in order to get actual image dimensions cross browser
                                    var img = $("<img>").attr("src", geojson[i].floorplans[j]);
                                    var nw = img.prop('naturalWidth');
                                    var nh = img.prop('naturalHeight');

                                    var floor = L.map('floorplan-div-' + i + j, {
                                        crs: L.CRS.Simple,
                                        minZoom: 0
                                    });

                                    if(nh>nw){
                                        $('#floorplan-div-'+i+j).height($('#floorplan-div-'+i+j).width());
                                        $('#floorplan-div-'+i+j).width(nw/nh*$('#floorplan-div-'+i+j).height());
                                    }else{
                                        $('#floorplan-div-'+i+j).height(nh/nw*$('#floorplan-div-'+i+j).width());
                                    }

                                    var bounds  = [[0, 0], [$('#floorplan-div-'+i+j).height(), $('#floorplan-div-'+i+j).width()]];
                                    var image = L.imageOverlay(geojson[i].floorplans[j], bounds).addTo(floor);
                                    floor.fitBounds(bounds);
                                    //possible to incorporate rooms with coordinates just as above,
                                    //just be sure to use right coordinate system (cf. leaflet page)
                                    //and mapping probably needed for room number vs. location
                                }
                            }
                        }
                    }
                    //add addresses as text
                    //self.geoContent(texts);

                }
            }

        };

    }

    $(document).ready(function() {

        var data = tools.hiddenData();

        window.viewer = LocationsViewModel(data["conferenceUuid"],data["mapType"]);

        window.viewer.init();

    });

});
});