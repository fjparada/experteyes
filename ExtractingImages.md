Steps

Record your data and somehow get them on a computer in some movie format.

To import the new data:

From the terminal, run ffmpeg:

Note different video recording devices need different ffmpeg parameters.
For videos recorded using a java program ( .AVI ) or from a DV cam (.dv)
`ffmpeg -i stream2001.avi -deinterlace eye/eye_%5d.tiff`

or

`ffmpeg -i stream2001.dv -deinterlace eye/eye_%5d.tiff`

For videos recorded using a new HDV camera. ( .MOV encoded with H.264 format )

`ffmpeg -i FILE0001.MOV  -pix_fmt h264 eye/eye_%5d.tiff``

(you may need to add the -deinterlace flag to the command above depending on your camera to avoid horizontal lines in your images.)

If you want to save as .jpg instead of .tiff (and the 2nd f is very important in tiff) also add the -sameq flag to ffmpeg

Repeat for the Scene movie.

Note that you must create the scene and eye folders before running these commands. Change the source file to the scene movie.

You can also extract these using quicktime pro by saving as an image sequence.