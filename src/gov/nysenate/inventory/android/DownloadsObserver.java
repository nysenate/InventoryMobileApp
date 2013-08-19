package gov.nysenate.inventory.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

public class DownloadsObserver extends FileObserver {

    public static final String LOG_TAG = DownloadsObserver.class.getSimpleName();
    File file = null;
    String path = null;
    DownloadManager downloadManager = null;
    DownloadManager.Request request = null;
    DownloadManager.Query q = new DownloadManager.Query();
    Cursor cursor = null;

    long downloadId = -1;

    private static final int flags =
            FileObserver.CLOSE_WRITE
            | FileObserver.OPEN
            | FileObserver.MODIFY
            | FileObserver.DELETE
            | FileObserver.MOVED_FROM;
    // Received three of these after the delete event while deleting a video through a separate file manager app:
    // 01-16 15:52:27.627: D/APP(4316): DownloadsObserver: onEvent(1073741856, null)

    public DownloadsObserver(String path, DownloadManager downloadManager, DownloadManager.Request request, long downloadId) {
        super(path, flags);
        this.path = path;
        this.downloadManager = downloadManager;
        this.request = request;
        q.setFilterById(downloadId);
        cursor = downloadManager.query(q);
    }

    @Override
    public void onEvent(int event, String path) {
        Log.d(LOG_TAG, "onEvent(" + event + ", " +Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+ path + ")");

        if (path == null) {
            return;
        }
        cursor.moveToFirst();
        double bytes_downloaded = cursor.getDouble(cursor
                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        double bytes_total = cursor.getDouble(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        double dl_progress = (bytes_downloaded / bytes_total) * 100;
        Log.d(LOG_TAG, "onEvent(" + event + ",  PROGESS: ("+bytes_downloaded+" out of " +bytes_total+") "+dl_progress+"%");
        
        
        switch (event) {
        case FileObserver.CLOSE_WRITE:
            // Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
            // Useful for noticing when a download has been paused. For completions, register a receiver for 
            // DownloadManager.ACTION_DOWNLOAD_COMPLETE.
            break;
        case FileObserver.OPEN:
            // Called for both read and write modes.
            // Useful for noticing a download has been started or resumed.
            break;
        case FileObserver.DELETE:
        case FileObserver.MOVED_FROM:
            // These might come in handy for obvious reasons.
            break;
        case FileObserver.MODIFY:
            // Called very frequently while a download is ongoing (~1 per ms).
            // This could be used to trigger a progress update, but that should probably be done less often than this.
            break;
        }
    }
}