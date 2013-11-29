Abstracts Web Viewer
====================

A abstract web viewer as used for the [Bernstein Conference 2013](https://portal.g-node.org/abstracts/bc13/). It uses JSON encoded abstract data as input and is based on Bootstrap and Knockout.js.

Testing
-------

Just start a local http server in the directory of `abstracts.html` using python:

	python -m SimpleHTTPServer
	
Data Format	
------------

Create js data suitable for the web from "raw" data as follows:

	./create_js.py abstracts.json > data.js
