/* package attilathehun.songbook.util;

import attilathehun.songbook.environment.Environment;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

@Deprecated(forRemoval = true)
public class ExportTest {
    private static String OUTPUT_PATH = Paths.get(System.getProperty("user.dir") + File.separator + "test_ground" + File.separator + "out" + File.separator + "out.pdf").toString();
    private static String INPUT_PATH = Paths.get(System.getProperty("user.dir") + File.separator + "test_ground"  + File.separator + "exp" + File.separator+ "templates" + File.separator  + "xs.html").toString();

    private static final String DEFAULT_PDF_OUTPUT_PATH = Paths.get(Environment.getInstance().settings.environment.OUTPUT_FILE_PATH + "/DefaultExport.pdf").toString();
    public static void main() {
        try {
            File inputHTML = new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/current_page.html");
            Document document = Jsoup.parse(inputHTML, "UTF-8");
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            try (OutputStream os = new FileOutputStream(DEFAULT_PDF_OUTPUT_PATH)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withUri(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/current_page.html");
                builder.toStream(os);
                String baseUrl = FileSystems.getDefault()
                        .getPath(new File(Environment.getInstance().settings.environment.RESOURCE_FILE_PATH).toString())
                        .toUri().toURL().toString();
                System.out.println("baseURL: " + baseUrl);
                builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
                builder.run();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done");
        System.exit(0);
    }

}
*/