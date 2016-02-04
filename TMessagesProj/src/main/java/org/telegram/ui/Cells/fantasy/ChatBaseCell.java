package org.telegram.ui.Cells.fantasy;

import android.content.Context;

import chitchat.ColorUtils;

/**
 * Created by RaminBT on 14/01/2016.
 */
public class ChatBaseCell extends org.telegram.ui.Cells.ChatBaseCell {

    public ChatBaseCell(Context context) {
        super(context);
    }


    @Override
    public ColorUtils.ColorPresenter getColorPresenter() {
        return ColorUtils.fantasyColorPresenter;
    }
}
