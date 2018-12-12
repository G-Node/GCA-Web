#!/usr/bin/env bash

# copy any external config files to the frameworks config folder
cp /srv/ext_conf/* /srv/gca/conf/

activator start

