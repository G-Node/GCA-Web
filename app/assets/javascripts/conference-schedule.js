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

            self.init = function () {
                ko.applyBindings(window.schedule);
                self.initScheduler();
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
            function onConferenceData(confObj) {
                var conf = models.Conference.fromObject(confObj);
                //now load the schedule data
                $.getJSON(conf.schedule, onScheduleData).fail(self.ioFailHandler);
            }

            //schedule data
            function onScheduleData(scheduleObj) {
                // TODO: implement loading schedule data
                self.isLoading(false);
            }

            self.initScheduler = function () {
                // Actually unnecessary, as the tab is not displayed anyways.
                window.dhtmlXScheduler.locale.labels.conference_scheduler_tab = "Conference Scheduler";
                window.dhtmlXScheduler.xy.nav_height = 0; // hide the navigation bar

                /*
                 * All the custom logic should be placed inside this event to ensure
                 * the templates are ready before the scheduler is initialised.
                 */
                scheduler.attachEvent("onTemplatesReady",function(){
                    window.dhtmlXScheduler.date.conference_scheduler_start = function (active_date) {
                        return window.dhtmlXScheduler.date.week_start(active_date);
                    };
                    window.dhtmlXScheduler.date.get_conference_scheduler_end = function (start_date) {
                        return window.dhtmlXScheduler.date.add(start_date,5,"day");
                    };
                    window.dhtmlXScheduler.date.add_conference_scheduler = function (date, inc) {
                        return window.dhtmlXScheduler.date.add(date,inc*7,"day");
                    };
                    window.dhtmlXScheduler.templates.conference_scheduler_date = function(start, end){
                        return window.dhtmlXScheduler.templates.day_date(start)+" &ndash; "+
                            window.dhtmlXScheduler.templates.day_date(window.dhtmlXScheduler.date.add(end,-1,"day"));
                    };
                    window.dhtmlXScheduler.templates.conference_scheduler_scale_date = window.dhtmlXScheduler.templates.week_scale_date;
                });
                window.dhtmlXScheduler.init("conference_scheduler",new Date(2018,0,1),"conference_scheduler");
            }
        }


        function Track (title, subtitle, chair, events) {

            if (!(this instanceof Track)) {
                return new Track(title, subtitle, chair, events);
            }

            var self = this;

            self.title = title || null;
            self.subtitle = subtitle || null;
            self.chair = chair || null;
            self.events = events || null;

            // look at all the events to find the starting date of the track
            self.getStart = function () {
                var startingDate = null;

                for (var e in self.events) {
                    if (self.events.hasOwnProperty(e)) {
                        if (startingDate === null) {
                            startingDate = e.getStart();
                        } else if (startingDate - e.getStart() > 0) {
                            startingDate = e.getStart();
                        }
                    }
                }
                return startingDate;
            };

            // look at all the events to find the ending date of the track
            self.getEnd = function () {
                var endingDate = null;

                for (var e in self.events) {
                    if (self.events.hasOwnProperty(e)) {
                        if (endingDate === null) {
                            endingDate = e.getEnd();
                        } else if (endingDate - e.getEnd() < 0) {
                            endingDate = e.getEnd();
                        }
                    }
                }
                return endingDate;
            };

        };

        function Session (title, subtitle, tracks) {

            if (!(this instanceof Session)) {
                return new Session(title, subtitle, tracks);
            }

            var self = this;

            self.title = title || null;
            self.subtitle = subtitle || null;
            self.tracks = tracks || null;

            // look at all the tracks to find the starting date of the session
            self.getStart = function () {
                var startingDate = null;

                for (var t in self.tracks) {
                    if (self.tracks.hasOwnProperty(t)) {
                        if (startingDate === null) {
                            startingDate = t.getStart();
                        } else if (startingDate - t.getStart() > 0) {
                            startingDate = t.getStart();
                        }
                    }
                }
                return startingDate;
            };

            // look at all the tracks to find the ending date of the session
            self.getEnd = function () {
                var endingDate = null;

                for (var t in self.tracks) {
                    if (self.tracks.hasOwnProperty(t)) {
                        if (endingDate === null) {
                            endingDate = t.getEnd();
                        } else if (endingDate - t.getEnd() < 0) {
                            endingDate = t.getEnd();
                        }
                    }
                }
                return endingDate;
            };

        };

        function Event (title, subtitle, start, end, date, location, authors, type, abstract) {

            if (!(this instanceof Event)) {
                return new Event(title, subtitle, start, end, date, location, authors, type, abstract);
            }

            var self = this;

            self.title = title || null;
            self.subtitle = subtitle || null;
            self.start = start || null;
            self.end = end || null;
            self.date = date || null;
            self.location = location || null;
            self.authors = authors || null;
            self.type = type || null;
            self.abstract = abstract || null;

            self.getStart = function () {
                // format year-month-day
                var ymd = self.date.split("-");
                // format hour:minute
                var time = self.start.split(":");
                return new Date(parseInt(ymd[0]), parseInt(ymd[1]), parseInt(ymd[2]), parseInt(time[0]), parseInt(time[1]));
            };

            self.getEnd = function () {
                // format year-month-day
                var ymd = self.date.split("-");
                // format hour:minute
                var time = self.end.split(":");
                return new Date(parseInt(ymd[0]), parseInt(ymd[1]), parseInt(ymd[2]), parseInt(time[0]), parseInt(time[1]));
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