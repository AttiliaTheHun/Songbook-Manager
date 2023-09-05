package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
//import attilathehun.songbook.ui.ProgressDialog;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.controlsfx.dialog.ProgressDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final int EXPORT_OPTION_DEFAULT = 0;
    private static final int EXPORT_OPTION_PRINTABLE = 1;
    private static final int EXPORT_OPTION_SINGLEPAGE = 2;

    private static final Logger logger = LogManager.getLogger(PDFGenerator.class);

    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/DefaultExport.pdf").toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/SinglepageExport.pdf").toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/PrintableExport.pdf").toString();
    private static final String EASTER_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.OUTPUT_FILE_PATH + "/DeluxeExport.pdf").toString();
    private static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/segment").toString();


    public PDFGenerator() {
        try {
            new File(Environment.getInstance().settings.OUTPUT_FILE_PATH).mkdirs();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("PDF Generation Error", "Cannot initialize the output folder!");
        }

    }

    private void progressUI(Task exportTask) {
        ProgressDialog dialog = new ProgressDialog(exportTask);
        dialog.initStyle(StageStyle.UNIFIED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setGraphic(null);
        dialog.setHeaderText(null);
        dialog.titleProperty().bind(exportTask.titleProperty());

        new Thread(exportTask).start();

        dialog.show();
    }

    public void generateSinglePage() {
        logger.info("Exporting singlepage....");

        progressUI(getExportTask(EXPORT_OPTION_SINGLEPAGE));

        logger.info("Exporting finished!");
    }


    public void generateDefault() {
        logger.info("Exporting default....");

        progressUI(getExportTask(EXPORT_OPTION_DEFAULT));

        logger.info("Exporting finished!");
    }

    public void generatePrintable() {
        logger.info("Exporting printable....");

        progressUI(getExportTask(EXPORT_OPTION_PRINTABLE));

        logger.info("Exporting finished!");
    }

    private Task getExportTask(int exportOption) {
        //TODO: implement singlepage and printable
        return new Task<Void>() {
            @Override
            protected Void call() {
                double progress = 0d;
                try {
                    updateTitle("Exporting...");
                    updateMessage("Loading the collection.....0%");
                    ArrayList<Song> collection = Environment.getInstance().getCollectionManager().getFormalCollection();
                    HTMLGenerator htmlGenerator = new HTMLGenerator();
                    int segmentCount = 0;
                    final double segmentProgressWeight = (double) 1 / collection.size() / 2;
                    for (int i = 0; i < collection.size(); i += 2) {
                        File inputHTML = new File(htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentCount));
                        String segmentFilePath = htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentCount);
                        segmentCount++;
                        progress += segmentProgressWeight;
                        updateProgress(progress, 1d);
                        updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                        Thread.sleep(100);
                    }

                    updateProgress(0.5d, 1d);
                    updateMessage("Converting HTML segments to PDF segments.....50%");
                    Thread.sleep(100);
                    try {

                        ProcessBuilder processBuilder = new ProcessBuilder();
                        //System.out.println(String.format("cd scripts & node html_to_pdf.js %s %d", Environment.getInstance().settings.TEMP_FILE_PATH, segmentCount));
                        processBuilder.command("cmd.exe", "/c", String.format("cd %s & node html_to_pdf.js %s %d", Environment.getInstance().settings.SCRIPTS_FILE_PATH, Environment.getInstance().settings.TEMP_FILE_PATH, segmentCount));
                        //processBuilder.command("cmd.exe", "/c", "timeout 5");
                        processBuilder.directory(new File(System.getProperty("user.dir")));
                        processBuilder.inheritIO();
                        Process process = processBuilder.start();
                        updateProgress(0.85d, 1d);
                        updateMessage("Converting HTML segments to PDF segments.....85%");
                        process.waitFor();
                        updateMessage("Joining PDF segments.....85%");
                        joinSegments(segmentCount, DEFAULT_PDF_OUTPUT_PATH);
                        updateMessage("Finished.....100%");
                        updateProgress(1d, 1d);
                        Environment.showMessage("PDF generation finished", "You can view the newly generated file in this location: " + DEFAULT_PDF_OUTPUT_PATH);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        Environment.showErrorMessage("PDF Generation Failed", e.getMessage());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
                return Void.TYPE.cast(null);
            }
        };
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
