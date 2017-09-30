package net.mksat.gan.keysmanager.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import net.mksat.gan.keysmanager.activities.EntryActivity;
import net.mksat.gan.keysmanager.database.DbAdapter;
import ua.edu.nuos.androidtraining2013.kms.api.APIConstants;
import ua.edu.nuos.androidtraining2013.kms.dto.CatalogContainer;
import ua.edu.nuos.androidtraining2013.kms.dto.PersonnelContainer;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static net.mksat.gan.keysmanager.connection.ServerConnection.connectionParser;
import static net.mksat.gan.keysmanager.connection.ServerConnection.outputJournalEntry;

/**
 * Created by Serega on 07.07.2014.
 */
public class KMService extends Service {

    private static final String LOG_TAG = "KMService";
    private static DbAdapter dbAdapter;
    private BindConnect binder = new BindConnect();

    private static final String HTTP_URL_CATALOG = APIConstants.REST_CLIENT_URL + APIConstants.CLIENT_CTRL_CATALOG;
    private static final String HTTP_URL_PERSONNEL_PHOTO = APIConstants.REST_CLIENT_URL + APIConstants.CLIENT_CTRL_PERSONNEL_PHOTO;
    private static final String HTTP_URL_LOG = APIConstants.REST_CLIENT_URL + APIConstants.CLIENT_CTRL_LOG;

    public static final String SAVE = "keys_manager";
    public static final String KEY_INSTRUCTION = "instruction";
    public static final String KEY_SPREADSHEET_URL = "spreadsheet_URL";

    SharedPreferences sPref;

    public DbAdapter getDbAdapter() {
        return dbAdapter;
    }

    WeakReference<EntryActivity> activity;

    public void onCreate() {
        super.onCreate();
        dbAdapter = new DbAdapter(getApplicationContext());
        Log.d(LOG_TAG, "Service onCreate");
    }

    public void updateDb(EntryActivity activity) {
        this.activity = new WeakReference<EntryActivity>(activity);
        CheckConnection conn = new CheckConnection();
        conn.execute();
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Service onBind");
        return binder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "Service onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
        Log.d(LOG_TAG, "Service onDestroy");
    }

    public class BindConnect extends Binder {
        public KMService getService() {
            return KMService.this;
        }
    }

    private class CheckConnection extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                CatalogContainer catalog = connectionParser(HTTP_URL_CATALOG);
                sPref = getSharedPreferences(SAVE, MODE_PRIVATE);
                sPref.edit().putString(KEY_INSTRUCTION, catalog.getInstructionText()).
                        putString(KEY_SPREADSHEET_URL, catalog.getSpreadsheetURL()).commit();
                dbAdapter.update(catalog);
                outputJournalEntry(HTTP_URL_LOG,dbAdapter.getListJournalEntry());
                /*BitmapUtil bitmapUtil = new BitmapUtil(KMService.this);

                //цикл с id преподавателей, получаем последовательно фото и сохраняем на карту памяти
                List<PersonnelContainer> personnelContainers = catalog.getPersonnelList();
                for (PersonnelContainer pc : personnelContainers) {
                    long id = pc.getId();
                    File tempFile = File.createTempFile("photo", null);
                    String webUrl = HTTP_URL_PERSONNEL_PHOTO;
                    URL url = new URL(webUrl + "/" + id);
                    URLConnection connection = url.openConnection();
                    if (downloadFile((HttpURLConnection) connection, tempFile)) { // если файл с таким именем есть на сервере
                        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), String.valueOf(id));
                        bitmapUtil.convertBitmap(tempFile.getAbsolutePath(), photoFile.getAbsolutePath());
                    }
                    tempFile.delete();
                }*/
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString(), e);
            }
            return null;
        }

        @Override // from AsyncTask
        protected void onPostExecute(Void result) {
            if (activity.get() != null)
                activity.get().resetResult();
            super.onPostExecute(result);
        }

        /**
         * Downloads file and store it in a file object.
         *
         * @param file File object where file should be downloaded
         */
        protected boolean downloadFile(HttpURLConnection connection, File file) {

            boolean result = false;
            FileOutputStream fos = null;
            BufferedInputStream bis = null;
            long nread = 0L;

            try {
                Log.v(LOG_TAG, String.format("downloadFile() - Used '%s' to fetch an file from REST server", connection.getURL().toString()));
                int responseCode = connection.getResponseCode();
                bis = new BufferedInputStream(connection.getInputStream()); //<проверьте здесь responsecode == 200 >
                fos = new FileOutputStream(file);
                nread = writeToFileStream(bis, fos);
                Log.v(LOG_TAG, String.format("downloadFile() - Downloaded file size %d bytes", nread));
            } catch (IOException e) {
                Log.e(LOG_TAG, "downloadFile() - Error during IO operation", e);
                // throw new NetworkErrorException(); ?????????????????????????????????????????????????????????????????
            } finally {
                closeStream(fos, "downloadFile() - Can't close destination file");
                closeStream(bis, "downloadFile() - Can't close input stream");
                if (connection != null)
                    connection.disconnect();
            }
            if (file.exists()) {
                if (nread == 0) {
                    Log.v(LOG_TAG, "downloadFile() - Erasing empty downloaded file.");
                    file.delete();
                } else {
                    result = true;
                }
            }
            return result;
        }

        protected long writeToFileStream(InputStream bis, FileOutputStream fos) {
            long nread = 0L;
            byte[] buf = new byte[1024];
            int n;
            try {
                while ((n = bis.read(buf)) > 0) {
                    fos.write(buf, 0, n);
                    nread += n;
                    if (nread > 10 * 1024 * 1024) { // 10MB
                        throw new IllegalArgumentException(); // <--- решите здесь что делать если файл слишком большлй
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return nread;
        }

        /**
         * Close Input-,Outputstream. If stream is null nothing will be done.
         *
         * @param stream  Stream to be closed
         * @param message Log message if exception occurs when closing
         */
        private void closeStream(Object stream, String message) {
            if ((stream == null) || (!(stream instanceof InputStream) && !(stream instanceof OutputStream))) {
                return;
            }
            try {
                if (stream instanceof InputStream) {
                    ((InputStream) stream).close();
                } else {
                    ((OutputStream) stream).close();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, message, e);
            }
        }
    }
}
