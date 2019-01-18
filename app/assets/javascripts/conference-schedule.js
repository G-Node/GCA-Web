require(["main"], function () {
    require(["lib/models", "lib/tools", "knockout", "moment", "lib/offline"], function(models, tools, ko, moment, offline) {
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
            self.customNavbar = false;
            self.isLoading = ko.observable("Loading conference schedule");
            self.error = ko.observable(false);
            self.schedulerHeight = ko.observable(2000);
            // navbar observables
            self.displayNext = ko.observable("block");
            self.displayPrevious = ko.observable("block");
            self.displayDate = ko.observable("block");
            self.displayCustomNavbar = ko.observable("none");
            self.displayCustomDates = ko.observableArray([]);
            self.displayHighlightDate = ko.observableArray([]);
            // info panel observables
            self.infoEventType = ko.observable(null);
            self.infoID = ko.observable(null);
            self.infoBaseEvent = ko.observable(null);
            self.infoAbstract = ko.observable("");
            self.infoIsLoadingAbstract = ko.observable(false);
            self.infoError = ko.observable(false);
            self.infoChair = ko.observable(null);
            self.infoAuthors = ko.observable(null);
            self.schedule = null;
            // dates of the conference
            self.days = ko.observableArray([]);

            self.init = function () {
                ko.applyBindings(window.schedule);
                self.loadConference(confId);
            };

            self.setError = function(level, text) {
                self.error({message: text, level: "alert-" + level});
                self.isLoading(false);
            };

            self.infoSetError = function(level, text) {
                self.infoError({message: text, level: "alert-" + level});
                self.infoIsLoadingAbstract(false);
            };

            // Data IO
            self.ioFailHandler = function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                self.setError("danger", "Error while fetching data from server: <br\\>" + err);
            };

            self.infoIoFailHandler = function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                self.infoSetError("danger", "Error while fetching data from server: <br\\>" + err);
            };

            self.loadConference = function(id) {
                if (!self.isLoading()) {
                    self.isLoading("Loading conference schedule.");
                }

                var confURL = "/api/conferences/" + id;
                offline.requestJSON(id, confURL, self.onConferenceData, self.ioFailHandler);
            };

            // Conference data
            self.onConferenceData = function (confObj) {
                var conf = models.Conference.fromObject(confObj);
                offline.requestJSON(confObj.uuid + "schedule", confObj.schedule, self.onScheduleData, self.ioFailHandler);
            };

            // Schedule data
            self.onScheduleData = function (scheduleObj) {
                try {
                    self.schedule = models.Schedule.fromObject(scheduleObj);

                    var startingDate = self.schedule.getStart();
                    // Use the start of the day for comparisons.
                    startingDate = new Date(startingDate.getFullYear(), startingDate.getMonth(), startingDate.getDate());
                    var numberOfDays = Math.ceil((self.schedule.getEnd() - startingDate) / (24 * 60 * 60 * 1000));

                    for (var i = 0; i < numberOfDays; i++) {
                        self.days.push(new Date(startingDate.getTime() + i * 24 * 60 * 60 * 1000));
                    }

                    // Initialising the scheduler must be the last step after loading all the required data.
                    self.initScheduler();
                    self.isLoading(false);
                } catch (e) {
                    self.setError("danger", "Error while parsing the conference schedule: Schedule Format Error");
                    throw e;
                }
            };

            /*
            * Load the abstract from a specific URL for display in the modal
            * info popup. Execute some function afterwards if required.
            */
            self.infoLoadAbstract = function (abstractURL, doAfer) {
                self.infoAbstract("");
                self.infoError(false);
                if (abstractURL) {
                    self.infoIsLoadingAbstract("Loading abstract");
                    var abstractUuid = abstractURL.substring(abstractURL.lastIndexOf("/") + 1);
                    offline.requestJSON(abstractUuid, abstractURL, onAbstractData, self.infoIoFailHandler);
                } else {
                    self.infoIsLoadingAbstract(false);
                }

                function onAbstractData (abstractObj) {
                    if (abstractObj !== null && abstractObj !== undefined) {
                        self.infoAbstract(models.Abstract.fromObject(abstractObj));
                        if (doAfer !== null && doAfer !== undefined) {
                            doAfer();
                        }
                        self.infoIsLoadingAbstract(false);
                    }
                }
            };

            /*
             * Create the URL to the public abstract view based on the conference URL.
             */
            self.createAbstractURL = function (baseURL) {
                return baseURL + "#/uuid/" + self.infoAbstract().uuid;
            };

            // check if a track or session can be split
            self.canExpandEvent = function (id) {
                var event = window.dhtmlXScheduler.getEvent(id);
                if (event !== null && event !== undefined) {
                    var splitEvents = event.getSplitEvents();
                    if (splitEvents.length > 0) {
                        return true;
                    }
                }
                return false;
            };

            // split a track or session
            self.expandEvent = function (id) {
                if (self.canExpandEvent(id)) {
                    var event = window.dhtmlXScheduler.getEvent(id);
                    window.dhtmlXScheduler.getEvent(id).getSplitEvents().forEach(function (splitEvent) {
                        window.dhtmlXScheduler.addEvent(splitEvent);
                    });
                    window.dhtmlXScheduler.deleteEvent(id);
                }
            };

            // check if a track or event can be collapsed
            self.canCollapseEvent = function (id) {
                var event = window.dhtmlXScheduler.getEvent(id);
                if (event !== null && event !== undefined) {
                    return event.parentEvent !== null;
                }
                return false;
            };

            // collapse a track or event
            self.collapseEvent = function (id) {
                if (self.canCollapseEvent(id)) {
                    var parentEvent = window.dhtmlXScheduler.getEvent(id).parentEvent;
                    window.dhtmlXScheduler.addEvent(parentEvent);
                    var allEvents = window.dhtmlXScheduler.getEvents();
                    if (allEvents.length > 0) {
                        allEvents.forEach(function (e) {
                            if (e.parentEvent === parentEvent
                                || (e.parentEvent !== null && e.parentEvent.parentEvent === parentEvent)) {
                                window.dhtmlXScheduler.deleteEvent(e.id);
                            }
                        });
                    }
                }
                // TODO: maybe add ID based removal to improve the removal process
            };

            // switch the custom navbar on or off
            self.toggleCustomNavbar = function (toggle) {
                self.customNavbar = toggle;
                if (toggle) {
                    self.displayDate("none");
                    self.displayPrevious("none");
                    self.displayNext("none");
                    self.displayCustomNavbar("inline");
                } else {
                    self.displayCustomNavbar("none");
                    self.displayDate("block");
                }
            };

            /*
             * Get all dates that should be displayed in the custom navbar.
             */
            self.getNavbarDates = function () {
                var dates = [];
                if (self.days().length > 0 && self.schedule.isScheduledDate(window.dhtmlXScheduler.getState().date)) {
                    // find the starting index
                    for (var currentIndex = 0; currentIndex < self.days().length; currentIndex++) {
                       if (self.days()[currentIndex].toDateString() === window.dhtmlXScheduler.getState().date.toDateString()) {
                          break;
                       }
                    }
                    // add the current date and the two previous dates if present
                    var indexToAddPrev = currentIndex;
                    while (indexToAddPrev >= 0 && dates.length < 3) {
                        dates.push(self.days()[indexToAddPrev--]);
                    }
                    // add the next dates if present
                    var indexToAddNext = currentIndex + 1;
                    while (indexToAddNext < self.days().length && dates.length < 5) {
                        dates.push(self.days()[indexToAddNext++]);
                    }
                    // fill up the remaining space with previous dates
                    while (indexToAddPrev >= 0 && dates.length < 5) {
                        dates.push(self.days()[indexToAddPrev--]);
                    }
                    dates.sort(function (a, b) {
                        return a.getTime() - b.getTime();
                    });
                }
                return dates;
            };

            /*
             * Set the currently displayed date of the scheduler.
             * This will only work, if the date is during the schedule and has at least one event.
             */
            self.setSchedulerDate = function (date) {
                if (self.schedule.isScheduledDate(date)) {
                    window.dhtmlXScheduler.setCurrentView(date);
                }
            };

            /*
             * Modify the currently displayed date by the specified
             * number of days.
             */
            self.modifySchedulerDate = function(modificationInDays) {
                self.setSchedulerDate(new Date(window.dhtmlXScheduler.getState().date.getTime()
                    + 24 * 60 * 60 * 1000 * modificationInDays));
            };

            /*
             * Set the current scheduler date to the first date of the conference.
             */
            self.firstSchedulerDate = function() {
                if (self.days().length > 0) {
                    self.setSchedulerDate(self.days()[0]);
                }
            };

            /*
             * Set the current scheduler date to the last date of the conference.
             */
            self.lastSchedulerDate = function() {
                if (self.days().length > 0) {
                    self.setSchedulerDate(self.days()[self.days().length - 1]);
                }
            };

            /*
             * Check whether the specified date is the date currently set in the scheduler.
             */
            self.isCurrentDate = function(date) {
                return date.toDateString() === window.dhtmlXScheduler.getState().date.toDateString();
            };

            /*
             * Open an info view for the selected event, track or session.
             */
            self.displayEventInfo = function (id, childIndex) {
                var currentEvent = window.dhtmlXScheduler.getEvent(id);
                if (currentEvent !== null && currentEvent !== undefined) {
                    if (childIndex !== null && childIndex !== undefined && childIndex >= 0) {
                        // general info
                        self.infoID(null);
                        if (currentEvent.isTrack()) {
                            self.infoBaseEvent(currentEvent.baseEvent.events[childIndex]);
                        } else {
                            // must be a session as events have no children
                            self.infoBaseEvent(currentEvent.baseEvent.tracks[childIndex]);
                        }
                    } else {
                        // general info
                        self.infoID(id);
                        self.infoBaseEvent(currentEvent.baseEvent);
                    }
                    // specific info
                    if (self.infoBaseEvent() instanceof models.Session) {
                        self.infoEventType("Session");
                    } else if (self.infoBaseEvent() instanceof models.Track) {
                        self.infoEventType("Track");
                        var formattedChair = "";
                        if (Array.isArray(self.infoBaseEvent().chair)) {
                            self.infoBaseEvent().chair.forEach(function (person) {
                                formattedChair += person + ", ";
                            });
                            formattedChair = formattedChair.replace(/, +$/, "");
                        } else {
                            formattedChair = self.infoBaseEvent().chair;
                        }
                        self.infoChair(formattedChair);
                    } else {
                        self.infoEventType("Event");
                        var formattedAuthors = "";
                        if (Array.isArray(self.infoBaseEvent().authors)) {
                            self.infoBaseEvent().authors.forEach(function (person) {
                                formattedAuthors += person + ", ";
                            });
                            formattedAuthors = formattedAuthors.replace(/, +$/, "");
                        } else {
                            formattedAuthors = self.infoBaseEvent().authors;
                        }
                        self.infoAuthors(formattedAuthors);
                        self.infoLoadAbstract(self.infoBaseEvent().abstract);
                    }
                    $("#conference-scheduler-info").modal("show");
                }
            };

            self.initScheduler = function () {
                // hide the day display
                window.dhtmlXScheduler.xy.scale_height = -2;
                // hide the scroll bar
                window.dhtmlXScheduler.xy.scroll_width = -1;
                // disable editing events
                window.dhtmlXScheduler.config.readonly = true;
                // prevent short events from overlapping
                window.dhtmlXScheduler.config.separate_short_events = true;
                window.dhtmlXScheduler.config.multi_day = false;
                // window.dhtmlXScheduler.config.mark_now = true; // mark the current time
                /*
                 * Disable dragging events by touching.
                 * This can also be set to "false" to completely disable dragging,
                 * but some touch functionality will break that way.
                 */
                window.dhtmlXScheduler.config.touch_drag = 99999999;
                /*
                 * Size of the x-axis hour steps.
                 * Must be a multiple of 44 for proper alignment (default skin).
                 * This number may vary between different skins.
                 */
                window.dhtmlXScheduler.config.hour_size_px = 264;

                self.toggleCustomNavbar(true);

                /*
                 * Display a custom event box.
                 */
                window.dhtmlXScheduler.renderEvent = function(container, ev) {
                    // define specific templates
                    var templateBoarderClass = "";
                    var templateEventType = "";
                    var templateButtonClass = "";
                    var templateEventContent = "";
                    if (ev.isSession()) {
                        templateBoarderClass = "conference-scheduler-event-s";
                        templateEventType = "Session";
                        templateButtonClass = "conference-scheduler-header-button-session";
                        templateEventContent = "<table>";
                        for (var eventIndex = 0; eventIndex < ev.baseEvent.tracks.length; eventIndex++) {
                            templateEventContent += "<tr data-bind='click: function (data, event) {"
                                + "displayEventInfo(\"" + ev.id + "\", " + eventIndex + ")}'>"
                                + "<td style='border: none; min-width: 12px'></td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='width: 100%'><strong>" + ev.baseEvent.tracks[eventIndex].title + "</strong></td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='text-align: center'>"
                                + moment(ev.baseEvent.tracks[eventIndex].getStart()).format("HH:mm") + "</br>"
                                + moment(ev.baseEvent.tracks[eventIndex].getEnd()).format("HH:mm") + "</td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='border: none; min-width: 12px'></td></tr>";
                        }
                        templateEventContent += "</table>";
                    } else if (ev.isTrack()) {
                        templateBoarderClass = "conference-scheduler-event-t";
                        templateButtonClass = "conference-scheduler-header-button-track";
                        if (ev.parentEvent !== null) {
                            templateBoarderClass += " conference-scheduler-event-st";
                        }
                        templateEventType = "Track";
                        templateEventContent = "<table>";
                        for (var eventIndex = 0; eventIndex < ev.baseEvent.events.length; eventIndex++) {
                            templateEventContent += "<tr data-bind='click: function (data, event) {"
                                + "displayEventInfo(\"" + ev.id + "\", " + eventIndex + ")}'>"
                                + "<td style='border: none; min-width: 12px'></td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='width: 100%'><strong>" + ev.baseEvent.events[eventIndex].title + "</strong></td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='text-align: center'>"
                                + moment(ev.baseEvent.events[eventIndex].getStart()).format("HH:mm") + "</br>"
                                + moment(ev.baseEvent.events[eventIndex].getEnd()).format("HH:mm") + "</td>"
                                + "<td style='min-width: 12px'></td>"
                                + "<td style='border: none; min-width: 12px'></td></tr>";
                        }
                        templateEventContent += "</table>";
                    } else {
                        templateBoarderClass = "conference-scheduler-event-e";
                        templateButtonClass = "conference-scheduler-header-button-event";
                        if (ev.parentEvent !== null) {
                            templateBoarderClass += " conference-scheduler-event-te";
                        }
                        templateEventType = "Event";
                    }
                    var html = "<div class='conference-scheduler-event-border " + templateBoarderClass + "'>";
                    html += "<div class='conference-scheduler-event'>";

                    // the header with date and event type
                    html += "<div class='conference-scheduler-header' data-bind='click: function (data, event) "
                        + "{displayEventInfo(\"" + ev.id + "\")}'><button type='button' "
                        + "class='btn btn-secondary btn-sm pull-left " + templateButtonClass + "' disabled>"
                        + templateEventType + "</button>"
                        + "<h4>" + window.dhtmlXScheduler.templates.event_text(ev.start_date, ev.end_date, ev) + "</h4>"
                        + window.dhtmlXScheduler.templates.event_header(ev.start_date, ev.end_date, ev)
                        + "</div>";

                    // the modification bar for expanding and collapsing
                    html += "<div>";
                    if (self.canCollapseEvent(ev.id)) {
                        html += "<span class='glyphicon glyphicon-minus pull-right conference-scheduler-mod' aria-hidden='true' "
                            + "data-bind='click: function (data, event) {collapseEvent(\"" + ev.id + "\")}'></span>";
                    }
                    if (self.canExpandEvent(ev.id)) {
                        html += "<span class='glyphicon glyphicon-plus pull-right conference-scheduler-mod' aria-hidden='true' "
                            + "data-bind='click: function (data, event) {expandEvent(\"" + ev.id + "\")}'></span>";
                    }
                    html += "</div>";

                    // the body with all the necessary information
                    html += "<div class='conference-scheduler-body'></br>" + templateEventContent + "</div>";

                    // closing divs
                    html += "</div></div>";

                    container.innerHTML = html;
                    ko.applyBindings(self, container);
                    return true;
                };

                // dynamically scale the hour range (y-axis) for different days
                window.dhtmlXScheduler.attachEvent("onViewChange", function (new_mode, new_date) {
                    // disable buttons if first or last day of the conference
                    if (!self.customNavbar) {
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
                    } else {
                        self.displayCustomDates(self.getNavbarDates());
                        // showing the active date is a bit hacky here
                        var actives = [];
                        self.displayCustomDates().forEach(function (d) {
                            if (self.isCurrentDate(d)) {
                                actives.push("active");
                            } else {
                                actives.push("");
                            }
                        });
                        self.displayHighlightDate(actives);
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
                        if (endingDate.getHours() < 24) {
                            window.dhtmlXScheduler.config.last_hour = endingDate.getHours() + 1;
                        } else {
                            window.dhtmlXScheduler.config.last_hour = 24;
                        }
                    } else {
                        window.dhtmlXScheduler.config.first_hour = 0;
                        window.dhtmlXScheduler.config.last_hour = 24;
                    }
                    // adjust the height of the scheduler
                    self.schedulerHeight(window.dhtmlXScheduler.xy.nav_height
                        + (window.dhtmlXScheduler.config.last_hour - window.dhtmlXScheduler.config.first_hour)
                        * window.dhtmlXScheduler.config.hour_size_px);
                    // update scheduler to display all changes
                    window.dhtmlXScheduler.updateView();
                });

                /*
                 * Dynamically initialise the current day if the conference is currently ongoing,
                 * or the first day of the conference if not.
                 */
                var now = new Date();
                if ((self.schedule.getStart() - now) <= 0 && (self.schedule.getEnd() - now) >= 0) {
                    window.dhtmlXScheduler.init("conference_scheduler", now, "day");
                } else {
                    window.dhtmlXScheduler.init("conference_scheduler", self.days()[0], "day");
                }
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
                // add dummy events for empty days
                self.days().forEach(function (day) {
                   if (self.schedule.getDailyEvents(day).length === 0) {
                       var dummyEvent = models.SchedulerEvent.fromObject(
                           new models.Event("No Events", null, "00:00", "23:59", moment(day).format("YYYY[-]MM[-]DD")));
                       dummyEvent.id = contentIndex++ + ":-1:-1";
                       window.dhtmlXScheduler.addEvent(dummyEvent);
                   }
                });
            };
        }

        $(document).ready(function() {
            var data = tools.hiddenData();

            window.moment = moment;
            window.schedule = ScheduleViewModel(data.conferenceUuid);
            window.schedule.init();
        });
    });
});
