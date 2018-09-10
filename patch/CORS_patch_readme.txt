--------------------------------------------------
  CORS Patch Readme
  by Robin Patrick Schenk
  05.09.2018
--------------------------------------------------


-----------------------------------
  Short Description
-----------------------------------
CORS patch to allow remote testing, e.g. on mobile devices.

-----------------------------------
  Further Information
-----------------------------------
The patch "Implemented_CORS_Filter.patch" will apply CORS filters to the project allowing requests from all origins.
This patch thereby introduces a major security risk and is just usefull for remote testing.
It should never be used in production code.

Furthermore, the "baseurl" in the "application.conf" has to be changed to the IP address of the client device
in order for remote requests to be successfull.
Beware, many data fields, including URLs used for requests, are not updated if the containing entity 
is not explicitly changed. This may result into URLs pointing to previously set base URLs, which will break 
functionality. The tested entities, e.g. conferences, need to be updated beforehand.

