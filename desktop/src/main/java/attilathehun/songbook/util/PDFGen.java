package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.vcs.IndexBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PDFGen {
    private static final Logger logger = LogManager.getLogger(PDFGen.class);

    private static final int EXPORT_OPTION_DEFAULT = 0;
    private static final int EXPORT_OPTION_PRINTABLE = 1;
    private static final int EXPORT_OPTION_SINGLEPAGE = 2;
    private static final int EXPORT_OPTION_PREVIEW = 4;

    public static Logger getLogger() {
        return logger;
    }

    static final int PREVIEW_SEGMENT_NUMBER = -1;
    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("defaultExportName")).toString();
    private static final String SINGLEPAGE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("singlepageExportName")).toString();
    private static final String PRINTABLE_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH, (String)PluginManager.getInstance().getSettings().get(Export.getInstance().getName()).get("printableExportName")).toString();
    public static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment%d%s").toString();
    public static final String PREVIEW_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/segment_preview%s").toString();
    public static final String EXTENSION_HTML = ".html";
    public static final String EXTENSION_PDF = ".pdf";

    private CollectionManager manager;

    public PDFGen() {
        this(Environment.getInstance().getCollectionManager());
    }

    public PDFGen(CollectionManager manager) {
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

    public void generateDefault() throws IOException {
        exec(createDataCollection(EXPORT_OPTION_DEFAULT), DEFAULT_PDF_OUTPUT_PATH);
    }

    public void generateSinglepage() throws IOException {
        exec(createDataCollection(EXPORT_OPTION_SINGLEPAGE), SINGLEPAGE_PDF_OUTPUT_PATH);
    }

    public void generatePrintable() throws IOException {
        exec(createDataCollection(EXPORT_OPTION_PRINTABLE), PRINTABLE_PDF_OUTPUT_PATH);
    }

    public void generatePreview(Song s) {

    }

    public void generatePreview(Song s1, Song s2) {

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

    private void exec(Collection<SegmentDataModel> data, String outputFileName) throws IOException {
        ExportWorker.performTask(data);
        for (int i = 0; i < data.size(); i++) {
            HTMLtoPDF(String.format(DEFAULT_SEGMENT_PATH, i, EXTENSION_HTML), String.format(DEFAULT_SEGMENT_PATH, i, EXTENSION_PDF));
        }
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

    private void HTMLtoPDF(String inputPath, String outputPath) {
        try {
            File inputHTML = new File(inputPath);
            Document document = Jsoup.parse(inputHTML, "UTF-8");
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            try (OutputStream os = new FileOutputStream(outputPath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withUri(inputPath);
                builder.toStream(os);
                String baseUrl = FileSystems.getDefault()
                        .getPath(new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH).toString())
                        .toUri().toURL().toString();
                builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
                builder.run();
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }



    private static class ExportWorker implements Runnable {
        private static final Logger logger = LogManager.getLogger(ExportWorker.class);

        static ConcurrentLinkedDeque<SegmentDataModel> segmentContent;
        static AtomicInteger activeThreads;

        HTMLGenerator HTMLgenerator = new HTMLGenerator();

        @Override
        public void run() {
            activeThreads.incrementAndGet();
            try {
                SegmentDataModel content;
                while (segmentContent.size() > 0) {
                    content = segmentContent.pollLast();
                    if (content.isSinglepage()) {
                        String path = HTMLgenerator.generatePrintableSongFile(content.song1(), content.number());
                    } else {
                        String path = HTMLgenerator.generateSegmentFile(content.song1(), content.song2(), content.number());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            activeThreads.decrementAndGet();
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

        public static void performTask(Collection<SegmentDataModel> contentData) {
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
