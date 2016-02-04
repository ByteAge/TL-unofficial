package chitchat.fragments;

import org.telegram.tgnet.TLRPC;

/**
 * Created by RaminBT on 16/01/2016.
 */
public class StickersActivity extends org.telegram.ui.StickersActivity {


    @Override
    public boolean optionsVisible() {
        return false;
    }

    @Override
    public void itemClicked(TLRPC.TL_messages_stickerSet stickerSet) {
        presentFragment(new StickerSetActivity(stickerSet));
        finishFragment();
    }
}
