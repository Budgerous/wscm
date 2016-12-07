package pt.uc.cm.nfcbla;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;


class TagWriter {
    private static final String TAG = TagWriter.class.getSimpleName();

    public void writeTag(Tag tag, String text) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            ultralight.writePage(4, text.getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing with MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }

    public String readTag(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            byte[] payload = ultralight.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading with MifareUltralight...", e);
        } finally {
            if (ultralight != null) {
                try {
                    ultralight.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing MifareUltralight...", e);
                }
            }
        }
        return null;
    }
}
