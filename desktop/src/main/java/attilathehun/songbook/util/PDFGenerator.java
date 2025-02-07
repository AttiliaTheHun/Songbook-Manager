package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class PDFGenerator {
    private static final Logger logger = LogManager.getLogger(PDFGenerator.class);
    public static final int PREVIEW_SEGMENT_NUMBER = -1;
    public static final String DEFAULT_SEGMENT_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/segment%d%s").toString();
    public static final String PREVIEW_SEGMENT_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/segment_preview%s").toString();
    public static final String EXTENSION_HTML = ".html";
    public static final String EXTENSION_PDF = ".pdf";
    private static final int EXPORT_OPTION_DEFAULT = 0;
    private static final int EXPORT_OPTION_PRINTABLE = 1;
    private static final int EXPORT_OPTION_SINGLEPAGE = 2;
    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_DEFAULT_FILE_NAME")).toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_SINGLEPAGE_FILE_NAME")).toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_PRINTABLE_FILE_NAME")).toString();
    private final CollectionManager manager;

    /**
     * Default constructor that uses the default {@link CollectionManager}.
     */
    public PDFGenerator() {
        this(Environment.getInstance().getCollectionManager());
    }

    /**
     * Constructor that allows to use a custom {@link CollectionManager}.
     * @param manager target collection manager
     */
    public PDFGenerator(final CollectionManager manager) {
        if (manager == null) {
            this.manager = Environment.getInstance().getCollectionManager().copy();
        } else {
            this.manager = manager.copy();
        }
        try {
            new File((String) SettingsManager.getInstance().getValue("EXPORT_FILE_PATH")).mkdirs();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("PDF Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Cannot initialize the output folder!").setParent(SongbookApplication.getMainWindow())
                    .addOkButton().build().open();
            throw new RuntimeException("ignore");
        }
    }

    /**
     * Generates a landscape-oriented alphabetically-ordered two-songs-per-page PDF file to the default export location.
     */
    public void generateDefault() {
        exec(EXPORT_OPTION_DEFAULT);
    }

    /**
     * Generates a portrait-oriented alphabetically-ordered one-song-per-page PDF file to the default export location.
     */
    public void generateSinglepage() {
        exec(EXPORT_OPTION_SINGLEPAGE);
    }

    /**
     * Generates a landscape-oriented two-songs-per-page PDF file formatted for two-sided printing to the default export location.
     */
    public void generatePrintable() {
        exec(EXPORT_OPTION_PRINTABLE);
    }

    /**
     * Generates a preview portrait-oriented PDF file with a single song to the temp folder. Creates a notification dialog to show to the user
     * while the work is done on a background thread.
     *
     * @param s target song
     * @return path to the PDF file
     */
    public String generatePreview(final Song s)  {
        final AlertDialog dialog = Misc.createProgressIndicatorDialog("Exporting", "Generating preview...", SongbookApplication.getMainWindow());
        final CompletableFuture<String> path = new CompletableFuture<>();

        new Thread(() -> {
            try (final BrowserFactory factory = BrowserFactory.getInstance()) {
                final Browser browser = factory.getBrowserInstance();
                final Page page = browser.newPage();
                final String HTMLSegmentFilePath = new HTMLGenerator().generatePrintableSongFile(s, PREVIEW_SEGMENT_NUMBER);
                page.navigate(HTMLSegmentFilePath);
                final String outputPath = HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF);
                page.pdf(BrowserFactory.getPrintOptionsPortrait().setPath(Paths.get(outputPath)));
                path.complete(outputPath);
            }
            Platform.runLater(dialog::close);
        }).start();
        String result = null;
        dialog.open();
        try {
            result = path.get();
        } catch (final Exception e) {
            logger.error("Failed to wait for result", e);
        }
        return result;
    }

    /**
     * Generates a preview landscape-oriented PDF file with a page of the songbook (technically any two songs) to the temp folder. Creates a notification dialog to
     * show while the work is done on a background thread.
     *
     * @param s1 song one (on the left)
     * @param s2 song two (on the right)
     * @return path to the PDF file
     */
    public String generatePreview(final Song s1, final Song s2) {
        final AlertDialog dialog = Misc.createProgressIndicatorDialog("Exporting", "Generating preview...", SongbookApplication.getMainWindow());
        final CompletableFuture<String> path = new CompletableFuture<>();

        new Thread(() -> {
                try (final BrowserFactory factory = BrowserFactory.getInstance()) {
                    final Browser browser = factory.getBrowserInstance();
                    final Page page = browser.newPage();
                    final String HTMLSegmentFilePath = new HTMLGenerator().generateSegmentFile(s1, s2, PREVIEW_SEGMENT_NUMBER);
                    page.navigate(HTMLSegmentFilePath);
                    final String outputPath = HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF);
                    page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(outputPath)));
                    path.complete(outputPath);
                }
                Platform.runLater(dialog::close);
        }).start();
        String result = null;
        dialog.open();
        try {
            result = path.get();
        } catch (final Exception e) {
            logger.error("Failed to wait for result", e);
        }
        return result;
    }

    /**
     * Private API. Begins the export process. Option options are {@link #EXPORT_OPTION_DEFAULT}, {@link #EXPORT_OPTION_PRINTABLE} and
     * {@link #EXPORT_OPTION_SINGLEPAGE}.
     *
     * @param option export option
     */
    private void exec(final int option) {
        final Thread th = new Thread(new ExportWorker(manager, option));
        th.setDaemon(true);
        th.start();
    }

    /**
     * A {@link Runnable} implementation to execute the actual export work on a background thread.
     */
    private static class ExportWorker extends Task<Void> {
        final AlertDialog dialog;
        final CollectionManager manager;
        final int option;
        String outputPath;


        public ExportWorker(final CollectionManager m, final int option) {
            manager = m;
            this.option = option;

            // setup the work indication dialog
            final GridPane container = new GridPane();
            container.setVgap(10d);
            container.setPadding(new Insets(8, 8, 8, 8)); //top right bottom left
            // make the first column of fixed size for vertical alignment
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(30d);
            columnConstraints.setHalignment(HPos.LEFT); // center the content, just a visual improvement
            container.getColumnConstraints().add(columnConstraints);
            // fill rest of the width with the second column
            final ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints2.setPercentWidth(70d);
            columnConstraints2.setHalignment(HPos.LEFT); // center the content, just a visual improvement
            container.getColumnConstraints().add(columnConstraints2);

            final ProgressIndicator progressThingy = new ProgressIndicator();
            final Label label = new Label("The universe is infinite");

            container.add(progressThingy, 0, 0);
            container.add(label, 1, 0);
            HBox.setHgrow(label, Priority.ALWAYS);

            dialog = new AlertDialog.Builder().setTitle("Exporting").setCancelable(false).setParent(SongbookApplication.getMainWindow())
                    .addContentNode(container).build();

            label.textProperty().bind(messageProperty());
        }

        @Override
        protected Void call() throws Exception {
            Platform.runLater(dialog::open);
            updateMessage("Acquiring headless browser instance...");
            try (final BrowserFactory factory = BrowserFactory.getInstance()) {
                final Browser browser = factory.getBrowserInstance();
                final Page page = browser.newPage();

                Thread.sleep(300);
                final ArrayList<Song> collection = manager.getFormalCollection();
                // The number of songs must be even
                if (collection.size() % 2 != 0) {
                    collection.add(CollectionManager.getShadowSong());
                }
                updateMessage("Generating pdf segments...");

                // the loop works differently for each of the export options
                final int segmentNumber = loop(collection, page);

                updateMessage("Merging pdf segments...");
                PDFGenerator.joinSegments(segmentNumber, outputPath);

            }

            Thread.sleep(300);
            updateMessage("Finished");
            updateProgress(1, 1);
            Thread.sleep(300);
            Platform.runLater(() -> {
                dialog.close();
                Platform.runLater(() -> {
                    dialog.close();
                    final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Export finished").setMessage("You can view the output PDF file at " + outputPath).setIcon(AlertDialog.Builder.Icon.INFO)
                            .addOkButton("Open").addCloseButton().setCancelable(true).setParent(SongbookApplication.getMainWindow()).build().awaitResult();
                    result.thenAccept(r -> {
                        if (r == AlertDialog.RESULT_OK) {
                            try {
                                Desktop.getDesktop().open(new File(outputPath));
                            } catch (final Exception ex) {
                                logger.error(ex.getMessage(), ex);
                                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                                        .setMessage(ex.getLocalizedMessage()).setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                            }
                        }
                    });
                });
            });
            return null;
        }

        private int loop(final ArrayList<Song> collection, final Page page) {
            switch (option) {
                case PDFGenerator.EXPORT_OPTION_DEFAULT -> {
                    outputPath = PDFGenerator.DEFAULT_PDF_OUTPUT_PATH;
                    return loopDefault(collection, page);
                }
                case PDFGenerator.EXPORT_OPTION_PRINTABLE -> {
                    outputPath = PDFGenerator.PRINTABLE_PDF_OUTPUT_PATH;
                    return loopPrintable(collection, page);
                }
                case PDFGenerator.EXPORT_OPTION_SINGLEPAGE -> {
                    outputPath = PDFGenerator.SINGLEPAGE_PDF_OUTPUT_PATH;
                    return loopSinglepage(collection, page);
                }
            }
            return -1;
        }

        private int loopSinglepage(final ArrayList<Song> collection, final Page page) {
            final HTMLGenerator generator = new HTMLGenerator();
            int segmentNumber = 0;
            for (final Song song : collection) {
                if (song.id() == CollectionManager.SHADOW_SONG_ID) {
                    continue;
                }
                final String HTMLSegmentFilePath = generator.generatePrintableSongFile(song, segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                // Frontpage should have zero margin so the picture is bigger and covers the whole page
                if (song.id() == CollectionManager.FRONTPAGE_SONG_ID) {
                    page.pdf(BrowserFactory.getPrintOptionsPortraitNoMargin().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));
                } else {
                    page.pdf(BrowserFactory.getPrintOptionsPortrait().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));
                }
                segmentNumber++;
            }
            return segmentNumber;
        }

        private int loopDefault(final ArrayList<Song> collection, final Page page) {
            final HTMLGenerator generator = new HTMLGenerator();
            int segmentNumber = 0;
            for (int i = 0; i < collection.size(); i += 2) {
                final String HTMLSegmentFilePath = generator.generateSegmentFile(collection.get(i), collection.get(i + 1), segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));
                segmentNumber++;
            }
            return segmentNumber;
        }
