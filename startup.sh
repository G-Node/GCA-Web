#!/usr/bin/env bash

cp /srv/tmp/routes /srv/gca/conf/routes

# Use tests to set up a database in a dev environment
activator test stage

activator run

