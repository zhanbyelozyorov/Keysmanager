package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.database.DbAdapter;
import net.mksat.gan.keysmanager.database.DbLoaderFragment;
import net.mksat.gan.keysmanager.service.KMService;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.BarcodeFragment;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.IScanResultHandler;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.ScanResult;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class EntryActivity extends Activity implements IScanResultHandler, DbLoaderFragment.ReturnResult {

    public static long idJanitor;

    private BarcodeFragment fragment;
    private DbLoaderFragment dbFragment;
    private Map<Long, String> qrCodes;
    private ServiceConnection connection;
    private static KMService service;
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_activity);
        Button buttonUpdate = (Button) findViewById(R.id.db_update);
        buttonUpdate.setOnClickListener(clickUpdate);
        startService(new Intent(getApplicationContext(), KMService.class));
        Bundle bundle = new Bundle();
        bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_JANITOR);

        fragment = new BarcodeFragment();
        dbFragment = DbLoaderFragment.newInstance(bundle);
        getFragmentManager().beginTransaction().add(R.id.sample, fragment)
                .add(dbFragment, "dbLoader").commit();
        fragment.setScanResultHandler(this);
        fragment.setDecodeFor(EnumSet.of(BarcodeFormat.QR_CODE));

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // нормально свернутая клавиатура
    }


    @Override
    public void onDestroy() {
        if (connection != null)
            unbindService(connection);
        super.onDestroy();
    }

    @Override
    public void scanResult(ScanResult result) { // сканируем qr-code
        String qrCode = result.getRawResult().getText(); // считать и получить значение qr-code
        for (Map.Entry<Long, String> qr : qrCodes.entrySet()) {
            if (qrCode.equals(qr.getValue())) {
                idJanitor = qr.getKey(); // вычисляем id данного вахтера
                Intent intent = new Intent(EntryActivity.this, MainPageActivity.class);
                startActivity(intent);
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this); // если qr-code не тот, выводим диалоговое окно
        builder.setTitle(R.string.important_message)
                .setMessage(R.string.invalid_QR_code)
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                fragment.restart();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void resetResult() {
        Bundle bundle = new Bundle();
        bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_JANITOR);
        dbFragment.resetQuery(bundle);
        progress.cancel();
    }

    @Override
    public void getReturnResult(int result, Cursor cursor) { // получить данные из БД через курсор
        qrCodes = new HashMap<Long, String>();
        while (cursor.moveToNext()) {
            Long id = cursor.getLong(cursor.getColumnIndex(DbAdapter._ID));
            qrCodes.put(id, cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CODE)));
        }
    }

    private View.OnClickListener clickUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = ((KMService.BindConnect) binder).getService();
                    service.updateDb(EntryActivity.this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            progress = new ProgressDialog(EntryActivity.this);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage(getString(R.string.wait_please));
            progress.setIndeterminate(true); // выдать значек ожидания
            progress.setCancelable(false);
            progress.show();
            Intent intent = new Intent(EntryActivity.this, KMService.class);
            bindService(intent, connection, 0);
        }
    };
}

