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
package buseylab.fiteyemodel.gui;

import buseylab.fiteyemodel.gui.GradientPanel.Corner;
import buseylab.fiteyemodel.gui.GradientPanel.GradientPanelListener;
import edu.cornell.chew.delaunay.DelaunayTriangulation;
import edu.cornell.chew.delaunay.Pnt;
import edu.cornell.chew.delaunay.Simplex;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

/*
 * FitEyeModelSetup.java
 *
 * Created on March 17, 2008, 10:08 AM
 */
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import buseylab.fiteyemodel.logic.FitEyeModel;
import buseylab.fiteyemodel.logic.FittingListener;
import buseylab.fiteyemodel.logic.GradientCorrection;
import buseylab.fiteyemodel.logic.ImageUtils;
import buseylab.fiteyemodel.logic.NotHiddenPictureFilter;
import buseylab.fiteyemodel.logic.PointToFrameByEstimatedPupilLocation;
import buseylab.fiteyemodel.logic.RotatedEllipse2D;
import buseylab.fiteyemodel.logic.ThreadedImageProcessor;
import buseylab.fiteyemodel.logic.ThreadedImageProcessorListener;
import buseylab.fiteyemodel.util.FitEyeModelRunner;
import buseylab.fiteyemodel.util.ParameterList;
import buseylab.fiteyemodel.util.ParameterList.Entry;
import buseylab.fiteyemodel.util.Parameters;
import buseylab.fiteyemodel.util.TerminationListener;
import java.awt.Point;

/**
 * @todo add tool tip to the slide bar
 * @author  dwyatte, ruj
 */
public class FitEyeModelSetup extends javax.swing.JFrame implements FitEyeModelSystemInterface {

    public static int ESTIMATE_PUPIL_SAMPLING_RATE = 10;
    boolean notifyDiscardConfiguration = true;
    // path to the eye directory
    File eyeDir;
    // array containing all eye files
    File[] eyeFiles;
    int frameNum = 0;    // we need this to get our processed images   
    ThreadedImageProcessor imgProc;
    // For storing fit eye model system so that we can have auto update
    private FitEyeModelRunner fitEyeModelRunner;    // For managing the list panel
    DefaultListModel configListModel = new DefaultListModel();
    PointToFrameByEstimatedPupilLocation pointToFrameByEstimatedPupilLocation;
    BufferedImage voronoiBufferedImage = null;
    /** For computing voronoi */
    DelaunayTriangulation delaunayTriangulation = null;
    boolean isEyeFittingRunning = false;
    private boolean isSelectingColor = false;
    private Color searchBoxColor = Color.GREEN;
    private Color gradientBoxColor = Color.red;
    private boolean gradientChangePanelActivate = false;
    private boolean gradientChangeMode = false;
    private Rectangle gradientBox = new Rectangle(100, 150, 1, 1);
    private Rectangle savedSearchBox = null;
    private GradientCorrection gradientCorrection = new GradientCorrection();
    int lastFrameNum = -1;
    BufferedImage bufferedOriginalImage = null;

