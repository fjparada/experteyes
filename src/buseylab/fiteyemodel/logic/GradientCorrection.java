/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package buseylab.fiteyemodel.logic;

import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * This class correct illumination using gradient.  Make sure that the image
 * is in RGB format
 * @author ruj
 */
public class GradientCorrection {

    @Override
    public GradientCorrection clone() {
        GradientCorrection gc = new GradientCorrection();
        gc.start.setLocation(this.start);
        gc.end.setLocation(this.end);
        gc.width = this.width;
        gc.height = this.height;
        gc.lightAdding = this.lightAdding;
        gc.backgroundLevel = this.backgroundLevel;
        gc.updateGradientMask();
        return gc;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end.setLocation(end);
    }

    public BufferedImage getGradientMask() {
        return gradientMask;
    }

    /** Get height of gradient mask */
    public int getHeight() {
        return height;
    }

    /** Set height of gradient mask (not the gradient guiding box) */
    public void setHeight(int height) {
        this.height = Math.max(1, height);
    }

    public int getLightAdding() {
        return lightAdding;
    }

    /** Accept 0-255 level */
    public void setLightAdding(int lightAdding) {
        this.lightAdding = Math.min(255, Math.max(0, lightAdding));
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start.setLocation(start);
    }

    /** Set width of gradient mask (not the gradient guiding box) */
    public int getWidth() {
        return width;
    }

    /** Set width of gradient mask (not the gradient guiding box) */
    public void setWidth(int width) {
        this.width = Math.max(1, width);
    }

    public boolean isOnlyShowGradient() {
        return onlyShowGradient;
    }

    public void setOnlyShowGradient(boolean onlyShowGradient) {
        this.onlyShowGradient = onlyShowGradient;
    }
    // Set parameters here
    int lightAdding = 0;
    int backgroundLevel = 0;

    public int getBackgroundLevel() {
        return backgroundLevel;
    }

    public void setBackgroundLevel(int backgroundLevel) {
        this.backgroundLevel = backgroundLevel;
    }
    // Gradient mask
    BufferedImage gradientMask = null;
    int width = 1, height = 1;
    Point start = new Point(0, 0);
    Point end = new Point(1, 1);
    boolean onlyShowGradient = false;

    /** Make sure you call this once you change a parameter to update your gradient mask or you will be sorry */
    public void updateGradientMask() {
        //this.gradientMask = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        this.gradientMask = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = (Graphics2D) this.gradientMask.getGraphics();

        //GradientPaint gradientPaint = new GradientPaint(this.start, new Color(lightAdding, lightAdding, lightAdding, 255), this.end, new Color(0, 0, 0, 255));
        int lightLevel = Math.min(255, this.lightAdding + this.backgroundLevel);
        GradientPaint gradientPaint = new GradientPaint(this.start, new Color(lightLevel, lightLevel, lightLevel), this.end, new Color(this.backgroundLevel, this.backgroundLevel, this.backgroundLevel));
        g2d.setPaint(gradientPaint);
        g2d.fill(new Rectangle(0, 0, this.width, this.height));
        g2d.dispose();
    }

    /* This method draw Image with corrected gradient on the given graphic
     * Make sure that g2d is from RGB image
     */
    public void correctGradient(Graphics2D g2d) {
        if (this.gradientMask != null) {

            //Graphics2D g2d = (Graphics2D) g;

            if (this.onlyShowGradient) {
                // Fill image with some gray
                int graylevel = 60;
                g2d.setColor(new Color(graylevel, graylevel, graylevel, 255));
                g2d.fill(new Rectangle(0, 0, gradientMask.getWidth(), gradientMask.getHeight()));
            }

            // Save old composite
            Composite oldComposite = g2d.getComposite();

            // Set our additive compisite rule
            g2d.setComposite(new AdditionComposite());

            // Draw our gradient mask
            g2d.drawImage(gradientMask, 0, 0, null);

            // Restore old composite
            g2d.setComposite(oldComposite);
        }
    }
}
