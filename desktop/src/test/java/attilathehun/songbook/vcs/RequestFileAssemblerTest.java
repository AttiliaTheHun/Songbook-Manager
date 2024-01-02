package attilathehun.songbook.vcs;

import attilathehun.songbook.vcs.index.Index;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class RequestFileAssemblerTest {

    @Test
    void yourWarranty() throws IOException, NoSuchAlgorithmException {
        Index local = CacheManager.getInstance().getCachedIndex();
        Index remote = Index.empty();
        RequestFileAssembler RFAssembler = new RequestFileAssembler().assembleSaveFile(new IndexBuilder().createSaveIndex(local, remote), IndexBuilder.compareCollections(local, remote));
    }

}