// 4 1 2 3
        private int loopPrintable(final ArrayList<Song> collection, final Page page) {
            final HTMLGenerator generator = new HTMLGenerator();
            int segmentNumber = 0;
            for (int i = 0; i < collection.size(); i += 4) {
                String HTMLSegmentFilePath = generator.generateSegmentFile(collection.get(i + 3), collection.get(i), segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                HTMLSegmentFilePath = generator.generateSegmentFile(collection.get(i + 1), collection.get(i + 2), ++segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                segmentNumber++;
            }

            final int extraSongs = collection.size() % 4;

            if (extraSongs == 1) {
                final String HTMLSegmentFilePath = generator.generateSegmentFile(CollectionManager.getShadowSong(), collection.getLast(), segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));
                segmentNumber++;
            } else if (extraSongs == 2) {
                String HTMLSegmentFilePath = generator.generateSegmentFile(CollectionManager.getShadowSong(), collection.get(collection.size() - 2), segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                HTMLSegmentFilePath = generator.generateSegmentFile(collection.getLast(), CollectionManager.getShadowSong(), ++segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                segmentNumber++;
            } else if (extraSongs == 3) {
                String HTMLSegmentFilePath = generator.generateSegmentFile(CollectionManager.getShadowSong(), collection.get(collection.size() - 3), segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                HTMLSegmentFilePath = generator.generateSegmentFile(collection.get(collection.size() - 2), collection.getLast(), ++segmentNumber);
                page.navigate(HTMLSegmentFilePath);
                page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(HTMLSegmentFilePath.replace(EXTENSION_HTML, EXTENSION_PDF))));

                segmentNumber++;
            }

            return segmentNumber;
        }
    }

    /**
     * Joins together segments of the PDF.
     *
     * @param segmentCount number of the segments
     * @param documentName final file name
     * @throws IOException
     */
    private static void joinSegments(final int segmentCount, final String documentName) throws IOException {
        final PDFMergerUtility ut = new PDFMergerUtility();
        for (int i = 0; i < segmentCount; i++) {
            ut.addSource(String.format(DEFAULT_SEGMENT_PATH, i, EXTENSION_PDF));
        }
        ut.setDestinationFileName(documentName);
        final MemoryUsageSetting settings = MemoryUsageSetting.setupMainMemoryOnly().setTempDir(new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")));
        ut.mergeDocuments(settings);
    }

}
