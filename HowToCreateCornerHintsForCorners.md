Creating hints for the EyeTrackerCalibrator program to find the corners

# Introduction #

The ExpertEyes eye tracker calibrator will use Gabor jets to find the corners on all frames once you've located the corners on one frame. However, it is too costly to search the entire image, so we provide corner hints that reduce the search space. These hints are added manually, using the program called Corner\_Selector.jar.

To run this program, download the Corner\_Selector.jar file. Run it by double-clicking the .jar file.

Once it runs, browse to your calibration folder for your subject (not the project folder, despite the name). This will place a folder called CornerHints in the calibration folder, which the EyeTrackerCalibrator creates. If you don't have this yet, you can just select the project folder and then drag the cornerhints folder into the calibration folder later on when you want to find the corners.

Also choose the small cleaned scene folder. The ratio of the small and large cleaned scene files should be approximately 2.81. The small versions are used for speed, and the large are used for accuracy. You can also correct for barrel distortion when you create these from your raw video footage if you like.

Once you have the paths correct, click the setup button. You can then go through the individual frames, centering the yellow square on the correct corner. Go through 4 different times for the 4 corners of your monitor.

The space bar turns the review function on and off, and the arrow keys advance and go backward. The location is set for a frame when the program LEAVES a frame.

After you have done all four corners, click done. This will save the hints to a folder called CornerHints located in your calibration folder.

Now start the ExpertEyes EyeTrackerCalibrator and go to Clean Data. You will now tell the program the range of frames you want to find corners for. Toggle the 4 corner buttons on the far right side of the screen. This tell the program that you are detecting corners. Move to the frame that has the first frame you would like to detect corners for. Click Start Marking, and move to the last frame you want to detect corners for. Click Stop Marking. Now highlight the entry in the list that you just created and select Detect Corners. The program will now allow you to identify the locations of the four corners in your scene to create a template for each.

Mark each corner in turn, making sure you select the correct radio button in the lower-left first.

When you click the Done button the program will ask you how many CPU cores you want to use (you do have a multi-cpu computer, don't you?) and then it will find the corners automatically, graphically displaying the results so you can see how it is doing.

The results get saved into the Corners folder in your project folder.