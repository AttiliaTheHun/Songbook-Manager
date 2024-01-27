package attilathehun.songbook.export;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.util.HTMLGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PDFGenerator {

    private static final Logger logger = LogManager.getLogger(PDFGenerator.class);

    private static final int EXPORT_OPTION_DEFAULT = 0;
    private static final int EXPORT_OPTION_PRINTABLE = 1;
    private static final int EXPORT_OPTION_SINGLEPAGE = 2;
    private static final int EXPORT_OPTION_PREVIEW = 4;

    public static Logger getLogger() {
        return logger;
    }

    public static final int PREVIEW_SEGMENT_NUMBER = -1;
    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String) PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("defaultExportName")).toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("singlepageExportName")).toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("printableExportName")).toString();
    public static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment%d%s").toString();
    public static final String PREVIEW_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment_preview%s").toString();
    public static final String EXTENSION_HTML = ".html";
    public static final String EXTENSION_PDF = ".pdf";

    private CollectionManager manager;

    public PDFGenerator() {
        this(Environment.getInstance().getCollectionManager());
    }

    public PDFGenerator(CollectionManager manager) {
        if (manager == null) {
            this.manager = Environment.getInstance().getCollectionManager().copy();
        } else {
            this.manager = manager.copy();
        }
        try {
            new File(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH).mkdirs();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("PDF Generation Error", "Cannot initialize the output folder!", "");
            throw new RuntimeException(e);
        }
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

    public String generatePreview(Song s) throws Exception {
        HTMLGenerator generator = new HTMLGenerator();
        String path = generator.generatePrintableSongFile(s, PREVIEW_SEGMENT_NUMBER);
        BrowserWrapper browser = BrowserWrapper.getInstance();
        String outputPath = path.replace(EXTENSION_HTML, EXTENSION_PDF);
        browser.print(path, outputPath);
        browser.close();
        return outputPath;
    }

    public String generatePreview(Song s1, Song s2) throws Exception {
        HTMLGenerator generator = new HTMLGenerator();
        String path = generator.generateSegmentFile(s1, s2, PREVIEW_SEGMENT_NUMBER);
        BrowserWrapper browser = BrowserWrapper.getInstance();
        String outputPath = path.replace(EXTENSION_HTML, EXTENSION_PDF);
        browser.print(path, outputPath);
        browser.close();
        return outputPath;
    }

    private Collection<SegmentDataModel> createDataCollection(int option) {
        if (option < EXPORT_OPTION_DEFAULT || option > EXPORT_OPTION_SINGLEPAGE) {
            throw  new IllegalArgumentException();
        }
        ArrayList<Song> collection = manager.getFormalCollection();
        if (collection.size() % 2 == 1) {
            collection.add(CollectionManager.getShadowSong());
        }
        Collection<SegmentDataModel> output = new ArrayList<>();
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
        return  output;
    }

    private void exec(Collection<SegmentDataModel> data, String outputFileName) throws Exception {
        ExportWorker.performTask(data);
        joinSegments(data.size(), outputFileName);
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
            ut.addSource(String.format(DEFAULT_SEGMENT_PATH, i, EXTENSION_PDF));
        }
        ut.setDestinationFileName(documentName);
        MemoryUsageSetting settings = MemoryUsageSetting.setupMainMemoryOnly().setTempDir(new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH));
        ut.mergeDocuments(settings);
    }


    private static class ExportWorker implements Runnable {
        private static final Logger logger = LogManager.getLogger(ExportWorker.class);

        static ConcurrentLinkedDeque<SegmentDataModel> segmentContent;
        static AtomicInteger activeThreads;

        HTMLGenerator HTMLgenerator = new HTMLGenerator();

        BrowserWrapper browser = BrowserWrapper.getInstance();

        private ExportWorker() throws IOException {
        }

        @Override
        public void run() {
            activeThreads.incrementAndGet();
            try {
                SegmentDataModel content;
                while (segmentContent.size() > 0) {
                    content = segmentContent.pollLast();
                    String path;
                    if (content.isSinglepage()) {
                        path = HTMLgenerator.generatePrintableSongFile(content.song1(), content.number());
                    } else {
                        path = HTMLgenerator.generateSegmentFile(content.song1(), content.song2(), content.number());
                    }
                    browser.print(path, path.replace(EXTENSION_HTML, EXTENSION_PDF));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            activeThreads.decrementAndGet();
            try {
                browser.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void init(Collection<SegmentDataModel> contentData) {
            activeThreads = new AtomicInteger(0);
            segmentContent = new ConcurrentLinkedDeque<>(contentData);
        }

        /**
         * A verification method to be called when the task execution is finished.
         */
        private static void postExecution() {
            if (segmentContent.size() > 0) {
                throw  new IllegalStateException();
            }
            if (activeThreads.intValue() != 0) {
                throw  new IllegalStateException();
            }
        }

        public static void performTask(Collection<SegmentDataModel> contentData) throws IOException {
            init(contentData);
            Thread[] threads = new Thread[((Double)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("conversionThreadCount")).intValue()];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(new ExportWorker());
                threads[i].start();
            }
            while (activeThreads.intValue() > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            postExecution();
        }
    }

    private record SegmentDataModel (int number, Song song1, Song song2) {
        public boolean isSinglepage() {
            return song2 == null;
        }
    }



}
