package edu.cuc.ccc.plugins.clipboardplugin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/*
 *  从Android10开始，只有获得焦点的应用才能访问剪贴板。
 */

public class ClipboardHelper {
    public static String getClipboardContent(Context ctx) {
        ClipboardManager cbm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cbm != null) {
            if (cbm.hasPrimaryClip() && cbm.getPrimaryClip().getItemCount() > 0) {
                // item类型为uri的分支尚未作处理
                String ret = cbm.getPrimaryClip().getItemAt(0).getText().toString();
                if (!ret.isEmpty()) {
                    return ret;
                }
            }
        }
        return "";
    }

    public static void putClipboardContent(Context ctx, String content) {
        if (content.isEmpty()) return;
        ClipboardManager cbm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cbm == null) return;
        ClipData item = ClipData.newPlainText(null, content);
        cbm.setPrimaryClip(item);
    }
}
