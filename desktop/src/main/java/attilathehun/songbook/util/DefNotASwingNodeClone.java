package attilathehun.songbook.util;

import javafx.embed.swing.SwingNode;

public class DefNotASwingNodeClone extends SwingNode {

    @Override
    public boolean isResizable() {
        return false;
    }

}
