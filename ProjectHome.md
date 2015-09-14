# ExpertEyes is an open source eyetracking application built in java. #


## News ##

2/14/2014- Regions of Interest analysis MATLAB script beta version available for testing. Email for inquires.

9/24/2013- Latest Wiki update
9/18/2013- Latest Wiki update

5/9/2013- Version 2.4 minor tweaks to the eye model fit minimization parameters to provide better fits to the corneal reflection.

5/08/2013- Latest Wiki update
4/5/2013- Latest Wiki update

6/13/2012- Version 2.3 fixed a bug in the Drift Correction procedure where it was using a drift correction setting that was not necessarily the one that is closest in time. All users should upgrade.

3/7/2012: For Windows 7 users experiencing troubles with not being able to find javaw.exe here is what you do. First, reinstall java from java.com.  If that doesn't work: °°°°°
Open C:/Windows/SysWOW64  °°°°°
Scroll down and find javaw.exe and copy it °°°°°
Open C:/Windows/System32 °°°°°
Paste javaw.exe °°°°°
Run the .bat file in the ExpertEyes folder.

11/10/2011- Version 2.2 Added drift correction. Sometimes the glasses slip, and if you know during your experiment where a subject is looking you can assign a drift correction set to that interval and all subsequent frames will be offset to correct for the drift. If using the drift correction feature, do not use the linear interpolation or drift correction sets will be ignored.

12/7/2010- **Version 2.1 fixed a misalignment bug in the movie export module. All users should upgrade to this version**.

6/16/2010- Version 2.0.2 is stable and fixed a small bug if there was no large cleaned scene folder specified.

6/9/2010- Version 2.0.1 is stable and produces research-grade data with accuracy comparable to commercial systems. Version 2.0.1 fixes a small bug in version 2.0 if the corneal refection was set to a circle in the eye model fit. Please update your code accordingly.

02/01/2010- Latest Wiki update

01/21/2010 Fit Eye model and Eye Tracking Calibration packages are now combined in one package called ExpertEyes.

12/21/2009- Latest Wiki update
09/18/2009- Latest Wiki update
08/18/2009- Latest Wiki update
06/10/2009- Latest Wiki update

## Info ##

Two video streams are recorded from this open source hardware design:

Babcock, J.S., and Pelz, J. (2004). Building a lightweight eyetracking headgear, ETRA 2004: Eye Tracking Research and Applications Symposium, 109-113.

http://www.jasonbabcock.com/research/ETRA04_babcock_pelz_color_small.pdf

and the video files are split into image sequences for temporal alignment and calibration.

The schematics, parts list, and instructional movie to build a model similar to the one described by Babcock and Pelz is found here:

http://129.79.193.155/~busey/EyeTracker/

For a more updated model (HD) visit the wiki **https://code.google.com/p/experteyes/w/list**.

This project uses the following software/libraries:

  * [Jdom](http://www.jdom.org/)
  * [JTransforms](http://piotr.wendykier.googlepages.com/jtransforms)
  * [JFreeChart](http://www.jfree.org/jfreechart/)
  * [Berkeley DB](http://www.oracle.com/technology/products/berkeley-db/index.html)
  * [Mantissa](http://www.spaceroots.org/software/mantissa/index.html)
  * [Commons-Math](http://commons.apache.org/math/)
  * [Voronoi Diagram](http://www.cs.cornell.edu/home/chew/Delaunay05.html)
  * [ImageJ](http://rsbweb.nih.gov/ij/index.html)
  * [JAI](http://java.sun.com/javase/technologies/desktop/media/jai/)
  * [FFMPEG](http://ffmpeg.org/)

# Subscribe to our moderated Mailing List:  http://www.freelists.org/list/experteyes #


Contact: busey {at} indiana.edu