package attilathehun.songbook.util;

import java.nio.file.Paths;
import java.io.File;

@Deprecated(forRemoval = true)
public class ZipTest {

    private static String OUTPUT_PATH = Paths.get(System.getProperty("user.dir") + File.separator + "test_ground"  + File.separator + "out" + File.separator + "xqz.zip").toString();
    private static String INPUT_PATH = Paths.get(System.getProperty("user.dir") + File.separator + "test_ground"  + "/zip_out/").toString();

    public static void main() {
        try {
            ZipBuilder.extract(OUTPUT_PATH, INPUT_PATH);

            System.out.println("Builder finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}
