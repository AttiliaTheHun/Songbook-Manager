package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.plugin.Export;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportTask extends Task<String> {
    private static final Logger logger = LogManager.getLogger(ExportTask.class);
    private ConcurrentLinkedDeque<Integer> tasks;
    final PDFGenerator generator;
    private int exportOption;
    private String exportFilePath;
    private int segmentCount = 0;
    private double progress;

    private Song songOne;
    private Song songTwo;

    ExecutorService executor;

    public ExportTask(PDFGenerator generator, int exportOption) {
        if (exportOption < PDFGenerator.EXPORT_OPTION_DEFAULT || exportOption >= PDFGenerator.EXPORT_OPTION_PREVIEW || generator == null) {
            throw new IllegalArgumentException();
        }
        this.generator = generator;
        this.exportOption = exportOption;
        this.progress = 0d;
        this.executor = Executors.newFixedThreadPool(Integer.parseInt((String) Environment.getInstance().settings.plugins.get(Export.getInstance().getName()).get("conversionThreadCount")));
    }

    public ExportTask(PDFGenerator generator, Song songOne) {
        if (generator == null || songOne == null || songOne.id() < 0) {
            throw new IllegalArgumentException();
        }
        this.generator = generator;
        this.progress = 0d;
        this.executor = Executors.newFixedThreadPool(Integer.parseInt((String) Environment.getInstance().settings.plugins.get(Export.getInstance().getName()).get("conversionThreadCount")));
        this.songOne = songOne;
    }

    public ExportTask(PDFGenerator generator, Song songOne, Song songTwo) {
        if (generator == null || songOne == null || songOne.id() < 0 || songTwo == null || songTwo.id() < 0) {
            throw new IllegalArgumentException();
        }
        this.generator = generator;
        this.progress = 0d;
        this.executor = Executors.newFixedThreadPool(Integer.parseInt((String) Environment.getInstance().settings.plugins.get(Export.getInstance().getName()).get("conversionThreadCount")));
        this.songOne = songOne;
        this.songTwo = songTwo;
    }

    @Override
    protected String call() throws Exception {
        while (!isCancelled()) {
            try {
                switch (exportOption) {
                    case PDFGenerator.EXPORT_OPTION_DEFAULT -> exportDefault();
                    case PDFGenerator.EXPORT_OPTION_PRINTABLE -> exportPritable();
                    case PDFGenerator.EXPORT_OPTION_SINGLEPAGE -> exportSinglepage();
                    case PDFGenerator.EXPORT_OPTION_PREVIEW -> exportPreview();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
            break;
        }

        executor.shutdown();
        return exportFilePath;
    }

    private void exportPreview() {
        exportFilePath = String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_PDF);

        updateTitle("Generating preview...");
        updateMessage("Applying song template.....0%");
        updateProgress(0d, 1d);
        String segmentFilePath;
        if (songTwo == null) {
           segmentFilePath = new HTMLGenerator().generatePrintableSongFile(songOne, PDFGenerator.PREVIEW_SEGMENT_NUMBER, generator.getManager());
        } else {
            segmentFilePath = new HTMLGenerator().generateSegmentFile(songOne, songTwo, PDFGenerator.PREVIEW_SEGMENT_NUMBER, generator.getManager());
        }

        updateValue(segmentFilePath);
        try {

            updateMessage("Converting HTML to PDF.....50%");
            updateProgress(0.50d, 1d);

            HTMLtoPDF(segmentFilePath, exportFilePath);

            updateMessage("Finished.....100%");
            updateProgress(1d, 1d);


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Error when creating the preview");
            cancel();
        }

    }

    private void exportDefault() {
        exportFilePath = PDFGenerator.DEFAULT_PDF_OUTPUT_PATH;

        updateTitle("Exporting...");
        updateMessage("Loading the collection.....0%");
        final ArrayList<Song> collection = generator.getManager().getFormalCollection();


    }

    private void exportSinglepage() {
        exportFilePath = PDFGenerator.SINGLEPAGE_PDF_OUTPUT_PATH;
    }

    private void exportPritable() {
        exportFilePath = PDFGenerator.PRINTABLE_PDF_OUTPUT_PATH;
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
                        .getPath(new File(Environment.getInstance().settings.environment.RESOURCE_FILE_PATH).toString())
                        .toUri().toURL().toString();
                builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
                builder.run();
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            cancel();
        }
    }

}
