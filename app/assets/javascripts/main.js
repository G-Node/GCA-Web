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
            "jquery.ui.sortable": ["../lib/jquery-ui/jquery-ui"],
            knockout: ["//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-debug"],
            "ko.sortable": ["knockout-sortable.min"],
            bootstrap: ["../lib/bootstrap/js/bootstrap"],
            sammy: ["../lib/sammy/sammy"],
            moment: ["../lib/momentjs/moment"],
            jsroutes: ["/jsroutes"]
        }
    });

    return requirejs;
})(requirejs);
