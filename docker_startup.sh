#!/usr/bin/env bash

# Copy any external config files to the frameworks config folder
cp /srv/ext_conf/* /srv/gca/conf/

# Cleanup leftover pid file if a server crash has happened,
# otherwise the service cannot be restarted.
PLAY_RUNNING="$(pgrep -F /srv/gca/target/universal/stage/RUNNING_PID)"
if [ -z $PLAY_RUNNING ]; then
   rm /srv/gca/target/universal/stage/RUNNING_PID
fi

activator start

