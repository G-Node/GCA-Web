/**
 * Require JS main configuration
 */
(function (requirejs) {
    "use strict";

    requirejs.config({
        shim: {
            "ko.sortable": ["knockout", "jquery.ui.sortable"]
        },
        paths: {
            requirejs: ["../lib/requirejs/require"],
            jquery: ["../lib/jquery/jquery"],
            "jquery-ui": ["../lib/jquery-ui/jquery-ui.min"],
            "jquery.ui.sortable": ["../lib/jquery-ui/jquery-ui"],
            knockout: ["//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-debug"],
            "ko.sortable": ["knockout-sortable.min"],
            bootstrap: ["../lib/bootstrap/js/bootstrap"],
            sammy: ["../lib/sammy/sammy"],
            moment: ["../lib/momentjs/moment"],
            jsroutes: ["/jsroutes"],
            datetimepicker: ["//cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.1/jquery-ui-timepicker-addon.min"],
            dhtmlxscheduler: ["../lib/scheduler/dhtmlxscheduler"],
            offline: ["../lib/offline"]
        }
    });

    return requirejs;
})(requirejs);
