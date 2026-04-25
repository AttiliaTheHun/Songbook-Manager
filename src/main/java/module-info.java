module SongbookManager {
    requires java.base;
    requires java.logging;
    requires javafx.base;
    requires javafx.web;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires com.github.kwhat.jnativehook;
    requires playwright;
    requires org.controlsfx.controls;
    requires com.google.gson;
    requires org.jsoup;
    requires java.compiler;
    requires org.apache.pdfbox;
    requires eu.mihosoft.monacofx;
    opens attilathehun.songbook.window to javafx.graphics, javafx.fxml;
    opens attilathehun.songbook.environment to com.google.gson;
    opens attilathehun.songbook.collection to com.google.gson;
    exports attilathehun.songbook.collection;
}