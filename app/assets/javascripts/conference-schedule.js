require(["main"], function () {
    require(["lib/models", "lib/tools", "knockout", "moment"], function(models, tools, ko, moment) {
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
                console.log("loadConference::");
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
                    self.days.push(startingDate.setDate(startingDate.getDate() + i));
                }
                /*
                 * Initialising the scheduler must be the last step after loading
                 * all the required data.
                 */
                self.initScheduler();
                self.isLoading(false);
            };

            self.initScheduler = function () {
                // Actually unnecessary, as the tab is not displayed anyways.
                window.dhtmlXScheduler.locale.labels.conference_scheduler_tab = "Conference Scheduler";
                window.dhtmlXScheduler.xy.nav_height = -1; // hide the navigation bar
                window.dhtmlXScheduler.xy.scale_height = -1; // hide the day display
                window.dhtmlXScheduler.config.readonly = true; // disable editing events

                /*
                 * All the custom logic should be placed inside this event to ensure
                 * the templates are ready before the scheduler is initialised.
                 */
                // scheduler.attachEvent("onTemplatesReady",function(){
                //     window.dhtmlXScheduler.date.conference_scheduler_start = function (active_date) {
                //         return window.dhtmlXScheduler.date.day_start(active_date);
                //     };
                //     window.dhtmlXScheduler.date.get_conference_scheduler_end = function (start_date) {
                //         return window.dhtmlXScheduler.date.add(start_date,0,"day");
                //     };
                //     window.dhtmlXScheduler.date.add_conference_scheduler = function (date, inc) {
                //         return window.dhtmlXScheduler.date.add(date,0,"day");
                //     };
                //     window.dhtmlXScheduler.templates.conference_scheduler_date = function(start, end){
                //         return window.dhtmlXScheduler.templates.day_date(start)+" &ndash; "+
                //             window.dhtmlXScheduler.templates.day_date(window.dhtmlXScheduler.date.add(end,-1,"day"));
                //     };
                //     // hide the date
                //     window.dhtmlXScheduler.templates.conference_scheduler_scale_date = function (date) {
                //          return "";
                //     };
                // });

                // Initialise all scheduler views.
                // for (var i = 0; i < self.days().length; i++) {
                //     window.dhtmlXScheduler.init("conference_scheduler_"+i,self.days()[i],"day");
                // }
                window.dhtmlXScheduler.init("conference_scheduler_0",self.days()[0],"day");
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
                    console.log(JSON.stringify(schedulerEvent, null, 4));
                    window.dhtmlXScheduler.addEvent(schedulerEvent);
                });

            };

            /*
             * Activate and deactivate the corresponding day's scheduler upon clicking.
             */
            self.toggleScheduler = function (index) {
                var toggle = document.getElementById("conference_scheduler_" + index);
                if (toggle !== null) {
                    if (toggle.style.display === "none") {
                        toggle.style.display = "block";
                    } else {
                        toggle.style.display = "none";
                    }
                }
            };

        };

        $(document).ready(function() {

            var data = tools.hiddenData();

            console.log(data.conferenceUuid);

            window.moment = moment;
            window.schedule = ScheduleViewModel(data.conferenceUuid);
            window.schedule.init();
        });

    });
});