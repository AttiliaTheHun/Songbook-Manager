package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
//import attilathehun.songbook.ui.ProgressDialog;
import attilathehun.songbook.environment.EnvironmentVerificator;
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

import java.awt.*;
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

    private static final int PREVIEW_SEGMENT_NUMBER = 96222;

    private static final String SCRIPT_OPTION_LANDSCAPE = "--landscape";

    private static final String SCRIPT_OPTION_SINGLE_SEGMENT = "--single-segment";

    private static final Logger logger = LogManager.getLogger(PDFGenerator.class);

    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH + "/DefaultExport.pdf").toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH + "/SinglepageExport.pdf").toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH + "/PrintableExport.pdf").toString();
    private static final String EASTER_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH + "/DeluxeExport.pdf").toString();
    private static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment").toString();

    private final CollectionManager manager;

    private boolean FLAG_SKIP_EVERYTHING = false;


    /**
     * The actual constructor. Upon instantiation creates an output folder.
     * @param manager the Collection Manager to use (null if default)
     */
    public PDFGenerator(CollectionManager manager) throws IllegalStateException {
        if (!new EnvironmentVerificator().verifyScripts()) {
            FLAG_SKIP_EVERYTHING = true;
            logger.info("Crippled PDFGenerator instantiated: no scripts folder found!");
            depart();
        }
        if (manager == null) {
            this.manager = Environment.getInstance().getCollectionManager().copy();
        } else {
            this.manager = manager.copy();
        }
        try {
            new File(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH).mkdirs();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("PDF Generation Error", "Cannot initialize the output folder!");
        }
    }

    public PDFGenerator() {
        this(Environment.getInstance().getCollectionManager());
    }

    /**
     * Creates a progress dialog for the given export Task.
     * @param exportTask target export task
     */
    private Object progressUI(Task exportTask) {
        ProgressDialog dialog = new ProgressDialog(exportTask);
        dialog.initStyle(StageStyle.UNIFIED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setGraphic(null);
        dialog.setHeaderText(null);
        dialog.titleProperty().bind(exportTask.titleProperty());

        new Thread(exportTask).start();

        dialog.showAndWait();

        return exportTask.getValue();
    }

    /**
     * Generates a portrait-oriented PDF with each song having its own page. Songs are ordered alphabetically.
     */
    public void generateSinglePage() {
        logger.info("Exporting singlepage....");

        progressUI(getExportTask(EXPORT_OPTION_SINGLEPAGE));

        logger.info("Exporting finished!");
    }

    /**
     * Generates a landscape-oriented PDF with two songs on a page. Songs are ordered alphabetically.
     */
    public void generateDefault() {
        logger.info("Exporting default....");

        progressUI(getExportTask(EXPORT_OPTION_DEFAULT));

        logger.info("Exporting finished!");
    }

    /**
     * Generates a landscape-oriented PDF with two songs on a page. Songs are ordered alphabetically, but their position
     * is optimised for two-sided printing (1-4 & 3-2).
     */
    public void generatePrintable() {
        logger.info("Exporting printable....");

        progressUI(getExportTask(EXPORT_OPTION_PRINTABLE));

        logger.info("Exporting finished!");
    }

    /**
     * Constructs an export Task for the specific export option.
     * @param exportOption EXPORT_OPTION_PRINTABLE, EXPORT_OPTION_SINGLEPAGE or EXPORT_OPTION_DEFAULT
     * @return the Task object
     */
    private Task getExportTask(int exportOption) {
        return new Task<Void>() {
            @Override
            protected Void call() {
                double progress = 0d;
                try {

                    final String EXPORT_FILE_PATH;
                    switch (exportOption) {
                        case EXPORT_OPTION_PRINTABLE -> EXPORT_FILE_PATH = PRINTABLE_PDF_OUTPUT_PATH;
                        case EXPORT_OPTION_SINGLEPAGE -> EXPORT_FILE_PATH = SINGLEPAGE_PDF_OUTPUT_PATH;
                        default -> EXPORT_FILE_PATH = DEFAULT_PDF_OUTPUT_PATH;
                    }

                    Environment.FLAG_IGNORE_SEGMENTS = true;
                    updateTitle("Exporting...");
                    updateMessage("Loading the collection.....0%");
                    ArrayList<Song> collection = manager.getFormalCollection();
                    HTMLGenerator htmlGenerator = new HTMLGenerator();
                    int segmentCount = 0;
                    if (exportOption == EXPORT_OPTION_DEFAULT) {
                        final double segmentProgressWeight = ((double) 1 / collection.size() / 2) / 2;
                        for (int i = 0; i < collection.size(); i += 2) {
                            String segmentFilePath = htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentCount, manager);
                            segmentCount++;
                            progress += segmentProgressWeight;
                            updateProgress(progress, 1d);
                            updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                            Thread.sleep(100);
                        }
                    } else if (exportOption == EXPORT_OPTION_PRINTABLE) {
                        final double segmentProgressWeight = ((double) 1 / collection.size() / 4) / 2;
                        for (int i = 0; i < collection.size(); i += 4) {
                            if (i + 4 > collection.size()) {
                                break;
                            }
                            String firstSegmentFilePath = htmlGenerator.generateSegmentFile(collection.get(i), collection.get(i + 3), segmentCount++, manager);
                            String secondSegmentFilePath = htmlGenerator.generateSegmentFile(collection.get(i + 2), collection.get(i + 1), segmentCount++, manager);
                            progress += segmentProgressWeight;
                            updateProgress(progress, 1d);
                            updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                            Thread.sleep(100);
                        }
                        switch (collection.size() % 4) {
                           case 1 -> {
                                String segmentFilePath = htmlGenerator.generateSegmentFile(collection.get(collection.size() - 1), CollectionManager.getShadowSong(), segmentCount, manager);
                                segmentCount++;
                                progress += segmentProgressWeight;
                                updateProgress(progress, 1d);
                                updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                                Thread.sleep(100);
                            }
                            case 2 -> {
                                String firstSegmentFilePath = htmlGenerator.generateSegmentFile(collection.get(collection.size() - 2), CollectionManager.getShadowSong(), segmentCount++, manager);
                                String secondSegmentFilePath = htmlGenerator.generateSegmentFile(CollectionManager.getShadowSong(), collection.get(collection.size() - 1), segmentCount++, manager);
                                progress += segmentProgressWeight;
                                updateProgress(progress, 1d);
                                updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                                Thread.sleep(100);
                            }
                            case 3 -> {
                                String firstSegmentFilePath = htmlGenerator.generateSegmentFile(collection.get(collection.size() - 3), collection.get(collection.size() - 1), segmentCount++, manager);
                                String secondSegmentFilePath = htmlGenerator.generateSegmentFile(CollectionManager.getShadowSong(), collection.get(collection.size() - 2), segmentCount++, manager);
                                progress += segmentProgressWeight;
                                updateProgress(progress, 1d);
                                updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                                Thread.sleep(100);
                            }
                        }
                    }else if (exportOption == EXPORT_OPTION_SINGLEPAGE) {
                        final double segmentProgressWeight = ((double) 1 / collection.size() / 2);
                        for (int i = 0; i < collection.size(); i++) {
                            String segmentFilePath = htmlGenerator.generatePrintableSongFile(collection.get(i), segmentCount, manager);
                            segmentCount++;
                            progress += segmentProgressWeight;
                            updateProgress(progress, 1d);
                            updateMessage("Generating HTML segments....." + (int) (progress * 100) + "%");
                            Thread.sleep(100);
                        }
                    }
                    logger.debug(String.format("collection size: %d segment count: %d size %% 4: %d", collection.size(), segmentCount, collection.size() % 4));

                    updateProgress(0.5d, 1d);
                    updateMessage("Converting HTML segments to PDF segments.....50%");
                    Thread.sleep(100);
                    try {

                        ProcessBuilder processBuilder = new ProcessBuilder();
                        String options = "";
                        if (exportOption != EXPORT_OPTION_SINGLEPAGE) {
                            options = SCRIPT_OPTION_LANDSCAPE;
                        }
                        processBuilder.command("cmd.exe", "/c", String.format("cd %s & node html_to_pdf.js %s %s %d", Environment.getInstance().settings.environment.SCRIPTS_FILE_PATH, Environment.getInstance().settings.environment.TEMP_FILE_PATH, options, segmentCount));
                        processBuilder.directory(new File(System.getProperty("user.dir")));
                        processBuilder.inheritIO();
                        Process process = processBuilder.start();
                        updateProgress(0.85d, 1d);
                        updateMessage("Converting HTML segments to PDF segments.....85%");
                        process.waitFor();
                        updateMessage("Joining PDF segments.....85%");

                        joinSegments(segmentCount, EXPORT_FILE_PATH);

                        updateMessage("Finished.....100%");
                        updateProgress(1d, 1d);
                        Environment.FLAG_IGNORE_SEGMENTS = false;
                        Environment.showMessage("PDF generation finished", "You can view the newly generated file in this location: " + EXPORT_FILE_PATH);
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


    /**
     * Joins together segments of the PDF.
     * @param segmentCount number of the segments
     * @param documentName final file name
     * @throws IOException
     */
    private void joinSegments(int segmentCount, String documentName) throws IOException {
        PDFMergerUtility ut = new PDFMergerUtility();
        for (int i = 0; i < segmentCount; i++) {
            ut.addSource(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment" + i + ".pdf");
        }
        ut.setDestinationFileName(documentName);
        MemoryUsageSetting settings = MemoryUsageSetting.setupMainMemoryOnly().setTempDir(new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH));
        ut.mergeDocuments(settings);
    }

    /**
     * Generates a single portrait-oriented PDF segment file of a song that can be opened for a preview.
     * @param s target song
     * @return path to the preview file
     */
    public String generatePreview(Song s) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }

        return (String) progressUI(new Task() {
            @Override
            protected Object call() throws Exception {
                updateTitle("Generating preview...");
                updateMessage("Applying song template.....0%");
                updateProgress(0d, 1d);
                String segmentFilePath = new HTMLGenerator().generatePrintableSongFile(s, PREVIEW_SEGMENT_NUMBER, manager);
                updateValue(segmentFilePath);
                try {

                    updateMessage("Converting HTML to PDF.....50%");
                    updateProgress(0.50d, 1d);
                    Environment.FLAG_IGNORE_SEGMENTS = true;
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    String options = SCRIPT_OPTION_SINGLE_SEGMENT;
                    processBuilder.command("cmd.exe", "/c", String.format("cd %s & node html_to_pdf.js %s %s %d", Environment.getInstance().settings.environment.SCRIPTS_FILE_PATH, Environment.getInstance().settings.environment.TEMP_FILE_PATH, options, PREVIEW_SEGMENT_NUMBER));
                    processBuilder.directory(new File(System.getProperty("user.dir")));
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();
                    process.waitFor();
                    Environment.FLAG_IGNORE_SEGMENTS = false;
                    updateMessage("Finished.....100%");
                    updateProgress(1d, 1d);
                    Thread.sleep(300);

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    Environment.showErrorMessage("Error", "Error when creating the preview");
                }
                return segmentFilePath;
            }
        });

    }

    /**
     * Generates a single landscape-oriented PDF segment file of a page of the songbook that can be opened for a preview.
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @return path to the preview file
     */
    public String generatePreview(Song songOne, Song songTwo) {
        if (songOne == null || songTwo == null) {
            throw new IllegalArgumentException();
        }

        return (String) progressUI(new Task() {
            @Override
            protected Object call() throws Exception {
                updateTitle("Generating preview...");
                updateMessage("Copying current_page HTML.....0%");
                updateProgress(0d, 1d);
                String segmentFilePath = new HTMLGenerator().generateSegmentFile(songOne, songTwo, PREVIEW_SEGMENT_NUMBER, manager);
                updateValue(segmentFilePath);
                try {


                    updateMessage("Converting HTML to PDF.....50%");
                    updateProgress(0.50d, 1d);
                    Environment.FLAG_IGNORE_SEGMENTS = true;
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    String options = String.format("%s %s", SCRIPT_OPTION_LANDSCAPE, SCRIPT_OPTION_SINGLE_SEGMENT);
                    processBuilder.command("cmd.exe", "/c", String.format("cd %s & node html_to_pdf.js %s %s  %d", Environment.getInstance().settings.environment.SCRIPTS_FILE_PATH, Environment.getInstance().settings.environment.TEMP_FILE_PATH, options, PREVIEW_SEGMENT_NUMBER));
                    processBuilder.directory(new File(System.getProperty("user.dir")));
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();
                    process.waitFor();
                    Environment.FLAG_IGNORE_SEGMENTS = false;
                    updateMessage("Finished.....100%");
                    updateProgress(1d, 1d);
                    Thread.sleep(300);

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    Environment.showErrorMessage("Error", "Error when creating the preview");
                }
                return segmentFilePath;
            }
        });




    }

    private void depart() {
        if (FLAG_SKIP_EVERYTHING) {
            throw new IllegalStateException("[FLAG_SKIP_EVERYTHING] To preform a PDF conversion you need to provide a valid scripts folder.");
        }
    }

}
