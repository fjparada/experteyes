## 1.15 ##
  * Make change so that only bad trial data will skip computation and output -666 for screen coordinate.
  * Add bad trial to error type.  See [error\_code](error_code.md) for information.

## 1.14 ##
  * Fix a bug that mark good trial output as having error and show bad trial as normal

## 1.13 ##
  * Fix unexpected crashes when scene file names are skipped or not available during corner detection
  * Corner can be marked as bad during detection by moving a hint box totally outside the scene image.
## 1.11 ##
  * Change "Screen" labels to "Scene"
  * Fix bug that corrupts export data when trial info is not entered in the correct order by the frame number
  * Add capability to mark a trial as a bad trial then the export data will have -666 in fixations for the trial.
## 1.10 ##
- Fix display keeps enlarging
## 1.9 ##
- Fix crash when an image file is corrupt (The file can be open but has error in data)
## 1.8 ##
-Fix export movie crashes when there is not enough eye gaze to do median.
-Add error angle to export of calibration point info.
## 1.7 ##
Fix incorrect scaling for Movie export when small scene image is not 512x512.
## 1.6 ##
Fix incorrect scaling when small scene image is not 512x512.
## 1.5.1 Windows, 1.5 OS X ##
Correct alignment of image during calibration point detection progress display.
## 1.5 Windows ##
Fix bad built of jar file
## 1.4 ##
Fix exporting movie does not scale screen image.
Add better error report when exporting movies.