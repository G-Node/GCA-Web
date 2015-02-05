G-Node custom bootstrap style
=============================

This git contains all custom less files required to compile a custom bootstrap css. It furthermore contains additional less file to compile a stylesheet specific for media type screen. The stylesheets can be either locally compiled using e.g. lessc or can be easily cloned into a play framework project as described in the following paragraphs.

# Compile custom bootstrap.css file using lessc
- Get nodejs and npm
	sudo apt-get install nodejs nodejs-dev npm node-less
- download bootstrap source code distribution of choice and extract to directory of choice
- clone the gnstyle project to a directory of choice
	git clone git@github.com:G-Node/gnstyle.git
- modify the bootstrap path variable in "_G-Node-bootstrap.less":
	@bootPath ... path to where the original bootstrap less files are located
- compile the custom boostrap.css file using lessc, e.g.:
	lessc _G-Node-bootstrap.less > gnode-bootstrap.css


# Using custom bootstrap git in a play framework:
- move to app/assets/stylesheets folder
- clone the gnstyle project, follow the cloning procedure exactly
- use a project name without special characters, especially "-" or ".". e.g.:
	git clone git@github.com:G-Node/G-Node-Bootstrap.git gnstyle
	git add gnstyle/
- only then the inner git will be set up properly to play nice with the outer git
- now you can properly commit changes from within both git repositories

## Build bootstrap from custom less in play framework
- change the content of the path variable in "G-Node-bootstrap.play.less" to where the original bootstrap less files can be found.
- change the references to the correct path/filenames where the compiled css stylesheets are actually used

