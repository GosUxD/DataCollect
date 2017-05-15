package datacollect.ryan.grant.com.datacollect.storage;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Daniel on 4/22/2017.
 */

public class FileHelper {

    private Context mContext;
    private SingleMediaScanner mediaScanner;


    public FileHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void writeToFile(String upc, String price, String location) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = SharedPrefHelper.getInstance(mContext).getCurrentUser().replaceAll("\\s+", "");
        File file = new File(path, fileName + ".csv");

        CSVWriter writer;
        // File exist
        if (file.exists() && !file.isDirectory()) {
            FileWriter mFileWriter = new FileWriter(file, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(file));
        }
        String[] data = {upc, price, location};

        writer.writeNext(data);

        writer.close();

        mediaScanner = new SingleMediaScanner(mContext, path.getAbsolutePath(), file);

    }

    private class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
        private MediaScannerConnection mMs;
        private String path;

        SingleMediaScanner(Context context, String f, File mFile) {
            path = f;
            mMs = new MediaScannerConnection(context, this);
            mMs.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            mMs.scanFile(path, null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mMs.disconnect();
        }

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}

