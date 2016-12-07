package pt.uc.cm.nfcbla;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    NfcAdapter myAdapter;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        myAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        myAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        final Tag someTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "Found a new tag... " + someTag);

        Context context = getApplicationContext();
        CharSequence text = "Found a new tag!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Button readButton = (Button) findViewById(R.id.readButton);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ReaderTask().execute(someTag);
            }
        });

        Button writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText writeText = (EditText) findViewById(R.id.writeText);
                String text = writeText.getText().toString();
                new WriterTask().execute(new WriterTaskLoad(someTag, text));
            }
        });
    }

    private class ReaderTask extends AsyncTask<Tag, Void, String> {
        private final String TAG = ReaderTask.class.getSimpleName();

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Log.d(TAG, "NDEF is null");
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            if (ndefMessage == null) {
                Log.d(TAG, "NDEF message is null");
                return null;
            }

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    return new String(ndefRecord.getPayload());
                }
            }
            Log.d(TAG, "No records");
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_SHORT;

            if (result != null) {
                TextView readText = (TextView) findViewById(R.id.readText);
                readText.setText(result);

                text = "Tag successfully read!";
            } else {
                text = "Empty tag!";
            }

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private class WriterTask extends AsyncTask<WriterTaskLoad, Void, Boolean> {
        private final String TAG = WriterTask.class.getSimpleName();

        @Override
        protected Boolean doInBackground(WriterTaskLoad... params) {
            WriterTaskLoad load = params[0];
            Tag tag = load.getTag();
            String string = load.getString();

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Log.d(TAG, "NDEF is null");
                return false;
            }

            byte[] payload = string.getBytes();

            NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
            NdefRecord[] records = {ndefRecord};

            NdefMessage ndefMessage = new NdefMessage(records);

            try {
                ndef.connect();
            } catch (IOException e) {
                Log.e(TAG, "Can't connect to NDEF", e);
                return false;
            }
            try {
                ndef.writeNdefMessage(ndefMessage);
            } catch (IOException | FormatException e) {
                Log.e(TAG, "Can't write to NDEF tag", e);
                return false;
            }
            try {
                ndef.close();
            } catch (IOException e) {
                Log.e(TAG, "Can't close connection to NDEF", e);
                return false;
            }
            return true;
        }

        @Override
        public void onPostExecute(Boolean result) {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_SHORT;

            if (result == true) {
                text = "Tag successfully written!";
            } else {
                text = "Error writing tag!";
            }

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private class WriterTaskLoad {
        private Tag tag;
        private String string;

        public WriterTaskLoad(Tag tag, String string) {
            this.tag = tag;
            this.string = string;
        }

        public WriterTaskLoad() {
        }

        public Tag getTag() {
            return tag;
        }

        public void setTag(Tag tag) {
            this.tag = tag;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}

