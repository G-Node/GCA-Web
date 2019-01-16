/**
 * Module for state related things
 * @module {lib/astate}
 */
define(["lib/tools", "moment"], function(tools, moment) {
    "use strict";
    function StateChangeHelper() {
        if (!(this instanceof StateChangeHelper)) {
            return new StateChangeHelper();
        }

        var self = this;

        self.transitionMap = {
            owner: {
                isOpen: {
                    InPreparation: ["Submitted"],
                    Submitted:     ["InPreparation", "Withdrawn"],
                    InRevision:    ["Submitted"]
                },
                isClosed: {
                    InRevision:    ["Submitted"]
                }},
            admin: {
                Submitted:  ["InReview"],
                InReview:   ["Accepted", "Rejected", "InRevision", "Withdrawn"],
                InRevision: ["InReview"],
                Accepted:   ["InRevision", "Withdrawn"],
                Rejected:   ["InRevision", "Withdrawn"]
            }};

        self.mkStateDisplayName = function(curState) {
            if (curState.substr(0, 2) === "In") {
                return "In " +  curState.substr(2);
            }

            return curState;
        };

        self.getActiveTransitionMap = function(isAdmin, isClosed) {
            if (isAdmin) {
                return self.transitionMap.admin;
            } else {
                return self.transitionMap.owner[isClosed ? "isClosed" : "isOpen"];
            }
        };

        self.getPossibleStatesFor = function(fromState, isAdmin, isClosed) {
            var map = self.getActiveTransitionMap(isAdmin, isClosed);
            return (fromState in map && map[fromState]) || [];
        };

        self.canTransitionTo = function(fromState, toState, isAdmin, isClosed) {
            var possibleStates = self.getPossibleStatesFor(fromState, isAdmin, isClosed);
            return possibleStates.indexOf(toState) > -1;
        };
    }

    function StateLogHelper() {
        if (tools.isGlobalOrUndefined(this)) {
            return new StateLogHelper();
        }

        var self = this;

        self.formatDate = function(stateLog) {
            stateLog.forEach(function (elm) {
                elm.formattedDate = moment(elm.timestamp).calendar();
            });
        };
    }

    return {
        changeHelper: new StateChangeHelper(),
        logHelper: new StateLogHelper()
    };
});
