package attilathehun.songbook.export;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.util.HTMLGenerator;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.controlsfx.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PDFGenerator {
    private static final Logger logger = LogManager.getLogger(PDFGenerator.class);
    public static final int PREVIEW_SEGMENT_NUMBER = -1;
    public static final String DEFAULT_SEGMENT_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/segment%d%s").toString();
    public static final String PREVIEW_SEGMENT_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/segment_preview%s").toString();
    public static final String EXTENSION_HTML = ".html";
    public static final String EXTENSION_PDF = ".pdf";

    private static final int EXPORT_OPTION_DEFAULT = 0;
    private static final int EXPORT_OPTION_PRINTABLE = 1;
    private static final int EXPORT_OPTION_SINGLEPAGE = 2;
    private static final int EXPORT_OPTION_PREVIEW = 4;
    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_DEFAULT_FILE_NAME")).toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_SINGLEPAGE_FILE_NAME")).toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(SettingsManager.getInstance().getValue("EXPORT_FILE_PATH"), (String) SettingsManager.getInstance().getValue("EXPORT_PRINTABLE_FILE_NAME")).toString();
    private final CollectionManager manager;
    private ProgressAggregator progressDialogtask;

    public PDFGenerator() {
        this(Environment.getInstance().getCollectionManager());
    }

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

    public static Logger getLogger() {
        return logger;
    }

    public void generateDefault() throws Exception {
        exec(createDataCollection(EXPORT_OPTION_DEFAULT), DEFAULT_PDF_OUTPUT_PATH);
    }

    public void generateSinglepage() throws Exception {
        exec(createDataCollection(EXPORT_OPTION_SINGLEPAGE), SINGLEPAGE_PDF_OUTPUT_PATH);
    }

    public void generatePrintable() throws Exception {
        exec(createDataCollection(EXPORT_OPTION_PRINTABLE), PRINTABLE_PDF_OUTPUT_PATH);
    }

    public String generatePreview(final Song s) throws Exception {
        final HTMLGenerator generator = new HTMLGenerator();
        final String path = generator.generatePrintableSongFile(s, PREVIEW_SEGMENT_NUMBER);
        final String outputPath = path.replace(EXTENSION_HTML, EXTENSION_PDF);
        final Browser browser = BrowserFactory.getDefaultBrowserInstance();
        final Page page = browser.newPage();
        page.navigate(path);
        page.pdf(BrowserFactory.getPrintOptionsPortrait().setPath(Paths.get(outputPath.replace(EXTENSION_HTML, EXTENSION_PDF))));
        return outputPath;
    }

    public String generatePreview(final Song s1, final Song s2) throws Exception {
        final HTMLGenerator generator = new HTMLGenerator();
        final String path = generator.generateSegmentFile(s1, s2, PREVIEW_SEGMENT_NUMBER);
        final Browser browser = BrowserFactory.getDefaultBrowserInstance();
        final String outputPath = path.replace(EXTENSION_HTML, EXTENSION_PDF);
        final Page page = browser.newPage();
        page.navigate(path);
        page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(outputPath.replace(EXTENSION_HTML, EXTENSION_PDF))));
        return outputPath;
    }

    private Collection<SegmentDataModel> createDataCollection(final int option) {
        if (option < EXPORT_OPTION_DEFAULT || option > EXPORT_OPTION_SINGLEPAGE) {
            throw new IllegalArgumentException();
        }
        final ArrayList<Song> collection = manager.getFormalCollection();
        if (collection.size() % 2 == 1) {
            collection.add(CollectionManager.getShadowSong());
        }
        final Collection<SegmentDataModel> output = new ArrayList<>();
        int segmentNumber = 0;
        if (option == EXPORT_OPTION_DEFAULT) {
            for (int i = 0; i < collection.size(); i += 2) {
                output.add(new SegmentDataModel(segmentNumber++, collection.get(i), collection.get(i + 1)));
            }
        } else if (option == EXPORT_OPTION_PRINTABLE) {
            for (int i = 0; i < collection.size(); i += 4) {
                if (i + 4 > collection.size()) {
                    break;
                }
                output.add(new SegmentDataModel(segmentNumber++, collection.get(i), collection.get(i + 3)));
                output.add(new SegmentDataModel(segmentNumber++, collection.get(i + 2), collection.get(i + 1)));
            }
        } else if (option == EXPORT_OPTION_SINGLEPAGE) {
            for (int i = 0; i < collection.size(); i++) {
                output.add(new SegmentDataModel(i, collection.get(i), null));
            }
        }
        return output;
    }

    private void exec(final Collection<SegmentDataModel> data, final String outputFileName) throws Exception {
        progressDialogtask = new ProgressAggregator(this, data, outputFileName);
        final ProgressDialog dialog = new ProgressDialog(progressDialogtask);
        dialog.contentTextProperty().bind(progressDialogtask.messageProperty());
        progressDialogtask.call();
        dialog.showAndWait();
        progressDialogtask = null;
    }

    private class ProgressAggregator extends Task<Object> {
        private final PDFGenerator generator;
        private final Collection<SegmentDataModel> data;
        private final String outputFileName;

        public ProgressAggregator(final PDFGenerator g, final Collection<SegmentDataModel> d, final String outputFileName) {
            generator = g;
            data = d;
            this.outputFileName = outputFileName;
        }

        @Override
        protected Object call() throws Exception {
            updateTitle("Exporting");
            ExportWorker.performTask(data, generator);
            updateMessage("Merging PDF pages...");
            updateProgress(getProgress() + 5, getTotalWork());
            joinSegments(data.size(), outputFileName);
            while (!this.isCancelled()) {
                Thread.onSpinWait();
            }
            return null;
        }

        public synchronized void updateProgress(final double value, final double maxValue, final String message) {
            updateProgress(value, maxValue);
            updateMessage(message);
        }
    }

    /**
     * Joins together segments of the PDF.
     *
     * @param segmentCount number of the segments
     * @param documentName final file name
     * @throws IOException
     */
    private void joinSegments(final int segmentCount, final String documentName) throws IOException {
        final PDFMergerUtility ut = new PDFMergerUtility();
        for (int i = 0; i < segmentCount; i++) {
            ut.addSource(String.format(DEFAULT_SEGMENT_PATH, i, EXTENSION_PDF));
        }
        ut.setDestinationFileName(documentName);
        final MemoryUsageSetting settings = MemoryUsageSetting.setupMainMemoryOnly().setTempDir(new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")));
        ut.mergeDocuments(settings);
    }


    private static class ExportWorker implements Runnable {
        private static final Logger logger = LogManager.getLogger(ExportWorker.class);
        static ConcurrentLinkedDeque<SegmentDataModel> segmentContent;
        static AtomicInteger activeThreads;
        private static PDFGenerator PDFgenerator;
        final HTMLGenerator HTMLgenerator = new HTMLGenerator();
        private final Playwright playwright = BrowserFactory.getPlaywright();

        private final Browser browser = BrowserFactory.getInstance().getBrowserInstance(playwright);

        private ExportWorker() {
        }

        private static void init (final Collection<SegmentDataModel> contentData, final PDFGenerator g) {
            activeThreads = new AtomicInteger(0);
            segmentContent = new ConcurrentLinkedDeque<>(contentData);
            PDFgenerator = g;
        }

        /**
         * A verification method to be called when the task execution is finished.
         */
        private static void postExecution() {
            PDFgenerator.progressDialogtask.cancel();
            PDFgenerator = null;
            if (segmentContent.size() > 0) {
                throw new IllegalStateException("Export work data is not empty");
            }
            if (activeThreads.intValue() > 0) {
                System.out.println("active threads: " + activeThreads);
                throw new IllegalStateException("Some threads were not finished");
            }
        }

        public static void performTask(final Collection<SegmentDataModel> contentData, final PDFGenerator g) {
            init(contentData, g);
            final Thread[] threads = new Thread[(Integer) SettingsManager.getInstance().getValue("EXPORT_THREAD_COUNT")];
            g.progressDialogtask.updateProgress(0, contentData.size() + 10, "Converting songbook pages to PDF...");
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(new ExportWorker());
                threads[i].start();
            }
            while (activeThreads.intValue() > 0) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            //postExecution();
        }

        @Override
        public void run() {
            activeThreads.incrementAndGet();
            try {
                SegmentDataModel content;
                final Page page = browser.newPage();
                while (segmentContent.size() > 0) {
                    content = segmentContent.pollLast();
                    String path;
                    if (content.isSinglepage()) {
                        path = HTMLgenerator.generatePrintableSongFile(content.song1(), content.number());
                    } else {
                        path = HTMLgenerator.generateSegmentFile(content.song1(), content.song2(), content.number());
                    }
                    logger.debug("segment path is: " + path);
                    page.navigate(path);
                    page.pdf(BrowserFactory.getPrintOptionsLandscape().setPath(Paths.get(path.replace(EXTENSION_HTML, EXTENSION_PDF))));
                    ExportWorker.PDFgenerator.progressDialogtask.updateProgress(ExportWorker.PDFgenerator.progressDialogtask.getProgress() + 1, ExportWorker.PDFgenerator.progressDialogtask.getTotalWork(), ExportWorker.PDFgenerator.progressDialogtask.getMessage());
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
            activeThreads.decrementAndGet();
            browser.close();
            playwright.close();
        }
    }

    private record SegmentDataModel(int number, Song song1, Song song2) {
        public boolean isSinglepage() {
            return song2 == null;
        }
    }


}
