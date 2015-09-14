Here is the explanation of each column in the output file.  All numbers are 0 or positive real number (except for trial number)  -666 represents an error or unavailable information.

| **Column name** | **Description** |
|:----------------|:----------------|
| Screen Frame    | This is the screen frame number.|
| Pupil x         | This is the x coordinate of the pupil in eye scene picture. |
| Pupil y         | This is the y coordinate of the pupil in eye scene picture. |
| Pupil fit top left x | Top left x coordinate of the box showing ellipse fit of the pupil|
| Pupil fit top left y | Top left y coordinate of the box showing ellipse fit of the pupil |
| Pupil fit bottom right x | Bottom right x coordinate of the box showing ellipse fit of the pupil |
| Pupil fit bottom right y | Bottom right y coordinate of the box showing ellipse fit of the pupil|
| Cornia reflect x| Similar to pupil|
| Cornia reflect  y| Similar to pupil|
| Raw primary x   | x coordinate of the eye gaze on to the small scene coordinate using the primary calibration set|
| Raw primary y   | y coordinate of the eye gaze on to the small scene coordinate using the primary calibration set|
| Raw secondary x | Similar to raw primary but uses the secondary calibration set.|
| Raw secondary y | Same as above   |
| Raw linear interpolated x| Similar to raw primary but using linear interpolation of the results from primary and secondary calibration set (primary + (primary - secondary)frame# / totalframe) |
| Raw linear interpolated y| Same as above but for y|
| Screen primary x| Projection on to the screen coordinate from the primary calibration set eye gaze.|
| Screen primary y|                 |
| Screen secondary x| Same as above but from the secondary calibration set |
| Screen secondary y|                 |
| Screen linear interpolated x| Similar explanation.|
| Screen linear interpolated y|                 |
| Similarity of topleft |Similarity score of the detected top left corner of the screen in the scene image (you probably don't need this..)|
| Similarity of topright|                 |
| Similarity of bottomleft|                 |
| Similarity of bottomright|                 |
| Error           | Error code from what are marked in "clean data" panel. The code is an integer whose bit represents the following error types `[bottomright corner detection, bottomleft corner detection, topright corner detection, topleft corner detection, unrecoverable, eye]`|
| Trial file name | Name of the trial as marked in the "Mark Trials" panel.  If the name is C\_x\_y, it means this is a part of calibration sequence stored in (x,y) slot in the calibration panel.|
| Trial number    | Trial number as marked in the "Mark Trials" panel. 0 means it's not a trial nor calibration.  negative number means it's part of a calibration sequence.|