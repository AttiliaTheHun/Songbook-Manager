package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.ui.ProgressDialog;
import javafx.concurrent.Task;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class serves as a middleware between the rest of the code and the underlying scripts used for the conversion of
 * HTML into a PDF file.
 */
public class PDFGenerator {

    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/DefaultExport.pdf").toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/SinglepageExport.pdf").toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/PrintableExport.pdf").toString();
    private static final String EASTER_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/DeluxeExport.pdf").toString();
    private static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/segment").toString();

    private boolean IS_IT_EASTER_ALREADY = false;

    public PDFGenerator(boolean deluxe) {
        IS_IT_EASTER_ALREADY = deluxe;
        try {
            new File(Environment.getInstance().settings.OUTPUT_FILE_PATH).mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("PDF Generation Error", "Cannot initialize the output folder!");
        }

    }

    public PDFGenerator() {
        this(false);
    }

    public void generateSinglePage() {
        //TODO:
    }

    //TODO:
    public void generateDefault() {
        Task task = new Task<Void>() {
            @Override
            protected Void call() {

                ArrayList<Song> collection = Environment.getInstance().getCollectionManager().getFormalCollection();
                HTMLGenerator htmlGenerator = new HTMLGenerator();
                int segmentCount = 0;
                final double segmentProgressWeight = (double) 1 / ((double) collection.size() / 2);
                for (int i = 0; i < collection.size(); i += 2) {
                    File inputHTML = new File(htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentCount));
                    String segmentFilePath = htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentCount);
                    segmentCount++;
                    //dialog.setProgress(dialog.getProgress() + segmentProgressWeight);
                    //dialog.setText("Generating HTML segments....." + dialog.getProgress() + "%");
                    updateProgress(getProgress() + segmentProgressWeight, 1d);
                    updateMessage("Generating HTML segments....." + getProgress() + "%");
                }
                //dialog.setText("Converting HTML segments to PDF segments.....50%");
                //dialog.setProgress(0.5d);
                updateProgress(0.5d, 1d);
                updateMessage("Converting HTML segments to PDF segments.....50%");
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    //processBuilder.command("cmd.exe", "/c", String.format("cd scripts & node html_to_pdf.js %s %d", Environment.getInstance().settings.TEMP_FILE_PATH, segmentCount));
                    processBuilder.command("cmd.exe", "/c", "timeout 5");
                    processBuilder.directory(new File(System.getProperty("user.dir")));
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();
                    //dialog.setProgress(0.85d);
                    //dialog.setText("Converting HTML segments to PDF segments.....85%");
                    updateProgress(0.85d, 1d);
                    updateMessage("Converting HTML segments to PDF segments.....85%");
                    process.waitFor();
                    // dialog.setText("Joining PDF segments.....85%");
                    updateMessage("Joining PDF segments.....85%");
                    Thread.sleep(3 * 1000);
                    //joinSegments(segmentCount, DEFAULT_PDF_OUTPUT_PATH);
                    //dialog.setText("Finished.....100%");
                    updateMessage("Finished.....100%");
                    updateProgress(1d, 1d);
                    //dialog.setProgress(1d);
                    Thread.sleep(200);
                    //dialog.hide();
                    //Environment.showMessage("PDF generation finished", "You can view the newly generated file in this location: " + DEFAULT_PDF_OUTPUT_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                    Environment.getInstance().logTimestamp();
                    e.printStackTrace(Environment.getInstance().getLogPrintStream());
                    Environment.showErrorMessage("PDF Generation Failed", e.getMessage());
                }
                return Void.TYPE.cast(null);
            }
        };

        ProgressDialog dialog = new ProgressDialog(0.2d);
        dialog.setTitle("Generating PDF...");
        dialog.setText("Launching conversion thread.....0%");
        //dialog.bind(task);
        // new Thread(task).start();
        dialog.show();
        try {
            dialog.setProgress(0.6d);
            dialog.setText("Joining PDF segments.....85%");

            Thread.sleep(3 * 1000);
            //joinSegments(segmentCount, DEFAULT_PDF_OUTPUT_PATH);
            dialog.setText("Finished.....100%");


            dialog.setProgress(1d);
            Thread.sleep(200);
            dialog.hide();
        } catch (Exception e) {

        }

        Environment.showMessage("PDF generation finished", "You can view the newly generated file in this location: " + DEFAULT_PDF_OUTPUT_PATH);


    }


    public void generatePrintable() {
        //TODO:
    }


    private void joinSegments(int segmentCount, String documentName) throws IOException {
        PDFMergerUtility ut = new PDFMergerUtility();
        for (int i = 0; i < segmentCount; i++) {
            ut.addSource(Environment.getInstance().settings.TEMP_FILE_PATH + "/segment" + i + ".pdf");
        }
        ut.setDestinationFileName(documentName);
        MemoryUsageSetting settings = MemoryUsageSetting.setupMainMemoryOnly().setTempDir(new File(Environment.getInstance().settings.TEMP_FILE_PATH));
        ut.mergeDocuments(settings);
    }

}
