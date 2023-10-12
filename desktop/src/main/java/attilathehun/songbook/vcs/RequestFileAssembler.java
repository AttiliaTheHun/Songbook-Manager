package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private String outputFilePath = null;

    public RequestFileAssembler assembleSaveFile(SaveIndex index) {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        outputFilePath = Environment.getInstance().settings.vcs.REQUEST_ZIP_FILE_PATH;
        for (String s : index.getAdditions().get("standard")) {
            Files.copy(Paths.get(Environment.getInstance().settings.environment.SONG_DATA_FILE_PATH, s), Paths.get(Environment.getInstance().settings.vcs.REQUEST_TEMP_FILE_PATH, s), StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
