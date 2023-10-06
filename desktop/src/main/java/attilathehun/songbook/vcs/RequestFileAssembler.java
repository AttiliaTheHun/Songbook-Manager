package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;

public class RequestFileAssembler {
    private String outputFilePath = null;

    public RequestFileAssembler assembleSaveFile(SaveIndex index) {
        outputFilePath = Environment.getInstance().settings.vcs.REQUEST_ZIP_FILE_PATH;
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
