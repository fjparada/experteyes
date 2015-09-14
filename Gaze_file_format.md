The data file composes of 3 lines.  Data in each line is separated by white space (tab, space).  Data format is as follows:

Line 1:

<file name> <pupil X position> <pupil Y position> <cornea reflection X position> <cornea reflection Y position>

Note: File name MUST not contains any space and must includes extension.  Information here must be correct as they are used for calibration and further output.

Line 2:

<pupil fit top left x> <pupil fit top left y> <pupil fit bottom right x> <pupil fit bottom right y> [optional: <cornea reflect fit top left x> <cornea reflect fit top left y> <cornea reflect fit bottom right x> <cornea reflect fit bottom right y>]

Note: For optional data, you can skip it.  The information here tells the program how to draw the ellipse around detected pupil and cornea reflection.

Line 3:

<fit error> <pupil fit angle> <cornia reflect fit angle>