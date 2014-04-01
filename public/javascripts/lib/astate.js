/**
 * Module for state changes
 * @module {lib/astate}
 */
define(["lib/tools"], function(tools) {

    function StateChangeHelper() {

        if (tools.isGlobalOrUndefined(this)) {
            return new StateChangeHelper();
        }

        var self = this;

        self.transitionMap = {
            owner: {
                isOpen: {
                    InPreparation: ["Submitted"],
                    Submitted:     ["Withdrawn"],
                    Withdrawn:     ["InPreparation"],
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
            if (curState.substr(0, 2) === 'In') {
                return 'In ' +  curState.substr(2);
            }

            return curState;
        };

        self.getActiveTransitionMap = function(isAdmin, isClosed) {
            if(isAdmin) {
                return self.transitionMap.admin;
            } else {
                return self.owner[isClosed ? 'isClosed' : 'isOpen'];
            }
        };

        self.getPossibleStatesFor = function(fromState, isAdmin, isClosed) {
            var map = self.getActiveTransitionMap(isAdmin, isClosed);
            return (fromState in map && map[fromState]) || [];
        };

        self.canTransitionTo = function(fromState, toState, isAdmin, isClosed) {
            var possibleStates = self.getPossibleStatesFor(fromState, isAdmin, isClosed);
            return toState in possibleStates;
        };

    }

    return {
        StateChangeHelper: StateChangeHelper
    };

});


