package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import com.google.zxing.BarcodeFormat;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.database.DbAdapter;
import net.mksat.gan.keysmanager.database.DbLoaderFragment;
import net.mksat.gan.keysmanager.service.KMService;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.BarcodeFragment;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.IScanResultHandler;
import qr_reader_logic.abhi.barcode.frag.libv2_modified.ScanResult;

import java.util.*;

public class InputDataActivity extends Activity implements IScanResultHandler, DbLoaderFragment.ReturnResult {

    public static long idPerson;

    private BarcodeFragment fragment;
    private Map<Integer, String> personnelByID;
    private List<String> personnelList;
    private Map<Integer, String> qrCodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_data_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true); // UpNavigation

        startService(new Intent(getApplicationContext(), KMService.class));
        Bundle bundle = new Bundle();
        bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_PERSONNEL);

        fragment = new BarcodeFragment();
        getFragmentManager().beginTransaction().add(R.id.sample1, fragment)
                .add(DbLoaderFragment.newInstance(bundle), "dbLoader").commit();
        fragment.setScanResultHandler(this);
        fragment.setDecodeFor(EnumSet.of(BarcodeFormat.QR_CODE));

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // всплывающая клавиатура

        Button button = (Button) findViewById(R.id.identification);
        button.setOnClickListener(new View.OnClickListener() { // слушаем нажатие кнопки
            public void onClick(View v) {
                String login = ((EditText) findViewById(R.id.person_fill_name)).getText().toString(); // читаем логин
                idPerson = 0;
                if (!login.isEmpty()) {
                    for (Map.Entry<Integer, String> person : personnelByID.entrySet()) {
                        if (login.equals(person.getValue())) {
                            idPerson = person.getKey(); // вычисляем id данного преподапателя
                            break;
                        }
                    }
                }
                if (idPerson != 0) {
                    Intent intent = new Intent(InputDataActivity.this, IdentificationActivity.class);
                    startActivity(intent); // запускаем следующую активность
                } else { // если пароль не тот или ФИО, выводим диалоговое окно
                    AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                    builder.setTitle(R.string.important_message)
                            .setMessage(R.string.invalid_password)
                            .setIcon(R.drawable.ic_launcher)
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public void scanResult(ScanResult result) {
        String qrCode = result.getRawResult().getText(); // считать и получить значение qr-code
        for (Map.Entry<Integer, String> qr : qrCodes.entrySet()) {
            if (qrCode.equals(qr.getValue())) {
                idPerson = qr.getKey(); // вычисляем id данного преподавателя
                Intent intent = new Intent(InputDataActivity.this, IdentificationActivity.class);
                startActivity(intent);
                return; // вываливаемся из метода scanResult() если считаеный qr-code совпал с одним из списка возможных
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this); // если qr-code не тот, выводим диалоговое окно
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

    @Override
    public void getReturnResult(int result, Cursor cursor) { // получить данные из БД через курсор
        personnelByID = new HashMap<Integer, String>();
        personnelList = new ArrayList<String>();
        qrCodes = new HashMap<Integer, String>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(cursor.getColumnIndex(DbAdapter._ID));
            personnelByID.put(id, cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME)));
            qrCodes.put(id, cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CODE)));
        }
        for (Map.Entry<Integer, String> person : personnelByID.entrySet()) { // получаем список преподов из коллекции
            personnelList.add(person.getValue());
        }
        ArrayAdapter<String> adapter = // выводим список преподавателей из БД на экран
                new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, personnelList);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) findViewById(R.id.person_fill_name);
        autoComplete.setAdapter(adapter);
    }
}