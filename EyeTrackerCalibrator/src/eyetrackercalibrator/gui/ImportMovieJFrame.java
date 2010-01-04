/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImportMovieJFrame.java
 *
 * Created on Dec 28, 2009, 1:49:33 PM
 */
package eyetrackercalibrator.gui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author SQ
 */
public class ImportMovieJFrame extends javax.swing.JFrame {

    protected File lastSelectedLocation = null;
    protected File outputDir = null;
    protected File ffmpegFile = null;
    protected String ffmpegAdvanceArguments = "";
    public final static String DEINTERLACE_ARG = "-deinterlace";
    public final static String SAME_QALITY_ARG = "-sameq";
    public final static String H264_ARG = "-pix_fmt h264";

    /** Creates new form ImportMovieJFrame */
    public ImportMovieJFrame(File importToDir, File ffmpeg) {
        if (importToDir == null) {
            throw new NullPointerException("Import target cannot be null");
        }

        if (ffmpeg == null) {
            throw new NullPointerException("ffmpeg location cannot be null");
        }

        this.outputDir = importToDir;
        this.ffmpegFile = ffmpeg;

        initComponents();
    }

    protected String creteFFMPEGArguments() {
        String arg = ffmpegAdvanceArguments;
        if (this.deinterlaceCheckBox.isSelected()) {
            arg += " " + DEINTERLACE_ARG;
        }
        if (this.sameQualityCheckBox.isSelected()) {
            arg += " " + SAME_QALITY_ARG;
        }
        if (this.h264CheckBox.isSelected()) {
            arg += " " + H264_ARG;
        }
        return arg;
    }

    protected String parseAdvanceArgument(String advanceArg) {
        // Clean up white space first
        advanceArg = advanceArg.trim();
        advanceArg = advanceArg.replaceAll("\\s+", " ");

        // See if we have basic arg in there
        if (advanceArg.contains(DEINTERLACE_ARG)) {
            this.deinterlaceCheckBox.setSelected(true);
            advanceArg.replaceAll(DEINTERLACE_ARG, "");
        } else {
            this.deinterlaceCheckBox.setSelected(false);
        }

        if (advanceArg.contains(SAME_QALITY_ARG)) {
            this.sameQualityCheckBox.setSelected(true);
            advanceArg.replaceAll(SAME_QALITY_ARG, "");
        } else {
            this.sameQualityCheckBox.setSelected(false);
        }

        if (advanceArg.contains(H264_ARG)) {
            this.h264CheckBox.setSelected(true);
            advanceArg.replaceAll(H264_ARG, "");
        } else {
            this.h264CheckBox.setSelected(false);
        }

        // Clean spaces one more time
        advanceArg = advanceArg.replaceAll("\\s+", " ");

        return advanceArg;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        movieFileTextField = new javax.swing.JTextField();
        movieBrowseButton = new javax.swing.JButton();
        advanceButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        progressTextArea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        importButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jpgRadioButton = new javax.swing.JRadioButton();
        tiffRadioButton = new javax.swing.JRadioButton();
        deinterlaceCheckBox = new javax.swing.JCheckBox();
        sameQualityCheckBox = new javax.swing.JCheckBox();
        h264CheckBox = new javax.swing.JCheckBox();
        testImportButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Source");

        movieBrowseButton.setText("Browse");
        movieBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movieBrowseButtonActionPerformed(evt);
            }
        });

        advanceButton.setText("Advance Setup");
        advanceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advanceButtonActionPerformed(evt);
            }
        });

        progressTextArea.setColumns(20);
        progressTextArea.setEditable(false);
        progressTextArea.setLineWrap(true);
        progressTextArea.setRows(5);
        progressTextArea.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(progressTextArea);

        jLabel2.setText("If importing fails, try changing advance setup");

        cancelButton.setText("Cancel");

        jLabel3.setText("Progress output");

        importButton.setText("Import");

        jLabel4.setText("Output file type:");

        fileTypeButtonGroup.add(jpgRadioButton);
        jpgRadioButton.setSelected(true);
        jpgRadioButton.setText("jpg");
        jpgRadioButton.setToolTipText("Smaller file size but poorer image quality");

        fileTypeButtonGroup.add(tiffRadioButton);
        tiffRadioButton.setText("tiff");
        tiffRadioButton.setToolTipText("Lerger file size but better image quality");

        deinterlaceCheckBox.setText("deinterlace");

        sameQualityCheckBox.setText("force quality to be the same as movie (Increase file sizes)");

        h264CheckBox.setText("input is H.264/MPEG-4");

        testImportButton.setText("Test Import");
        testImportButton.setToolTipText("Try importing one frame to test configuration");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(advanceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(movieFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(movieBrowseButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(importButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jpgRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tiffRadioButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sameQualityCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                        .addComponent(testImportButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(deinterlaceCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(h264CheckBox))
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(movieFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(movieBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(advanceButton)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jpgRadioButton)
                    .addComponent(tiffRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deinterlaceCheckBox)
                    .addComponent(h264CheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sameQualityCheckBox)
                    .addComponent(testImportButton))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(importButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void movieBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movieBrowseButtonActionPerformed
        browseFile(this.movieFileTextField);
    }//GEN-LAST:event_movieBrowseButtonActionPerformed

    private void advanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advanceButtonActionPerformed
        // Construct advance argument here
        String advanceArg = creteFFMPEGArguments();

        JOptionPane.showInputDialog("Please enter arguments for ffmpeg.  Do not include \"-i\" and output option.");

    }//GEN-LAST:event_advanceButtonActionPerformed

    private void browseFile(JTextField targetField) {
        // Set text box with directory that user chose.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (targetField.getText().length() > 1) {
            // Get current selection from text field
            fileChooser.setSelectedFile(new File(targetField.getText()));
        } else {
            // Otherwise open to latest location if there is nothing in the field text
            fileChooser.setSelectedFile(lastSelectedLocation);
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Save current position
            lastSelectedLocation = fileChooser.getSelectedFile();
            targetField.setText(lastSelectedLocation.getAbsolutePath());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advanceButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox deinterlaceCheckBox;
    private javax.swing.ButtonGroup fileTypeButtonGroup;
    private javax.swing.JCheckBox h264CheckBox;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jpgRadioButton;
    private javax.swing.JButton movieBrowseButton;
    private javax.swing.JTextField movieFileTextField;
    private javax.swing.JTextArea progressTextArea;
    private javax.swing.JCheckBox sameQualityCheckBox;
    private javax.swing.JButton testImportButton;
    private javax.swing.JRadioButton tiffRadioButton;
    // End of variables declaration//GEN-END:variables
}