    /** Creates new form FitEyeModelSetup */
    public FitEyeModelSetup() {

        initComponents();

        // Clear out buffered information
        this.lastFrameNum = -1;
        this.bufferedOriginalImage = null;

        // Create transparent empty buffer for voronoi
        this.voronoiBufferedImage = new BufferedImage(
                this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        clearVoronoiInfo();
        drawAllVoronoi();

        // set up parent handles to subcomponents. they will need this to use
        // getPaintPanel/getImageProcessor to paint or setFrame
        searchSpacePanel1.setParent(this);
        thresholdPanel1.setParent(this);

        this.pointToFrameByEstimatedPupilLocation =
                new PointToFrameByEstimatedPupilLocation();

        this.colorSelectionPanel1.addDropperActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (colorSelectionPanel1.isDropperSelected()) {
                    interactivePanel.setColorCaptureListener(colorSelectionPanel1);
                    // Switch to hand icon
                    interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    // Remove listener
                    interactivePanel.setColorCaptureListener(null);
                    // Switch to cross hair
                    interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });

        this.colorSelectionPanel1.addAutoTestModelCheckBoxActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleAutoTestModelCheckBoxAction();
            }
        });

        this.colorSelectionPanel1.addSliderChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                if (ColorSelectionPanel.SIGMA_SLIDER_NAME.equals(slider.getName())
                        || ColorSelectionPanel.SHARPENINGFACTOR_SLIDER_NAME.equals(slider.getName())) {
                    // SEt frame to trigger change
                    setFrame(frameNum);
                }
                triggerAutoFitEyeModelRecompute();
                setHighlight();
                interactivePanel.repaint();
            }
        });

        ChangeListener checkBoxChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setFrame(frameNum);
                triggerAutoFitEyeModelRecompute();
                setHighlight();
                interactivePanel.repaint();
            }
        };

        this.colorSelectionPanel1.addDetectPupilAngleChangeListener(checkBoxChangeListener);

        this.colorSelectionPanel1.addCRIsCircleChangeListener(checkBoxChangeListener);

        this.colorSelectionPanel1.addHighlightCheckBoxActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setHighlight();
                interactivePanel.repaint();
            }
        });

        this.thresholdPanel1.addPupilThreshSliderStateChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updateEstimateConfigutationPupilLocation();
            }
        });

        final Component parent = this;
        this.thresholdPanel1.addEstimatePupilLocationButtonActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int decision = JOptionPane.showConfirmDialog(parent,
                        "<html><p>Plase make sure that your current search space covers all pupil locations, and\n"
                        + "pupil threshold is set properly.  Otherwise, estimations may not be correct.",
                        "Before you start", JOptionPane.OK_CANCEL_OPTION);

                if (decision == JOptionPane.OK_OPTION) {
                    estimatePupilLocations();
                }
            }
        });

        this.thresholdPanel1.addLoadPupilLocationButtonActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadPupilLocations();
            }
        });

        this.interactivePanel.setSearchAreaChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                handleSearchAreaMove();
            }
        });


        // Set gradient box to match init value of the slider bar
        this.gradientBox.setBounds(this.interactivePanel.getWidth() / 2 - this.gradientPanel1.getGradientBoxWidth() / 2,
                this.interactivePanel.getHeight() / 2 - this.gradientPanel1.getGradientBoxHeight() / 2,
                this.gradientPanel1.getGradientBoxWidth(),
                this.gradientPanel1.getGradientBoxHeight());

        this.gradientPanel1.setListener(new GradientPanel.GradientPanelListener() {

            @Override
            public void enableGradientCorrection(boolean enable) {
                handleEnableGradientCorrection(enable);
            }

            @Override
            public void gradientBoxSizeChange(int height, int width) {
                handleGradientBoxSizeChange(height, width);
            }

            @Override
            public void darkestCornerChange(Corner corner) {
                setGradientStartEndPoints(corner);
                // Reload image
                changeFrame();
            }

            @Override
            public void brightnessIncreaseChange(int brightnessIncrease) {
                // Create equation for gradient
                gradientCorrection.setLightAdding(brightnessIncrease);
                // Reload image
                changeFrame();
            }

            @Override
            public void onlyShowGradient(boolean enable) {
                gradientCorrection.setOnlyShowGradient(enable);
                // Reload image
                changeFrame();
            }

            @Override
            public void backgroundLevelIncreaseChange(int value) {
                gradientCorrection.setBackgroundLevel(value);
                changeFrame();
            }
        });
    }

    private void handleSearchAreaMove() {
        if (this.gradientChangePanelActivate) {
            this.gradientBox.setLocation(this.interactivePanel.getSearchRect().getLocation());
            setGradientStartEndPoints(this.gradientPanel1.getDarkestCorner());
            this.gradientCorrection.updateGradientMask();
            changeFrame();
        }
        triggerAutoFitEyeModelRecompute();
    }

    private void setGradientStartEndPoints(Corner corner) {
        // Default to top left
        switch (corner) {
            case BOTTOMLEFT:
                this.gradientCorrection.setStart(
                        new Point((int) this.gradientBox.getMinX(), (int) this.gradientBox.getMaxY()));
                this.gradientCorrection.setEnd(
                        new Point((int) this.gradientBox.getMaxX(), (int) this.gradientBox.getMinY()));
                break;
            case BOTTOMRIGHT:
                this.gradientCorrection.setStart(
                        new Point((int) this.gradientBox.getMaxX(), (int) this.gradientBox.getMaxY()));
                this.gradientCorrection.setEnd(
                        new Point((int) this.gradientBox.getMinX(), (int) this.gradientBox.getMinY()));
                break;
            case TOPRIGHT:
                this.gradientCorrection.setStart(
                        new Point((int) this.gradientBox.getMaxX(), (int) this.gradientBox.getMinY()));
                this.gradientCorrection.setEnd(
                        new Point((int) this.gradientBox.getMinX(), (int) this.gradientBox.getMaxY()));
                break;
            default:
                this.gradientCorrection.setStart(
                        new Point((int) this.gradientBox.getMinX(), (int) this.gradientBox.getMinY()));
                this.gradientCorrection.setEnd(
                        new Point((int) this.gradientBox.getMaxX(), (int) this.gradientBox.getMaxY()));
        }
    }

    private void handleEnableGradientCorrection(boolean enable) {
        this.gradientChangeMode = enable;
        changeFrame();
    }

    private void handleGradientBoxSizeChange(int height, int width) {
        // Update gradient size
        this.gradientBox.setSize(width, height);

        this.gradientCorrection.setHeight(height);
        this.gradientCorrection.setWidth(width);

        this.gradientCorrection.updateGradientMask();

        // Only update display when we are in the gradient correction panel (mode)
        if (this.gradientChangePanelActivate) {
            this.interactivePanel.setSearchRect(this.gradientBox);
        }
        changeFrame();
    }

    private Rectangle getProperSearchRec() {
        if (this.savedSearchBox != null && this.gradientChangePanelActivate) {
            return new Rectangle(this.savedSearchBox);
        } else {
            return this.interactivePanel.getSearchRect();
        }
    }

    private void addConfig(int currentFrame, String frameFileName) throws HeadlessException {
        // Construct the config
        ConfigutationInfo grayLevelInfo = new ConfigutationInfo();
        grayLevelInfo.background = this.colorSelectionPanel1.getBackgroundGrayValue();
        grayLevelInfo.cr = this.colorSelectionPanel1.getCRGrayValue();
        grayLevelInfo.pupil = this.colorSelectionPanel1.getPupilGrayValue();
        grayLevelInfo.frameFileName = frameFileName;
        grayLevelInfo.unsharpFactor = this.colorSelectionPanel1.getSharpeningFactor();
        grayLevelInfo.unsharpRadious = this.colorSelectionPanel1.getSigma();
        grayLevelInfo.isDetectingPupilAngle = this.colorSelectionPanel1.isDetectingPupilAngle();
        grayLevelInfo.isCRCircle = this.colorSelectionPanel1.isCRIsCircle();
        grayLevelInfo.frameNum = currentFrame;
        grayLevelInfo.point = estimatePupilFromThreshold(currentFrame);
        // Need to use set method here to make sure that the value saved will not change

        grayLevelInfo.setSearchArea(getProperSearchRec());
        if (grayLevelInfo.point != null) {
            // Search if already have the info in the list
            int index = this.configListModel.indexOf(grayLevelInfo);
            if (index < 0) {
                // Add new
                this.configListModel.addElement(grayLevelInfo);
                // Add a voronoi site
                addVoronoiSource(grayLevelInfo.point);
            } else {
                // Replace old one
                ConfigutationInfo oldInfo = (ConfigutationInfo) this.configListModel.set(index, grayLevelInfo);
                // Check if the point changes (Make sure we add info first)
                if (!oldInfo.point.equals(grayLevelInfo.point)) {
                    // Reload all voronoi info if the point changes
                    reloadVoronoiSources();
                }
            }
            drawAllVoronoi();
            repaint();
        } else {
            // Give warning that threshold is not set properly
            JOptionPane.showMessageDialog(this, "Cannot estimate pupil location.  Please make adjustment to pupil threshold.", "Cannot add color config", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeFrame() {
        // grab the value and load the image
        this.frameNum = frameSlider.getValue();
        /**
         * Try setting gray level then if nothing is set then simply signal
         * a change.  We need this because selectGrayLevelFromDistance triggers
         * the change in colorSelection so there is no need to signal it twice
         * by calling colorSelectionHandleSliderStateChange
         */
        selectParametersFromDistance();

        setFrame(frameNum);
        if (eyeFiles != null && eyeFiles.length > frameNum) {
            this.fileNameTextField.setText(eyeFiles[frameNum].getName());

            triggerAutoFitEyeModelRecompute();
        }
    }

    private void estimatePupilLocations() {

        // Ask for a file to save to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EstimatedPupilLocations.txt"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {

            final File saveFile = chooser.getSelectedFile();

            // Set up progress bar for this
            this.loadingProgress.setValue(0);
            this.loadingProgress.setString("Estimating pupil location: 0/"
                    + this.loadingProgress.getMaximum());

            // Sanity check
            if (eyeFiles == null) {
                return;
            }

            this.pointToFrameByEstimatedPupilLocation.setLoadingListener(new ChangeListener() {

                private int progress = 0;

                @Override
                public void stateChanged(ChangeEvent e) {
                    progress++;
                    loadingProgress.setMaximum(eyeFiles.length);
                    loadingProgress.setValue(progress);
                    loadingProgress.setString("Estimating pupil location: "
                            + progress + "/" + eyeFiles.length);
                }
            });

            final GradientCorrection gc;

            if (this.gradientChangeMode) {
                gc = this.gradientCorrection.clone();
            } else {
                gc = null;
            }

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    pointToFrameByEstimatedPupilLocation.loadFrames(eyeFiles,
                            thresholdPanel1.getPupilThresh(),
                            getProperSearchRec(),
                            gc,
                            ESTIMATE_PUPIL_SAMPLING_RATE);
                    try {
                        pointToFrameByEstimatedPupilLocation.save(saveFile);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            t.start();
        }
    }

    private void handleInteractivePanelMouseClicked(MouseEvent evt) {
        // Do not move frame when dropper is activated
        if (!colorSelectionPanel1.isDropperSelected()) {
            int frame = this.pointToFrameByEstimatedPupilLocation.getNearestFrame(this.frameNum, evt.getPoint());
            if (frame >= 0) {
                this.frameSlider.setValue(frame);
            }
        }
    }

    private void loadPupilLocations() {
        // Ask for a file to save to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EstimatedPupilLocations.txt"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                File loadFile = chooser.getSelectedFile();
                this.pointToFrameByEstimatedPupilLocation.loadFromFile(loadFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error loading pupil location file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAutoTestModelCheckBoxAction() {
        if (this.colorSelectionPanel1.isAutoTestModelSelected()) {
            this.fitEyeModelRunner = new FitEyeModelRunner(
                    new FittingListener() {

                        @Override
                        public void setFit(RotatedEllipse2D crFit, RotatedEllipse2D pupilFit) {
                            interactivePanel.setCR(crFit);
                            interactivePanel.setPupil(pupilFit);
                            repaint();
                        }
                    });
            // Start one if pass sanity check
            if (eyeFiles != null && eyeFiles.length > frameNum) {
                GradientCorrection gc = null;

                if (this.gradientChangeMode) {
                    gc = this.gradientCorrection;
                }

                this.fitEyeModelRunner.setParameters(eyeFiles[frameNum],
                        createParameters(), gc);
                // Start the runner
                this.fitEyeModelRunner.start();
            }
        } else {
            // Kill and remove the runner
            if (this.fitEyeModelRunner != null) {
                this.fitEyeModelRunner.kill();
                try {
                    this.fitEyeModelRunner.join();
                } catch (InterruptedException ex) {
                }
                this.fitEyeModelRunner = null;
                interactivePanel.setCR(null);
                interactivePanel.setPupil(null);
            }
        }
    }

    private void triggerAutoFitEyeModelRecompute() {
        FitEyeModelRunner runner = this.fitEyeModelRunner;
        if (runner != null) {
            GradientCorrection gc = null;

            if (this.gradientChangeMode) {
                gc = this.gradientCorrection;
            }

            runner.setParameters(eyeFiles[frameNum], createParameters(), gc);
        }
    }

    /**
     * Create parameters by reading from all panels values.
     */
    private Parameters createParameters() {
        Rectangle searchRect;

        Parameters parameters = new Parameters(
                thresholdPanel1.getCRThresh(), thresholdPanel1.getPupilThresh(),
                this.colorSelectionPanel1.getCRGrayValue(),
                this.colorSelectionPanel1.getPupilGrayValue(),
                this.colorSelectionPanel1.getBackgroundGrayValue(),
                getProperSearchRec(),
                this.colorSelectionPanel1.getSigma(),
                this.colorSelectionPanel1.getSharpeningFactor(),
                this.colorSelectionPanel1.isDetectingPupilAngle(),
                this.colorSelectionPanel1.isCRIsCircle());

        return parameters;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameLabel = new javax.swing.JLabel();
        loadImageButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        searchSpacePanel1 = new buseylab.fiteyemodel.gui.SearchSpacePanel();
        gradientPanel1 = new buseylab.fiteyemodel.gui.GradientPanel();
        thresholdPanel1 = new buseylab.fiteyemodel.gui.ThresholdPanel();
        colorSelectionPanel1 = new buseylab.fiteyemodel.gui.ColorSelectionPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        commentTextPane = new javax.swing.JTextPane();
        loadingProgress = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        configList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        showVoronoiCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        saveSettingButton = new javax.swing.JButton();
        loadSettingButton = new javax.swing.JButton();
        runEyeModelFittingButton = new javax.swing.JButton();
        eyeDirTextField = new javax.swing.JTextField();
        eyeDirLabel = new javax.swing.JLabel();
        frameSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        frameTextField = new javax.swing.JTextField();
        fileNameTextField = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        interactivePanel = new buseylab.fiteyemodel.gui.InteractivePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        frameLabel.setText("Frame"); // NOI18N

        loadImageButton.setText("Browse"); // NOI18N
        loadImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageButtonActionPerformed(evt);
            }
        });

        jTabbedPane1.setOpaque(true);
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addTab("Search Space", searchSpacePanel1);
        jTabbedPane1.addTab("Gradient", gradientPanel1);
        jTabbedPane1.addTab("Thresholds", thresholdPanel1);
        jTabbedPane1.addTab("Eye Model", colorSelectionPanel1);

        jScrollPane2.setViewportView(commentTextPane);

        jTabbedPane1.addTab("Comment", jScrollPane2);

        loadingProgress.setPreferredSize(new java.awt.Dimension(100, 20));
        loadingProgress.setString("");
        loadingProgress.setStringPainted(true);

        configList.setModel(configListModel);
        configList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(configList);

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("-");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("buseylab/fiteyemodel/resources/FitEyeModelSetup"); // NOI18N
        showVoronoiCheckBox.setText(bundle.getString("Show config bound check box text")); // NOI18N
        showVoronoiCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVoronoiCheckBoxActionPerformed(evt);
            }
        });

        saveSettingButton.setText("Save Settings"); // NOI18N
        saveSettingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSettingButtonActionPerformed(evt);
            }
        });

        loadSettingButton.setText("Load Setting"); // NOI18N
        loadSettingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSettingButtonActionPerformed(evt);
            }
        });

        runEyeModelFittingButton.setText(bundle.getString("Run Eye Model Fitting button text")); // NOI18N
        runEyeModelFittingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runEyeModelFittingButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(runEyeModelFittingButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(loadSettingButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, saveSettingButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(63, Short.MAX_VALUE)
                .add(saveSettingButton)
                .add(18, 18, 18)
                .add(loadSettingButton)
                .add(18, 18, 18)
                .add(runEyeModelFittingButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(showVoronoiCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {addButton, deleteButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteButton)
                    .add(addButton)
                    .add(showVoronoiCheckBox)))
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        showVoronoiCheckBox.getAccessibleContext().setAccessibleName("");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 468, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(loadingProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(loadingProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        eyeDirTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eyeDirTextFieldActionPerformed(evt);
            }
        });

        eyeDirLabel.setText("Eye Directory"); // NOI18N

        frameSlider.setMaximum(0);
        frameSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                frameSliderStateChanged(evt);
            }
        });
        frameSlider.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                frameSliderKeyPressed(evt);
            }
        });
        frameSlider.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                frameSliderMouseWheelMoved(evt);
            }
        });

        jLabel1.setText("Image file name"); // NOI18N

        frameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameTextFieldActionPerformed(evt);
            }
        });

        fileNameTextField.setEditable(false);

        interactivePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                interactivePanelMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(interactivePanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, frameSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(frameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(frameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 243, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(eyeDirLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(eyeDirTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadImageButton))
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loadImageButton)
                    .add(eyeDirLabel)
                    .add(eyeDirTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(frameLabel)
                    .add(frameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(fileNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(frameSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void frameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameTextFieldActionPerformed
        // grab the frame number and set the slider appropriately
        // the slider action performed will do the necessary updating        
        frameNum = Integer.parseInt(frameTextField.getText());

        // Cap value
        frameNum = Math.min(frameSlider.getMaximum(),
                Math.max(frameNum, frameSlider.getMinimum()));

        frameSlider.setValue(frameNum);
    }//GEN-LAST:event_frameTextFieldActionPerformed

    private void loadImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImageButtonActionPerformed
        initProject();
    }//GEN-LAST:event_loadImageButtonActionPerformed

    private void frameSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_frameSliderStateChanged
        final int currentFrame = this.frameNum;
        final String frameFileName = this.fileNameTextField.getText();

        // Check if we have to warn user about loosing config
        if (this.notifyDiscardConfiguration) {
            // Check if the user made any change
            if (this.colorSelectionPanel1.isDirty()) {
                this.colorSelectionPanel1.setDirty(false);
                // Show warning
                final ConfigurationLossWarningJDialog dialog =
                        new ConfigurationLossWarningJDialog(this, true);
                dialog.setTitle("Warning Configuration May Be Lost");
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {
                        switch (dialog.getResult()) {
                            case ADD:
                                addConfig(currentFrame, frameFileName);
                        }
                        notifyDiscardConfiguration = !dialog.isDoNotNotify();
                        changeFrame();
                    }
                });
                dialog.setVisible(true);
                return;
            }
        }
        changeFrame();
    }//GEN-LAST:event_frameSliderStateChanged

    private void saveSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSettingButtonActionPerformed
        saveSetting();
    }//GEN-LAST:event_saveSettingButtonActionPerformed

    /**
     * Save setting
     * @return null if cancel or unsuccessful. Otherwise return a saved file
     */
    private File saveSetting() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("FitEyeModel.xml"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }

        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {

            File saveFile = chooser.getSelectedFile();

            ParameterList parameterList = new ParameterList();

            if (configListModel.isEmpty()) {
                Parameters parameters = createParameters();
                parameterList.addParameters(estimatePupilFromThreshold(this.frameNum),
                        this.eyeFiles[this.frameNum].getName(), parameters);
            } else {
                // If there is configuration then loop through each
                Enumeration elem = this.configListModel.elements();
                while (elem.hasMoreElements()) {
                    ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();

                    Parameters parameters = createParameters();
                    parameters.backgroundGrayValue = info.background;
                    parameters.crGrayValue = info.cr;
                    parameters.pupilGrayValue = info.pupil;
                    parameters.searchArea = info.getSearchArea();
                    parameters.unsharpFactor = info.unsharpFactor;
                    parameters.unsharpRadious = info.unsharpRadious;
                    parameters.detectPupilAngle = info.isDetectingPupilAngle;
                    parameters.isCRCircle = info.isCRCircle;

                    parameterList.addParameters(info.point, info.frameFileName, parameters);
                }

            }
            // Add comment parameter list
            parameterList.setComment(this.commentTextPane.getText());

            // Add gradient info
            parameterList.setGradientCorrectionInfo(this.gradientChangeMode,
                    this.gradientPanel1.getDarkestCorner(),
                    this.gradientBox.x, this.gradientBox.y,
                    this.gradientBox.width, this.gradientBox.height,
                    this.gradientCorrection.getLightAdding(),
                    this.gradientCorrection.getBackgroundLevel());

            try {
                // Save parameter list
                parameterList.save(saveFile);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error saving setting",
                        JOptionPane.ERROR_MESSAGE);
            }

            try {
                this.imgProc.save(chooser.getSelectedFile().getParentFile());
            } catch (IOException ex) {
                Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
            }

            return saveFile;
        }

        return null;
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        // Give warning when threshold is dirty
        if (this.thresholdPanel1.isDirty()) {
            JOptionPane.showMessageDialog(this,
                    "Threshold values have been changed.  The changes \n"
                    + "may invalidate the estimated pupil locations.  \n"
                    + "You may need to restimate the locations or load \n"
                    + "another appropriate estimation.",
                    "Pupil locations may be invalid!", JOptionPane.WARNING_MESSAGE);
            this.thresholdPanel1.setDirty(false);
        }

        if (this.jTabbedPane1.getSelectedComponent().equals(this.colorSelectionPanel1)) {
            this.isSelectingColor = true;

            // Change paint to picture so that we can select color
            this.thresholdPanel1.setNoThreshold();
            // repaint image with new info
            setFrame(getFrame());

            this.interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            this.isSelectingColor = false;

            // Make sure that all droppers are off
            this.colorSelectionPanel1.unselectAllDropperButtons();
            if (this.jTabbedPane1.getSelectedComponent().equals(this.searchSpacePanel1)) {
                this.interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else {
                this.interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

        }

        if (this.jTabbedPane1.getSelectedComponent().equals(this.thresholdPanel1)) {
            // Make sure that we are not sharpening here
            setFrame(getFrame());
        }

        if (this.jTabbedPane1.getSelectedComponent().equals(this.gradientPanel1)) {
            this.gradientChangePanelActivate = true;


            setFrame(getFrame());

            // Save search box
            this.savedSearchBox = this.interactivePanel.getSearchRect();

            // Set the color of the search box
            this.interactivePanel.setSearchRecColor(this.gradientBoxColor);

            /* Set the gradient box             */
            this.interactivePanel.setSearchRect(this.gradientBox);

            // Set cursor to a default
            this.interactivePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            // Set tool tip to show gray level
            this.interactivePanel.setShowGrayLevelToolTip(true);
        } else {
            if (this.jTabbedPane1.getSelectedComponent().equals(this.searchSpacePanel1)) {
                this.interactivePanel.setShowGrayLevelToolTip(true);
            } else {
                this.interactivePanel.setShowGrayLevelToolTip(false);
            }

            // Only do this if we are previously in the gradient change mode
            if (this.gradientChangePanelActivate) {
                this.gradientChangePanelActivate = false;

                // Set the color of the search box
                this.interactivePanel.setSearchRecColor(this.gradientBoxColor);

                // Set the color of the search box
                this.interactivePanel.setSearchRecColor(this.searchBoxColor);

                /* Restore last known search box   */
                this.interactivePanel.setSearchRect(this.savedSearchBox);

                // Trigger reloading of search space
                changeFrame();
            }
        }

    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void loadSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSettingButtonActionPerformed
        // Switch to threshold tab.
        this.jTabbedPane1.setSelectedComponent(this.thresholdPanel1);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }

        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            ParameterList parameterList =
                    ParameterList.load(chooser.getSelectedFile());

            // Get comment
            this.commentTextPane.setText(parameterList.getComment());

            // Stop panel from responsing to value changes
            GradientPanelListener listener = this.gradientPanel1.getListener();
            this.gradientPanel1.setListener(null);

            // Set up gradient correction
            this.gradientPanel1.setBrightnessIncrease(parameterList.getGradientBrightnessAddValue());
            this.gradientCorrection.setLightAdding(parameterList.getGradientBrightnessAddValue());

            this.gradientPanel1.setBackGroundLevel(parameterList.getGradientBackgroundLevelValue());
            this.gradientCorrection.setBackgroundLevel(parameterList.getGradientBackgroundLevelValue());

            this.gradientBox.setBounds(parameterList.getGradientBoxGuide());

            this.gradientPanel1.setGradientBoxSize(this.gradientBox.width, this.gradientBox.height);
            this.gradientPanel1.setDarkestCorner(parameterList.getGradientStartCorner());

            // This has to be done ONLY after the gradient box is peoperly
            setGradientStartEndPoints(parameterList.getGradientStartCorner());

            this.gradientPanel1.enableGradientCorrection(parameterList.isGradientCorrecting());

            this.gradientBox.setBounds(parameterList.getGradientBoxGuide());
            if (this.gradientChangePanelActivate) {
                this.interactivePanel.setSearchRect(this.gradientBox);
            }

            // Restore panel interaction
            this.gradientPanel1.setListener(listener);

            // Set the state
            this.gradientChangeMode = parameterList.isGradientCorrecting();

            // Set up initial parameters
            Parameters parameters = parameterList.getFirstParameters();
            if (parameters != null) {
                // Set all parameters accordingly
                this.interactivePanel.setSearchRect(parameters.searchArea);
                this.searchSpacePanel1.setArea(parameters.searchArea.width,
                        parameters.searchArea.height);
                this.colorSelectionPanel1.setBackgroundGrayValue(parameters.backgroundGrayValue);
                this.colorSelectionPanel1.setPupilGrayValue(parameters.pupilGrayValue);
                this.colorSelectionPanel1.setCRGrayValue(parameters.crGrayValue);
                this.colorSelectionPanel1.setSharpeningFactor(parameters.unsharpFactor);
                this.colorSelectionPanel1.setSigma(parameters.unsharpRadious);
                this.colorSelectionPanel1.setCRIsCircle(parameters.isCRCircle);
                this.colorSelectionPanel1.setDirty(false);

                this.thresholdPanel1.setCrThresh(parameters.crThreshold);
                this.thresholdPanel1.setPupilThresh(parameters.pupilThreshold);
            }

            // Clear current model
            this.configListModel.clear();

            for (Iterator<Entry> it = parameterList.iterator(); it.hasNext();) {
                Entry entry = it.next();

                ConfigutationInfo info = new ConfigutationInfo();
                info.frameFileName = entry.filename;
                info.background = entry.parameters.backgroundGrayValue;
                info.cr = entry.parameters.crGrayValue;
                info.pupil = entry.parameters.pupilGrayValue;
                info.unsharpFactor = entry.parameters.unsharpFactor;
                info.unsharpRadious = entry.parameters.unsharpRadious;
                info.isDetectingPupilAngle = entry.parameters.detectPupilAngle;
                info.isCRCircle = entry.parameters.isCRCircle;

                info.setSearchArea(entry.parameters.searchArea);

                // Locate file number for for file name
                if (eyeFiles != null) {
                    File target = new File(eyeFiles[0].getParentFile(),
                            entry.filename);
                    int pos = Arrays.binarySearch(eyeFiles, target);
                    if (pos >= 0) {
                        // Found in list so we add the info to list
                        info.frameNum = pos;
                        this.configListModel.addElement(info);
                    }

                }
            }
            // Update the pupil position in configuration list
            updateEstimateConfigutationPupilLocation();

            // Make system switch to proper config
            selectParametersFromDistance();

            // Load min,max,avg pics is any
            this.imgProc.load(chooser.getSelectedFile().getParentFile());

            repaint();

        }
    }//GEN-LAST:event_loadSettingButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // Remove entries from list
        Object[] selected = configList.getSelectedValues();

        for (int i = 0; i
                < selected.length; i++) {
            configListModel.removeElement(selected[i]);
        }

        reloadVoronoiSources();
        drawAllVoronoi();

        repaint();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Clean dirty flag
        this.colorSelectionPanel1.setDirty(false);

        addConfig(this.frameNum, this.fileNameTextField.getText());
    }//GEN-LAST:event_addButtonActionPerformed

    private void configListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configListMouseClicked
        // Get selected gray level
        ConfigutationInfo grayLevelInfo = (ConfigutationInfo) configList.getSelectedValue();
        if (grayLevelInfo != null) {
            // Set current parameters to the selected one
            frameSlider.setValue(grayLevelInfo.frameNum);

        }
    }//GEN-LAST:event_configListMouseClicked

    private void runEyeModelFittingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runEyeModelFittingButtonActionPerformed

        if (this.isEyeFittingRunning) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Would you like to terminate eye model fitting? "
                    + "Termination may take sometime to complete.",
                    "Abort Eye Model Fitting",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Stop the running through the flag
                this.isEyeFittingRunning = false;
            }

        } else {

            // Save current setting
            final File saveFile = saveSetting();
            if (saveFile != null) {
                final RunEyeModelFittingJDialog d = new RunEyeModelFittingJDialog(this, true);
                d.setTitle("Eye Model Running Parameters");
                d.setMaximumCPU(Runtime.getRuntime().availableProcessors());
                d.setOutputDir(new File(saveFile.getParent(), "Gaze"));
                d.addRunConfirmedListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final java.util.ResourceBundle bundle =
                                java.util.ResourceBundle.getBundle("buseylab/fiteyemodel/resources/FitEyeModelSetup");

                        // Set the flag that eye fitting is running
                        isEyeFittingRunning =
                                true;

                        // Change text to "stop running"
                        runEyeModelFittingButton.setText(
                                bundle.getString("Stop Eye Model Fitting button text"));

                        Thread t = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                runEyeModelFitting(new File(d.getOutputDir()),
                                        d.getNumberCPU(), saveFile, d.isRedo());

                                // Change the flag that eye fitting is running
                                isEyeFittingRunning =
                                        false;

                                // Change text to normal "Run" button
                                runEyeModelFittingButton.setText(
                                        bundle.getString("Run Eye Model Fitting button text"));

                            }
                        });

                        t.start();
                        d.dispose();
                    }
                });

                d.setVisible(true);
            }

        }
    }//GEN-LAST:event_runEyeModelFittingButtonActionPerformed

    private void frameSliderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_frameSliderKeyPressed
        int step = 1;
        // Set up modifier
        switch (evt.getModifiers()) {
            case KeyEvent.SHIFT_MASK:
                step = 1000;
                break;

            case KeyEvent.CTRL_MASK:
                step = 100;
                break;

            case KeyEvent.ALT_MASK:
                step = 10;
                break;

            case KeyEvent.META_MASK:
                step = 5;
                break;

            default:

                return;
        }
        // Move according to arrow button
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.frameSlider.setValue(this.frameSlider.getValue() - step);
                break;

            case KeyEvent.VK_RIGHT:
                this.frameSlider.setValue(this.frameSlider.getValue() + step);
                break;

        }
    }//GEN-LAST:event_frameSliderKeyPressed

    private void showVoronoiCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVoronoiCheckBoxActionPerformed
        if (this.showVoronoiCheckBox.isSelected()) {
            // Add voronoi to picture
            this.interactivePanel.addHighlight(this.voronoiBufferedImage);
        } else {
            this.interactivePanel.removeHighlight(this.voronoiBufferedImage);
        }

        repaint();
    }//GEN-LAST:event_showVoronoiCheckBoxActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveSetting();
    }//GEN-LAST:event_formWindowClosing

    private void eyeDirTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eyeDirTextFieldActionPerformed
        initProject();
    }//GEN-LAST:event_eyeDirTextFieldActionPerformed

    private void interactivePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_interactivePanelMouseClicked
        handleInteractivePanelMouseClicked(evt);
    }//GEN-LAST:event_interactivePanelMouseClicked

    private void frameSliderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_frameSliderMouseWheelMoved
        int step = evt.getWheelRotation();

        this.frameSlider.setValue(this.frameSlider.getValue() - step);
    }//GEN-LAST:event_frameSliderMouseWheelMoved

    private void initProject() {
        // list the eye files and set the text field to reflect their directory
        eyeDir = new File(this.eyeDirTextField.getText());
        eyeDirTextField.setText(eyeDir.getAbsolutePath());

        if (!eyeDir.exists()) {
            // Give warning when file not exists
            JOptionPane.showMessageDialog(this,
                    eyeDirTextField.getText() + " directory does not exists, please select a directory",
                    "Error accessing the direcory", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // list the eye files and set the text field to reflect their directory
        eyeFiles = eyeDir.listFiles(new NotHiddenPictureFilter());
        // Sort the input
        Arrays.sort(eyeFiles, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // Set correct total images
        this.frameSlider.setMaximum(eyeFiles.length - 1);

        // Try getting the first image so that we can set the maximum of slider
        BufferedImage image = ImageUtils.loadRGBImage(eyeFiles[0]);
        if (image != null) {
            this.searchSpacePanel1.setMaximumHeightWidth(image.getWidth(), image.getHeight());
        } else {
            this.searchSpacePanel1.setMaximumHeightWidth(512, 512);
        }

        imgProc = new ThreadedImageProcessor(new ThreadedImageProcessorListener() {

            @Override
            public void progress(int progress) {
                loadingProgress.setMaximum(eyeFiles.length);
                loadingProgress.setValue(progress);
                loadingProgress.setString("Computing min, max & avg " + progress + "/" + eyeFiles.length);
                repaint();
            }

            @Override
            public void complete() {
                loadingProgress.setString("Computing min, max & avg is completed");
                searchSpacePanel1.enableComputeMinMaxAvg();
                repaint();
            }
        });
    }

    /** This method start min max avg image computation */
    @Override
    public void startMinMaxAverageImageComputation() {
        // Get starting dir from the text box
        try {

            imgProc.initialize(eyeFiles);
            Thread imgProcThread = new Thread(imgProc);
            imgProcThread.start();

            // load the first image for display
            frameNum = 0;
            frameSlider.setValue(frameNum);
            this.fileNameTextField.setText(eyeFiles[0].getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** This method stop min max avg image computation */
    @Override
    public void stopMinMaxAverageImageComputation() {
        imgProc.kill();
    }

    /**
     * Warning!! This is a blocking method.  It will compute FitEyeModel for all
     * files
     * @param redo If true will redo all frames. Otherwise if output file exists
     * no model fitting will be performed.
     */
    private void runEyeModelFitting(File outputDir, int numCPUs, File saveFile,
            boolean redo) {
        // Cap number of cpu to the job available
        numCPUs = Math.min(eyeFiles.length, numCPUs);
        final Semaphore sem = new Semaphore(0);
        ParameterList parameters = ParameterList.load(saveFile);
        // Clear out loading progress value
        this.loadingProgress.setValue(0);

        TerminationListener terminationListener = new TerminationListener() {

            @Override
            synchronized public void complete() {
                sem.release();
            }
        };

        int curEyeFile = 0;
        // spawn numCPUs-1 FitEyeModel threads
        for (int i = 0; i < numCPUs && this.isEyeFittingRunning; i++) {
            FitEyeModel fem = null;

            GradientCorrection gc = null;

            if (this.gradientChangeMode) {
                gc = this.gradientCorrection;
            }

            fem = new FitEyeModel(
                    this.eyeFiles[curEyeFile], outputDir.getAbsolutePath(),
                    parameters, gc);
            fem.setTerminationListener(terminationListener);

            Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
            threads.start();
            curEyeFile++;
        }
        // monitor threads, and regenerate dead ones
        while (curEyeFile < eyeFiles.length && this.isEyeFittingRunning) {
            try {
                // Check if there is available thread
                sem.acquire();
            } catch (InterruptedException ex) {
                // Abort when interrupted
                return;
            }
            FitEyeModel fem = null;
            boolean toRun = true;
            if (!redo) {
                // Check if output file already exists
                toRun = !FitEyeModel.isOutputFileExisting(
                        this.eyeFiles[curEyeFile], outputDir);
            }

            if (toRun) {
                GradientCorrection gc = null;

                if (this.gradientChangeMode) {
                    gc = this.gradientCorrection;
                }
                fem = new FitEyeModel(
                        this.eyeFiles[curEyeFile], outputDir.getAbsolutePath(),
                        parameters, gc);
                fem.setTerminationListener(terminationListener);

                Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
                threads.start();
            } else {
                sem.release();
            }

            // Increment the counter
            curEyeFile++;

            loadingProgress.setMaximum(eyeFiles.length);
            loadingProgress.setValue(curEyeFile);
            loadingProgress.setString("Fitting eye model: " + curEyeFile + "/" + eyeFiles.length);

        }
        // wait for remaining threads to die before exiting
        for (int i = 0; i < numCPUs; i++) {
            try {
                sem.acquire();
            } catch (InterruptedException ex) {
                // Abort when interrupted
                return;
            }
        }
        this.loadingProgress.setString("Fitting eye model is completed.");
    }

    // subcomponenets need to know what to paint to
    @Override
    public InteractivePanel getInteractivePanel() {
        return interactivePanel;
    }

    // searchSpacePanel needs to use the ImageProcessor to get min/max/avg images
    @Override
    public ThreadedImageProcessor getImageProcessor() {
        return imgProc;
    }

    @Override
    public int getFrame() {
        return frameNum;
    }
    BufferedImage drawingBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    // this should get called whenever the frame changes from the frame slider
    // gets called from slider statechanged as well as from other classes
    @Override
    public void setFrame(int newFrameNumber) {
        // Sanity check and cap
        if (eyeFiles != null) {
            newFrameNumber = Math.min(eyeFiles.length - 1, newFrameNumber);
            newFrameNumber = Math.max(0, newFrameNumber);

            this.frameNum = newFrameNumber;
            // first disable any modifiers from search space panels
            searchSpacePanel1.setDefaultButton();
            frameTextField.setText(new Integer(newFrameNumber).toString());

            try {
                BufferedImage paintImg = null;

                // Check if we need to load image from file
                if (this.lastFrameNum == this.frameNum && this.bufferedOriginalImage != null) {
                    // Use the previously loaded image
                    paintImg = this.bufferedOriginalImage;
                } else {
                    // Load image
                    paintImg = ImageUtils.loadRGBImage(eyeFiles[newFrameNumber]);

                    // Change buffered data
                    this.lastFrameNum = this.frameNum;
                    this.bufferedOriginalImage = paintImg;
                }

                if (this.drawingBuffer.getWidth() != paintImg.getWidth()
                        || this.drawingBuffer.getHeight() != paintImg.getHeight()) {
                    // We need to change the size of buffer
                    this.drawingBuffer = new BufferedImage(
                            paintImg.getWidth(), paintImg.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                }

                Graphics2D g2d = drawingBuffer.createGraphics();
                g2d.drawImage(paintImg, 0, 0, null);

                if (this.gradientChangeMode) {

                    this.gradientCorrection.setWidth(drawingBuffer.getWidth());
                    this.gradientCorrection.setHeight(drawingBuffer.getHeight());
                    this.gradientCorrection.updateGradientMask();

                    this.gradientCorrection.correctGradient(g2d);
                }
                g2d.dispose();

                // Get proper search space
                Rectangle searchRect = ImageUtils.clipRectangle(drawingBuffer,
                        getProperSearchRec());

                if (this.colorSelectionPanel1.getSigma() > 0 && this.isSelectingColor) {
                    // Avoid avoid loading image from image plus directly since
                    // it cannot load some tiff compression

                    // Limit sharpen to the search space area to increase the speed

                    // Clip the size just to make sure
                    BufferedImage img = drawingBuffer.getSubimage(
                            searchRect.x, searchRect.y,
                            searchRect.width, searchRect.height);

                    ImagePlus imagePlus = new ImagePlus("", img);

                    ImageUtils.unsharpMask(imagePlus.getProcessor(),
                            this.colorSelectionPanel1.getSigma(),
                            this.colorSelectionPanel1.getSharpeningFactor());


                    drawingBuffer.getGraphics().drawImage(
                            ImageUtils.toBufferedImage(imagePlus.getImage()),
                            searchRect.x, searchRect.y, null);

                }
                // user should be able to scroll through sequence with threshold on,
                // so check to see what kind of threshold is set in the thresh panel
                switch (thresholdPanel1.getThresholdType()) {
                    case NO_THRESH_TYPE:
                        interactivePanel.setImage(drawingBuffer);
                        setHighlight();
                        break;
                    case CR_THRESH_TYPE:
                        // find the cr
                        RotatedEllipse2D foundCR = FitEyeModel.findCR(drawingBuffer,
                                searchRect,
                                this.thresholdPanel1.getCRThresh());

                        // draw into the image
                        paintFoundEllisp(foundCR, drawingBuffer,
                                this.thresholdPanel1.getCRThresh());

                        break;
                    case PUPIL_THRESH_TYPE:

                        RotatedEllipse2D foundPupil = FitEyeModel.findPupil(drawingBuffer,
                                searchRect,
                                this.thresholdPanel1.getPupilThresh(),
                                false);

                        // draw into the image
                        paintFoundEllisp(foundPupil, drawingBuffer,
                                this.thresholdPanel1.getPupilThresh());
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method help with setting highlight
     */
    private void setHighlight() {
        interactivePanel.clearHighlight();
        if (this.showVoronoiCheckBox.isSelected()) {
            interactivePanel.addHighlight(this.voronoiBufferedImage);
        }
        BufferedImage img = interactivePanel.getImage();
        if (img != null) {
            int[] pixels = buseylab.fiteyemodel.logic.ImageUtils.RGBtoGray(ImageUtils.getPixels(img));
            if (this.colorSelectionPanel1.isCRColorHeighlighted()) {
                buseylab.fiteyemodel.logic.ImageUtils.grayToRGB(pixels);

                int limit = (this.colorSelectionPanel1.getCRGrayValue()
                        + this.colorSelectionPanel1.getBackgroundGrayValue()) / 2;

                int[] highPix = ImageUtils.threshold(pixels, limit, 255,
                        ImageUtils.createARGB(125, 255, 0, 0));

                BufferedImage highLight = new BufferedImage(img.getWidth(),
                        img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                highLight.setRGB(0, 0, img.getWidth(), img.getHeight(), highPix,
                        0, img.getWidth());
                this.interactivePanel.addHighlight(highLight);
            }

            if (this.colorSelectionPanel1.isPupilColorHeighlighted()) {
                buseylab.fiteyemodel.logic.ImageUtils.grayToRGB(pixels);

                int limit = (this.colorSelectionPanel1.getPupilGrayValue()
                        + this.colorSelectionPanel1.getBackgroundGrayValue()) / 2;

                int[] highPix = ImageUtils.threshold(pixels, 0, limit,
                        ImageUtils.createARGB(125, 0, 255, 0));

                BufferedImage highLight = new BufferedImage(img.getWidth(),
                        img.getHeight(), BufferedImage.TYPE_INT_ARGB);

                highLight.setRGB(0, 0, img.getWidth(), img.getHeight(), highPix,
                        0, img.getWidth());

                this.interactivePanel.addHighlight(highLight);
            }
        }
    }

    /**
     * Internal helper for painting found pupil or cr thresholding result
     */
    private void paintFoundEllisp(RotatedEllipse2D found,
            BufferedImage paintedImg, int threshold) {
        Graphics paintedImgGraphics;
        int[] paintedImgPixels = buseylab.fiteyemodel.logic.ImageUtils.RGBtoGray(
                buseylab.fiteyemodel.logic.ImageUtils.getPixels(paintedImg));
        paintedImgPixels = buseylab.fiteyemodel.logic.ImageUtils.threshold(
                paintedImgPixels, threshold);
        paintedImg.setRGB(0, 0, paintedImg.getWidth(), paintedImg.getHeight(),
                buseylab.fiteyemodel.logic.ImageUtils.grayToRGB(paintedImgPixels), 0,
                paintedImg.getWidth());
        // now draw the CR
        paintedImgGraphics = paintedImg.getGraphics();
        paintedImgGraphics.setColor(Color.RED);

        Graphics2D g2d = (Graphics2D) paintedImgGraphics;

        AffineTransform oldTransform = g2d.getTransform();

        g2d.setTransform(AffineTransform.getRotateInstance(
                found.getAngle(), found.getCenterX(), found.getCenterY()));

        g2d.draw(found);

        g2d.drawLine((int) found.getX(), (int) found.getCenterY(),
                (int) found.getMaxX(), (int) found.getCenterY());

        g2d.setTransform(oldTransform);

        interactivePanel.setImage(paintedImg);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Set look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new FitEyeModelSetup().setVisible(true);
            }
        });
    }

    /**
     * @return null when there is error with file
     */
    private Point2D.Double estimatePupilFromThreshold(int frameNum) {
        Point2D.Double pupil = null;

        BufferedImage paintedImg = ImageUtils.loadRGBImage(eyeFiles[frameNum]);

        if (this.gradientChangeMode) {
            Graphics2D g2d = paintedImg.createGraphics();
            this.gradientCorrection.setWidth(paintedImg.getWidth());
            this.gradientCorrection.setHeight(paintedImg.getHeight());
            this.gradientCorrection.updateGradientMask();

            this.gradientCorrection.correctGradient(g2d);
            g2d.dispose();
        }

        if (paintedImg != null) {

            Rectangle searchArea = null;

            if (!this.configListModel.isEmpty()) {
                // Try getting search rect from the first stored config
                ConfigutationInfo info =
                        (ConfigutationInfo) this.configListModel.firstElement();
                searchArea = info.getSearchArea();
            } else {
                searchArea = getProperSearchRec();
            }
            // Get pupil estimate
            Ellipse2D foundPupil = FitEyeModel.findPupil(paintedImg,
                    searchArea, this.thresholdPanel1.getPupilThresh(),
                    false);

            pupil = new Point2D.Double(foundPupil.getCenterX(),
                    foundPupil.getCenterY());
        }

        return pupil;
    }

    private void updateEstimateConfigutationPupilLocation() {
        for (Enumeration elem = this.configListModel.elements();
                elem.hasMoreElements();) {
            ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();
            Double point = estimatePupilFromThreshold(info.frameNum);
            if (point != null) {
                info.point = point;
            } else {
                // Give warning that threshold is not set properly
                JOptionPane.showMessageDialog(this,
                        "Cannot estimate pupil location for one of the configuration.  Please make adjustment to pupil threshold.",
                        "Configuration point becomes invalid", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        reloadVoronoiSources();
        drawAllVoronoi();
    }

    /**
     * This method uses threshold to approximate current pupil place. Then
     * compute distance from the current spot to all gray level configurations.
     * The panel is then set to the configuration which is closest to it.
     * Currently the configuration includes gray levels and search area
     * @return true when there is a change else false
     */
    private boolean selectParametersFromDistance() {

        // Check if we have gray level configuration
        if (!this.configListModel.isEmpty()) {
            // Estimate pupil location from Threshold
            Point2D.Double pupil = estimatePupilFromThreshold(this.frameNum);

            if (pupil != null) {
                // Find the configuration which is closest to us
                Enumeration elem = this.configListModel.elements();
                // Get the first config
                ConfigutationInfo currentConfig =
                        (ConfigutationInfo) elem.nextElement();
                double distance = pupil.distance(currentConfig.point);

                // Keep finding a closer one
                while (elem.hasMoreElements() && this.frameNum != currentConfig.frameNum) {
                    ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();

                    double newDistance = pupil.distance(info.point);

                    if (this.frameNum == info.frameNum
                            || newDistance < distance) {
                        // Take a new closer one
                        distance = newDistance;
                        currentConfig = info;
                    }
                }

                // Set the current gray level to whatever we get
                this.colorSelectionPanel1.setBackgroundGrayValue(
                        currentConfig.background);
                this.colorSelectionPanel1.setCRGrayValue(currentConfig.cr);
                this.colorSelectionPanel1.setPupilGrayValue(currentConfig.pupil);

                // Set unsharpen
                this.colorSelectionPanel1.setSigma(currentConfig.unsharpRadious);
                this.colorSelectionPanel1.setSharpeningFactor(currentConfig.unsharpFactor);

                // Set search space if not in gradient selection mode
                if (!this.gradientChangePanelActivate) {
                    this.interactivePanel.setSearchRect(currentConfig.getSearchArea());
                }
                this.savedSearchBox = currentConfig.getSearchArea();

                this.searchSpacePanel1.setArea(
                        currentConfig.getSearchArea().width,
                        currentConfig.getSearchArea().height);

                // Set whether we have to use angle detection
                this.colorSelectionPanel1.setDetectPupilAngle(
                        currentConfig.isDetectingPupilAngle);

                // Set whether CR is force to be circle
                this.colorSelectionPanel1.setCRIsCircle(currentConfig.isCRCircle);

                // Clear dirty bit
                this.colorSelectionPanel1.setDirty(false);

                //Mark the config that we pick
                this.configList.setSelectedValue(currentConfig, true);
                return true;
            }
        }

        return false;
    }

    /** Clear all voronoi information */
    private void clearVoronoiInfo() {
        // Init voronoid computation
        int size = Math.max(this.interactivePanel.getWidth(),
                this.interactivePanel.getHeight()) * 3;
        Simplex<Pnt> initialTriangle = new Simplex<Pnt>(
                new Pnt(-size, -size),
                new Pnt(size, -size),
                new Pnt(0, size));

        this.delaunayTriangulation = new DelaunayTriangulation(initialTriangle);
    }

    private void drawAllVoronoi() {
        Graphics2D g = this.voronoiBufferedImage.createGraphics();

        // Clear out old picture
        g.setBackground(new Color(255, 255, 255, 0));
        g.clearRect(0, 0, this.voronoiBufferedImage.getWidth(),
                this.voronoiBufferedImage.getWidth());

        g.setColor(Color.CYAN);
        // Loop through all the edges of the DT (each is done twice)
        for (Simplex<Pnt> triangle : this.delaunayTriangulation) {
            for (Simplex<Pnt> other : this.delaunayTriangulation.neighbors(triangle)) {
                Pnt p = Pnt.circumcenter(triangle.toArray(new Pnt[0]));
                Pnt q = Pnt.circumcenter(other.toArray(new Pnt[0]));
                int px = (int) p.coord(0);
                int py = (int) p.coord(1);
                int qx = (int) q.coord(0);
                int qy = (int) q.coord(1);
                g.drawLine(px, py, qx, qy);
            }
        }
        g.dispose();
    }

    private void addVoronoiSource(Point2D p) {
        Pnt site = new Pnt(p.getX(), p.getY());
        this.delaunayTriangulation.delaunayPlace(site);
    }

    private void reloadVoronoiSources() {
        clearVoronoiInfo();

        for (Enumeration elem = this.configListModel.elements();
                elem.hasMoreElements();) {
            ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();
            addVoronoiSource(info.point);
        }
    }

    /** This method set current eye directory and force the program to load the frames */
    @Override
    public void setEyeDirectory(String eyePath) {
        this.eyeDirTextField.setText(eyePath);
        initProject();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private buseylab.fiteyemodel.gui.ColorSelectionPanel colorSelectionPanel1;
    private javax.swing.JTextPane commentTextPane;
    private javax.swing.JList configList;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel eyeDirLabel;
    private javax.swing.JTextField eyeDirTextField;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel frameLabel;
    private javax.swing.JSlider frameSlider;
    private javax.swing.JTextField frameTextField;
    private buseylab.fiteyemodel.gui.GradientPanel gradientPanel1;
    private buseylab.fiteyemodel.gui.InteractivePanel interactivePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadImageButton;
    private javax.swing.JButton loadSettingButton;
    private javax.swing.JProgressBar loadingProgress;
    private javax.swing.JButton runEyeModelFittingButton;
    private javax.swing.JButton saveSettingButton;
    private buseylab.fiteyemodel.gui.SearchSpacePanel searchSpacePanel1;
    private javax.swing.JCheckBox showVoronoiCheckBox;
    private buseylab.fiteyemodel.gui.ThresholdPanel thresholdPanel1;
    // End of variables declaration//GEN-END:variables

    /** This method is not optimized for being called multiple times  */
    @Override
    public void setImage(BufferedImage img) {
        if (img != null) {
            BufferedImage buffer = img;

            if (this.gradientChangeMode) {
                // Fix gradient before putting image
                buffer = new BufferedImage(buffer.getWidth(), buffer.getHeight(),
                        BufferedImage.TYPE_INT_RGB);

                Graphics2D gd = buffer.createGraphics();
                gd.drawImage(img, 0, 0, null);
                this.gradientCorrection.correctGradient(gd);
                gd.dispose();
            }

            this.interactivePanel.setImage(buffer);
        } else {
            changeFrame();
        }
    }

    @Override
    public void setSearchSpaceSize(int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
