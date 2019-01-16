/**
 * Module with knockout like getters and setters.
 *
 * @module {lib/accessors}
 */
define(function() {
    function newAcessor(initVal) {
        var val = initVal;
        function accessor(newVal) {
            var result = val;

            if (newVal !== undefined) {
                val = newVal;
                result = this;
            }

            return result;
        }

        return accessor;
    }

    function newArrayAccessor(initVal) {
        var val = initVal;
        function arrayAccessor(newVal) {
            var result = val;

            if (newVal !== undefined) {
                val = newVal;
                result = this;
            }

            return result;
        }

        arrayAccessor.push = function(addVal) {
            val.push(addVal);
            return val;
        };

        arrayAccessor.length = 0;

        return arrayAccessor;
    }

    return {
        accessor: newAcessor,
        arrayAccessor: newArrayAccessor
    };
});
