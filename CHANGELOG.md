# Changelog

Used to document all changes from previous releases and collect changes 
until the next release.

# Latest changes in master

## Features
- Added docker support to the project. GCA-Web can now also be run using docker containers.
  The latest builds can be found at [dockerhub](https://hub.docker.com/r/gnode/gca/builds).
- Added a "Favourite Abstracts" feature to the page. Viewed abstracts can now be
  marked and a list of favoured abstracts can be separately accessed.
- Login behavior has been changed to support page redirecting to the original page after 
  a login has occurred. See issue #361 for details.
- Due to previously added features like "Schedule" or "Location" the navigation bar
  has been restructured to a two row display to accommodate all new features.
  See also issue #446 for details.
- Various smaller changes in behavior have been made to the conference administration page.
  See issues #341, #426, #427 and #437 for details.
- Various smaller changes in behavior have been made to the abstract submission page.
  See issues #201, #288, #338, #339, #397, #437 and #443 for details.

## Fixes
- JavaScript code files have been sanitized. Unsanitized code lead to premature stop of 
  JavaScript execution in more strict browsers like IE and Edge. See #445 for details.
- Various fixes have been applied to the abstract submission. See #438, #439 and #444 for details.
- Various fixes have been applied to the conference administration. See #317, #425, #439 and #444 for details 
- The site now displays pages within the GCA-Web scope on notAuthenticated and notAuthorized 
  access. See #444 for details.
- The password reset page now properly resolves error template messages. See #434 for details.


# Release 1.1
Major changes since the last release:

- Support for conference description
- Support for conference schedule
- Support for conference locations and floorplans
- Support for multiple abstract figures
- Support for offline viewing on mobile devices
- Support for Docker deployment
- iOS and Android support
- Changes to the database schema of the project have been introduced. See #309 and #319 for details.


# Release 1.0
Major changes from the previous version of the GCA-Web include:

- migration to play framework 2.3
- migration from securesocial to silhouette authentication frame work


# Release 0.1
First stable release for the BCCN 2014
