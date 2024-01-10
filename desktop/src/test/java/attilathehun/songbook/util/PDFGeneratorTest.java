package attilathehun.songbook.util;

import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.plugin.Plugin;
import attilathehun.songbook.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PDFGeneratorTest {


    @Test
    void test() throws Exception {
        PluginManager.loadPlugins();
        Export.getInstance();
        PDFGen gen = new PDFGen();
        gen.generateDefault();
    }
}