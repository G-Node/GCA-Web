require(["lib/models", "lib/tools"], function(models, tools) {
    "use strict";


    /**
     * AbstractList view model.
     *
     *
     * @param confId
     * @returns {AbstractListViewModel}
     * @constructor
     */
    function AbstractListViewModel(confId) {

        if (! (this instanceof AbstractListViewModel)) {
            return new AbstractListViewModel(confId);
        }

        var self = this;
        self.abstractsData = null;
        self.conference = ko.observable();
        self.abstracts = ko.observableArray(null);
        self.selectedAbstract = ko.observable(null);

        //maps for uuid -> abstract and doi -> abstract
        self.uuidMap = {};


        self.init = function() {
            ko.applyBindings(window.abstractList);
        };


        self.makeLink = function(abstract) {
            return '#' + '/uuid/' + abstract.uuid;
        };

        self.selectAbstract = function(abstract) {
            console.log("Selecting abstract " + abstract.uuid + " " + abstract.toString());
            window.location = self.makeLink(abstract);
        };

        self.showAbstract = function(abstract) {

            self.abstracts(null);
            self.selectedAbstract(abstract);
            document.title = abstract.title; //FIXME add conference
        };

        self.showAbstractByUUID = function(uuid) {

            if(!uuid in self.uuidMap) {
                console.log("Warning uuid to show not in map");
                return;
            }

            self.showAbstract(self.uuidMap[uuid]);
        };

        self.showAbstractList = function(theList) {
            self.selectedAbstract(null);
            self.abstracts(theList);
            document.title = self.conference().name;
        };

        //map related stuff
        self.buildMaps = function() {
            self.uuidMap = {}; //empty the map
            for (var i = 0; i < self.abstractsData.length; i++) {
                var currentAbstract = self.abstractsData[i];
                self.uuidMap[currentAbstract.uuid] = currentAbstract;
            }
        };


        //Data IO
        self.ioFailHandler = function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log( "Request Failed: " + err );
        };

        self.ensureDataAndThen = function(doAfter) {
            console.log("ensureDataAndThen::");
            if (self.abstractsData !== null) {
                doAfter();
                return;
            }

            //now load the data from the server
            var confURL ="/api/conferences/" + confId;
            $.getJSON(confURL, onConferenceData).fail(self.ioFailHandler);

            //conference data
            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                self.conference(conf);

                //now load the abstract data
                $.getJSON(conf.abstracts, onAbstractData).fail(self.ioFailHandler);
            }

            //abstract data
            function onAbstractData(absArray) {
                var absList = models.Abstract.fromArray(absArray);
                self.abstractsData = absList;
                self.buildMaps();
                self.abstracts(absList);

                doAfter();
            }
        };

        // client-side routes
        Sammy(function() {

            this.get('#/uuid/:uuid', function() {
                var uuid = this.params['uuid'];
                console.log("Sammy::get::uuid [" + uuid + "]");
                self.ensureDataAndThen(function () {
                    self.showAbstractByUUID(uuid);
                });
            });

            this.get('', function() {
                console.log('Sammy::get::');
                self.ensureDataAndThen(function () {
                    self.showAbstractList(self.abstractsData);
                });
            });

        }).run();

    }


    $(document).ready(function() {

        var data = tools.hiddenData();

        console.log(data.conferenceUuid);

        window.abstractList = AbstractListViewModel(data.conferenceUuid);
        window.abstractList.init();
    });

});