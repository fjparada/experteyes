/*
 * Copyright (c) 2009 by Thomas Busey and Ruj Akavipat
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Experteyes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Thomas Busey and Ruj Akavipat ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Thomas Busey and Ruj Akavipat BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package logic;

import gauss.GaussKernel1d;
import ij.plugin.filter.Convolver;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.Vector;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.ImageIcon;

public class ImageUtils {

    /* scale image to preferred dimension
     * first scale width to preferred width
     * then scale scaledHeight further to preferred height
     */
    public static Image scaleImage(Image I, int preferredWidth, int preferredHeight) {
        Image scaledI = I;
        int width = I.getWidth(null);
        int height = I.getHeight(null);
        double scaledWidth, scaledHeight;
        double scaleFactor;

        // scale width
        scaleFactor = (double) preferredWidth / (double) width;
        scaledWidth = width * scaleFactor;
        scaledHeight = height * scaleFactor;
        // scale height from width scaling
        scaleFactor = (double) preferredHeight / (double) scaledHeight;
        scaledWidth = scaledWidth * scaleFactor;
        scaledHeight = scaledHeight * scaleFactor;

        System.out.println("Scaling image to " + (int) scaledWidth + "x" + (int) scaledHeight);
        scaledI = I.getScaledInstance((int) scaledWidth, (int) scaledHeight, Image.SCALE_DEFAULT);

        return scaledI;
    }
    // stitch images

    public static Image stitchImages(Image left, Image right) {
        int leftWidth = left.getWidth(null);
        int leftHeight = left.getHeight(null);
        int rightWidth = right.getWidth(null);
        int rightHeight = right.getHeight(null);
        int taller;
        BufferedImage stitched;
        Graphics stitchedGraphics;

        // find taller image 
        if (leftHeight > rightHeight) {
            taller = leftHeight;
        } else {
            taller = rightHeight;
        }

        stitched = new BufferedImage(leftWidth + rightWidth, taller, BufferedImage.TYPE_INT_RGB);
        stitchedGraphics = stitched.getGraphics();
        // left image
        stitchedGraphics.drawImage(left, 0, 0, null);
        // right image
        stitchedGraphics.drawImage(right, leftWidth, 0, null);

        return stitched;
    }

    // stitches images with padding between
    public static Image stitchImages(Image left, Image right, int padWidth, Color padColor) {
        int leftWidth = left.getWidth(null);
        int leftHeight = left.getHeight(null);
        int rightWidth = right.getWidth(null);
        int rightHeight = right.getHeight(null);
        int taller;
        BufferedImage stitched;
        Graphics stitchedGraphics;

        // find taller image 
        if (leftHeight > rightHeight) {
            taller = leftHeight;
        } else {
            taller = rightHeight;
        }

        stitched = new BufferedImage(leftWidth + padWidth + rightWidth, taller, BufferedImage.TYPE_INT_RGB);
        stitchedGraphics = stitched.getGraphics();
        // left image
        stitchedGraphics.drawImage(left, 0, 0, null);
        // padding
        stitchedGraphics.setColor(padColor);
        stitchedGraphics.fillRect(leftWidth, 0, leftWidth + padWidth, taller);
        // right image
        stitchedGraphics.drawImage(right, leftWidth + padWidth, 0, null);

        return stitched;
    }

    // get pixels from an image
    public static int[] getPixels(Image I) {
        int width = I.getWidth(null);
        int height = I.getHeight(null);
        int pixels[] = new int[width * height];
        PixelGrabber pg = new PixelGrabber(I, 0, 0, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {
            System.out.println("Interrupted");
        }

        return pixels;
    }

    // get pixels from an image
    public static int[] getPixels(Image I, int x, int y, int width, int height) {
        /** Cap the width and height to the image size */

        int pixels[] = new int[width * height];
        PixelGrabber pg = new PixelGrabber(I, x, y, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {
            System.out.println("Interrupted");
        }
        return pixels;
    }

    public static double[] RGBtoGrayDouble(int[] RGBpixels) {
        double[] grayDoublePixels = new double[RGBpixels.length];

        for (int i = 0; i < RGBpixels.length; i++) {
            // we are working with grayscale images, so just mask off a channel.
            // blue is the fastest since there is no shifting involved
            int blue = RGBpixels[i] & 0xff;
            grayDoublePixels[i] = (double) blue;
        }

        return grayDoublePixels;
    }

    public static int[] RGBtoGray(int[] RGBpixels) {
        int[] grayPixels = new int[RGBpixels.length];

        for (int i = 0; i < RGBpixels.length; i++) {
            // we are working with grayscale images, so just mask off a channel.
            // blue is the fastest since there is no shifting involved
            int blue = RGBpixels[i] & 0xff;
            grayPixels[i] = blue;
        }

        return grayPixels;
    }

    public static int[] grayDoubleToRGB(double[] grayDoublePixels) {
        double pixel;
        int numClipneg = 0;
        int numClip = 0;
        int[] RGBpixels = new int[grayDoublePixels.length];
        int red, green, blue;
        int alpha = 255;

        for (int i = 0; i < grayDoublePixels.length; i++) {
            pixel = grayDoublePixels[i];
            // clip
            if (pixel < 0) {
                pixel = 0;
                numClipneg++;
            }
            if (pixel > 255) {
                pixel = 255;
                numClip++;
            }
            red = (int) pixel;
            green = (int) pixel;
            blue = (int) pixel;
            // make rgb
            RGBpixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return RGBpixels;
    }

    public static int[] grayToRGB(int[] grayPixels) {
        double pixel;
        int numClipneg = 0;
        int numClip = 0;
        int[] RGBpixels = new int[grayPixels.length];
        int red, green, blue;
        int alpha = 255;

        for (int i = 0; i < grayPixels.length; i++) {
            pixel = grayPixels[i];
            // clip
            if (pixel < 0) {
                pixel = 0;
                numClipneg++;
            }
            if (pixel > 255) {
                pixel = 255;
                numClip++;
            }
            red = (int) pixel;
            green = (int) pixel;
            blue = (int) pixel;
            // make rgb
            RGBpixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return RGBpixels;
    }
    // create a new java image with pixels

    public static Image makeImageFromPixels(int[] pixels, int width, int height) {
        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
    }

    public static BufferedImage plotXtoImage(BufferedImage I, int x, int y, Color c) {
        int alpha = 255;
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        int pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;

        if (x > 0 && y > 0 && x < I.getWidth() && y < I.getHeight()) {
            I.setRGB(x - 1, y, pixel);
            I.setRGB(x, y, pixel);
            I.setRGB(x + 1, y, pixel);
            I.setRGB(x, y - 1, pixel);
            I.setRGB(x, y + 1, pixel);
        }

        return I;
    }

    /*
     * this method takes a binary pixel array and inverts it
     * 0 -> 255
     * 255 -> 0
     */
    public static int[] invertPixels(int[] pixels) {
        int[] inverted = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            inverted[i] = 255 - pixels[i];
        }

        return inverted;
    }

    public static int[] threshold(int[] pixels, int t) {
        int[] threshPixels = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] < t) {
                threshPixels[i] = 0;
            } else {
                threshPixels[i] = 255;
            }
        }
        return threshPixels;
    }

    public static int[] threshold(int[] pixels, int from, int to, int color) {
        int[] threshPixels = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] <= to && pixels[i] >= from) {
                threshPixels[i] = color;
            } else {
                threshPixels[i] = (255 << 32);
            }
        }
        return threshPixels;
    }

    /***********************************************************************
     * all functions below here work on grayscale double pixels, not RGB int
     ***********************************************************************/    // rotate pixels by angle
    public static double[] rotate(double[] pixels, int width, int height, double angle) {
        double newPixels[] = null;

        if ((width * height) == pixels.length) {
            angle = angle / 180 * Math.PI;	//program needs angle in radians	

            double midWidth = (double) width / 2.0;
            double midHeight = (double) height / 2.0;

            double CosAngle = Math.cos(angle);
            double SinAngle = Math.sin(angle);
            double mSinAngle = -Math.sin(angle);
            double transX, transY;
            int newX, newY;
            double newXD, newYD;
            double a, b;
            double newPixel;

            newPixels = new double[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    transX = x - midWidth;
                    transY = y - midHeight;
                    newXD = (transX * CosAngle + transY * SinAngle) + midWidth;
                    newYD = (transX * mSinAngle + transY * CosAngle) + midHeight;
                    newX = (int) Math.floor(newXD);
                    newY = (int) Math.floor(newYD);

                    if ((newX < width - 1) & (newX >= 0) & (newY < height - 1) & newY >= 0) {
                        a = newXD - newX;
                        b = newYD - newY;
                        newPixel = (1 - a) * (1 - b) * pixels[newY * width + newX] +
                                a * (1 - b) * pixels[newY * width + (newX + 1)] +
                                (1 - a) * b * pixels[(newY + 1) * width + newX] +
                                a * b * pixels[(newY + 1) * width + (newX + 1)];
                        newPixels[y * width + x] = newPixel;
                    } else {
                        newPixels[y * width + x] = pixels[0];//probaly white; fix

                    }
                }
            }
        }
        return newPixels;
    }

    public static double[] adjustContrast(double pixels[], int width, int height, double contrastChange, double brightnessChange) {
        double newPixels[] = null;

        int x, y;
        double pixel;

        if ((width * height) == pixels.length) {
            newPixels = new double[width * height];
            int newIndex = 0;
            for (y = 0; y < height; y++) {
                for (x = 0; x < width; x++) {
                    pixel = pixels[y * width + x];
                    pixel = ((pixel - 128.0) * contrastChange) + (128.0 + brightnessChange);
                    newPixels[newIndex++] = pixel;
                }
            }
        }
        return newPixels;
    }
    // combine pixels in pix1 with those in pix2 using weights in pix1weight and pix2weight respectively

    public static double[] combinePixels(double[] pix1, double pix1weight, double[] pix2, double pix2weight, int width, int height) {
        double newPixels[] = null;
        int newIndex;
        double p1, p2;
        int x, y;

        if ((width * height) == pix1.length && (width * height) == pix2.length) {
            newIndex = 0;
            newPixels = new double[width * height];
            for (y = 0; y < height; y++) {
                for (x = 0; x < width; x++) {
                    p1 = pix1[y * width + x];
                    p2 = pix2[y * width + x];
                    newPixels[newIndex++] = (p1 * pix1weight) + (p2 * pix2weight);
                }
            }
        }

        return newPixels;
    }
    // linearize pixels will linearize and clip to [0, 255]

    public static double[] linearizePixels(double pixels[], int width, int height, Vector abVector) {
        double newPixels[] = null;

        int x, y;
        double pixel, a, b, newLinear, linearVoltage;

        a = Double.parseDouble((String) abVector.elementAt(0));
        b = Double.parseDouble((String) abVector.elementAt(1));

        // double maxLinear = Math.pow(((1.0/a)*255.0),(1/b));
        double maxLinear = Math.pow((1.0 / a) * 255.0, (1.0 / b));

//		System.out.println(" a = " + a + " b = " + b + " maxLinear = " + maxLinear);

        if ((width * height) == pixels.length) {
            newPixels = new double[width * height];
            int newIndex = 0;
            for (y = 0; y < height; y++) {
                for (x = 0; x < width; x++) {
                    pixel = pixels[y * width + x];
                    newLinear = (pixel / 255.0) * (maxLinear - 1.0) + 1.0;
                    linearVoltage = a * Math.pow(newLinear, b);
                    //System.out.println("Old Linear " + pixel + " New Linear " + newLinear + " Linear Voltage " + linearVoltage);
                    pixel = linearVoltage;
                    newPixels[newIndex++] = pixel;
                }
            }
        }

        return newPixels;
    }

    public static int createARGB(int alpha, int r, int g, int b) {
        return (alpha << 24) + (r << 16) + (g << 8) + b;
    }

    /** 
     * This method returns true if the specified image has transparent pixels 
     * From http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
     */
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

    /**
     * This method returns a buffered image with the contents of an image
     * From http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    public static void unsharpMask(ImageProcessor ip, double sigma, double a) {
        ImageProcessor I = ip.convertToFloat();

        //create a blurred version of the original
        ImageProcessor J = I.duplicate();
        float[] H = GaussKernel1d.create(sigma);
        Convolver cv = new Convolver();
        cv.setNormalize(true);
        cv.convolve(J, H, 1, H.length);
        cv.convolve(J, H, H.length, 1);

        //multiply the original image by (1+a)
        I.multiply(1 + a);
        //multiply the mask image by a
        J.multiply(a);
        //subtract the weighted mask from the original
        I.copyBits(J, 0, 0, Blitter.SUBTRACT);

        //copy result back into original byte image
        ip.insert(I.convertToByte(false), 0, 0);
    }

    public static BufferedImage loadRGBImage(File inputFile) {
        RenderedOp op = null;
        try {
            op = JAI.create("fileload", inputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage buffer = null;
        try {
            buffer = op.getAsBufferedImage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        BufferedImage image = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(buffer, 0, 0, null);

        return image;
    }

    /** 
     * Return a part of the regtangle that is within in image. The result is
     * unknown when the rectangle is completely outside of the image
     */
    public static Rectangle clipRectangle(Image image, Rectangle rec){
        Rectangle imRec = new Rectangle(0,0,image.getWidth(null), image.getHeight(null));
        return imRec.intersection(rec);
    }
}
