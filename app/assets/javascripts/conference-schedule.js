require(["main"], function () {
    require(["lib/models", "lib/tools", "knockout"], function(models, tools, ko) {
        "use strict";

        /**
         * Schedule view model.
         *
         *
         * @param confId
         * @returns {ScheduleViewModel}
         * @constructor
         */
        function ScheduleViewModel(confId) {

            if (!(this instanceof ScheduleViewModel)) {
                return new ScheduleViewModel(confId);
            }

            var self = this;
            self.isLoading = ko.observable("Loading conference schedule.");
            self.error = ko.observable(false);
            self.schedulerHeight = ko.observable(2000);
            self.displayNext = ko.observable("block");
            self.displayPrevious = ko.observable("block");
            self.schedule = null;
            self.days = ko.observableArray([]); // number of days and thereby calendar instances of the conference

            self.init = function () {
                ko.applyBindings(window.schedule);
                self.loadConference(confId);
            };

            self.setError = function(level, text) {
                self.error({message: text, level: 'alert-' + level});
                self.isLoading(false);
            };

            //Data IO
            self.ioFailHandler = function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                console.log( "Request Failed: " + err );
                self.setError("danger", "Error while fetching data from server: <br\\>" + error);
            };

            self.loadConference = function(id) {
                if(!self.isLoading()) {
                    self.isLoading("Loading conference schedule.");
                }

                //now load the data from the server
                var confURL ="/api/conferences/" + id;
                $.getJSON(confURL, self.onConferenceData).fail(self.ioFailHandler);
            };

            //conference data
            self.onConferenceData = function (confObj) {
                var conf = models.Conference.fromObject(confObj);
                //now load the schedule data
                $.getJSON(conf.schedule, self.onScheduleData).fail(self.ioFailHandler);
            };

            //schedule data
            self.onScheduleData = function (scheduleObj) {
                self.schedule = models.Schedule.fromObject(scheduleObj);

                var startingDate = self.schedule.getStart();
                var numberOfDays = Math.ceil((self.schedule.getEnd() - startingDate) / (24*60*60*1000));

                for (var i = 0; i < numberOfDays; i++) {
                    self.days.push(new Date(startingDate.getTime() + i*24*60*60*1000));
                }
                /*
                 * Initialising the scheduler must be the last step after loading
                 * all the required data.
                 */
                self.initScheduler();
                self.isLoading(false);
            };

            self.initScheduler = function () {
                // window.dhtmlXScheduler.xy.nav_height = -1; // hide the navigation bar
                window.dhtmlXScheduler.xy.scale_height = -1; // hide the day display
                window.dhtmlXScheduler.xy.scroll_width = -1; // hide the scroll bar
                window.dhtmlXScheduler.config.readonly = true; // disable editing events
                window.dhtmlXScheduler.config.separate_short_events = true; // prevent short events from overlapping
                // window.dhtmlXScheduler.config.mark_now = true; // mark the current time
                /*
                 * Size of the x-axis hour steps.
                 * Must be a multiple of 44 for proper alignment (default skin).
                 * This number may vary between different skins.
                 */
                window.dhtmlXScheduler.config.hour_size_px = 264;

                /*
                 * Split up tracks and sessions upon clicking on the corresponding scheduler event.
                 */
                window.dhtmlXScheduler.attachEvent("onClick", function (id, e) {

                    var splitEvents = (window.dhtmlXScheduler.getEvent(id)).getSplitEvents();
                    if (splitEvents.length > 0) {
                        splitEvents.forEach(function (splitEvent) {
                            window.dhtmlXScheduler.addEvent(splitEvent);
                        });
                        window.dhtmlXScheduler.deleteEvent(id);
                    }
                    // TODO: change IDs
                    // TODO: display infos
                    return true;
                });

                // dynamically scale the hour range (y-axis) for different days
                window.dhtmlXScheduler.attachEvent("onViewChange", function (new_mode, new_date) {
                    // disable buttons if first or last day of the conference
                    if (self.schedule.getStart().toDateString() === new_date.toDateString()) {
                        self.displayPrevious("none");
                    } else {
                        self.displayPrevious("block");
                    }
                    if (self.schedule.getEnd().toDateString() === new_date.toDateString()) {
                        self.displayNext("none");
                    } else {
                        self.displayNext("block");
                    }

                    // scale the scheduler
                    var dailyEvents = self.schedule.getDailyEvents(new_date);
                    var startingDate = null;
                    var endingDate = null;
                    // process all events for the current day to find the start and end point
                    dailyEvents.forEach(function (c) {
                        if (startingDate === null) {
                            startingDate = c.getStart();
                        } else if (startingDate - c.getStart() > 0) {
                            startingDate = c.getStart();
                        }
                        if (endingDate === null) {
                            endingDate = c.getEnd();
                        } else if (endingDate - c.getEnd() < 0) {
                            endingDate = c.getEnd();
                        }
                    });
                    // adjust the y-axis of the scheduler if possible
                    if (startingDate !== null && endingDate !== null) {
                        window.dhtmlXScheduler.config.first_hour = startingDate.getHours();
                        // TODO: maybe restrict this to max 23 hours
                        window.dhtmlXScheduler.config.last_hour = endingDate.getHours() + 1;
                    } else {
                        window.dhtmlXScheduler.config.first_hour = 0;
                        window.dhtmlXScheduler.config.last_hour = 23;
                    }
                    // adjust the height of the scheduler
                    self.schedulerHeight(window.dhtmlXScheduler.xy.nav_height + 1 // + 1 pixel to prevent scroll bar display
                        + (window.dhtmlXScheduler.config.last_hour - window.dhtmlXScheduler.config.first_hour)
                        * window.dhtmlXScheduler.config.hour_size_px);
                    window.dhtmlXScheduler.updateView(); // update scheduler to display all changes
                });

                /*
                 * All the custom logic should be placed inside this event to ensure
                 * the templates are ready before the scheduler is initialised.
                 */
                // window.dhtmlXScheduler.attachEvent("onTemplatesReady",function(){
                // });

                // Initialise all scheduler views.
                // for (var i = 0; i < self.days().length; i++) {
                //     window.dhtmlXScheduler.init("conference_scheduler_"+i,self.days()[i],"day");
                // }
                window.dhtmlXScheduler.init("conference_scheduler",self.days()[0],"day");
                /*
                 * Add all the events from the conference schedule.
                 * Event IDs correspond to the index of the element in the specified schedule
                 * in the format: "index of layer 1":"index of layer 2":"index of layer 3".
                 * A negative value means the layer is not applicable for this element.
                 *
                 * Example schedule:
                 * [Event0, Track1[Event1-0], Session2[Track2-0[Event2-0-0]]]
                 *
                 * Session2 is the third layer 1 element thereby its ID is 2:-1:-1. Layer 2 and 3 are not applicable.
                 * Track2-0 is the first layer 2 element contained by the layer 1 element Session2 thereby its ID is 2:0:-1.
                 * Event2-0-0 is the first layer 3 element contained by the layer 2 element Track2-0 thereby its ID is 2:0:0.
                 */
                var contentIndex = 0;
                self.schedule.content.forEach(function (event) {
                    var schedulerEvent = models.SchedulerEvent.fromObject(event);
                    schedulerEvent.id = contentIndex++ + ":-1:-1";
                    window.dhtmlXScheduler.addEvent(schedulerEvent);
                });

            };

        };

        $(document).ready(function() {

            var data = tools.hiddenData();

            console.log(data.conferenceUuid);

            window.schedule = ScheduleViewModel(data.conferenceUuid);
            window.schedule.init();
        });

    });
});