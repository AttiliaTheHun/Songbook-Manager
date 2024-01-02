package attilathehun.songbook.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ZipBuilderTest {

    @Test
    void ZipBuilder() throws IOException {
        //ZipBuilder.extract("test.zip");

        File target = new File("zip_test");
        String outputPath = "test.zip";
        var builder = new ZipBuilder();

        builder.setOutputPath("test.zip").addFile(new File("log.txt"), "data");
        builder.close();
    }


}