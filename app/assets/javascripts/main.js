/**
 * Require JS main configuration
 */
(function (requirejs) {
    "use strict";

    requirejs.config({
        shim: {
            jqueryui: ["jquery"],
            knockout: ["jquery"],
            bootstrap: ["jquery"]
        },
        paths: {
            requirejs: ["../lib/requirejs/require"],
            jquery: ["../lib/jquery/jquery"],
            jqueryui: ["../lib/jquery-ui/jquery-ui"],
            knockout: ["../lib/knockout/knockout"],
            bootstrap: ["../lib/bootstrap/js/bootstrap"],
            sammy: ["../lib/sammy/sammy"],
            moment: ["../lib/momentjs/moment"],
            jsroutes: ["/jsroutes"]
        }
    });

    return requirejs;
})(requirejs);
