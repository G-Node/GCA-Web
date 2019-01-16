/**
 * Module for offline functionality
 * @module {lib/offline}
 */
define([], function() {
    "use strict";

    /*
     * Request a JSON from a specific url and hand it over to a specific function if
     * successfully retrieved, otherwise call the fail handler.
     * The request will first try to fetch the resource from the server. If this fails the
     * local storage will be searched by the specified fallback key.
     */
    function requestJSON (key, url, successFunction, failHandler) {
        $.getJSON(url, successFunction).fail(function (keyHandover, urlHandover,
                                                       successFunctionHandover,
                                                       failHandlerHandover) {
            return function (jqxhr, textStatus, error) {
                var stored = localStorage.getItem(keyHandover);
                if (stored !== null && stored !== undefined) {
                    successFunctionHandover(JSON.parse(stored));
                } else {
                    failHandlerHandover(jqxhr, textStatus, error);
                }
            };
        }(key, url, successFunction, failHandler));
    }

    return {
        requestJSON: requestJSON
    };
});
