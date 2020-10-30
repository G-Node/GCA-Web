# Changelog

Used to document all changes from previous releases and collect changes 
until the next release.

# Release v1.3

## Database changes
This release contains required database changes. After redeployment the database needs to be updated manually using script `patch/patch_v1_2_to_v1_3.sql` and the database service restarted for the changes to take effect.

## Build issues
The current project setup is still using an outdated build tool (`activator`) and is several versions behind the current state of the play framework. The default sbt CDNs for the underlying build dependencies have been updated and no longer provide the required dependencies. Therefore additional CDNs have been added to the project build to keep it in a buildable state. The docker build has been changed to a layered build to decrease build times. See issue #497 and PRs #498 and #509 for details.

## Features
- Stars will be shown in the conference abstract list, indicating favourite abstracts of a user. For details see issue #419.
- The favour/disfavour abstract methods in abstract-list.js now only use javascript.
- Conference banners can now be uploaded via the conference admin pages instead of providing an external link to a banner. The corresponding banners are used when displaying a conference. For details see issue #385.
- Mobile versions of Figures and conference banners are now automatically created when a figure or banner is uploaded.
- Both site and conference admins are now able to upload banners via the conference administration.
- The selenium framework is introduced to integrate frontend tests.
- The way a DOI is represented in the abstract submission form has been updated. For details see issue #478.
- A conference dependent notice can now be added via the conference administration page. The text will be displayed for the respective conference only. This change requires a database change. After deployment of this version, the database needs to be updated manually using script `patch/patch_v1_2_to_v1_3.sql`. For details see issue #470.
- A search field has been added to the abstract list of a conference. For details see PR #505.
- Print view specific stylesheets were added for useful print rendering of abstract pages. See issue #508 for details.

## Fixes
- The string length of notes when changing abstract states will be limited to 255 to avoid database problems. For details see issue #448.
- Added feedback when a user signs up with an already registered email address. For details see issue #464.
- Added failed login notice. For details see issue #465.
- Added feedback on a failed password change attempt. For details see issue #466.
- Added feedback on a failed email change attempt. For details see issue #467.
- Added sign-up notification after new user registration. For details see issue #473.
- The login status is now always properly displayed. For details see issue #468.
- Added error messages on abstract reference validation, if the input is numeric. For details see issue #499.
- The outdated mapbox.js plugin was updated to the latest version. See issue #510 for details.
- An issue was resolved where the sortid of an abstract was removed when an abstract was re-submitted. See issue #507 for details.
- The outdated mathjax.js plugin was updated to version 2.7.7. See issue #506 for details.


# Release v1.2

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


# Release v1.1
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
