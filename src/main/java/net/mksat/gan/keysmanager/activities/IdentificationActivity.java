package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.database.DbAdapter;
import net.mksat.gan.keysmanager.database.DbLoaderFragment;
import net.mksat.gan.keysmanager.service.AuditoryButton;
import net.mksat.gan.keysmanager.service.KMService;
import net.mksat.gan.keysmanager.service.KeyButtonAdapter;
import net.mksat.gan.keysmanager.service.NextClickListener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentificationActivity extends Activity implements DbLoaderFragment.ReturnResult {

    private static final String LOG_TAG = "KeysManagerService";
    private List<AuditoryButton> auditoryButtonList;
    private GridView grid; // отображение списка доступных ключей (в виде кнопок)
    private Map<Integer, String> personnelByID;
    TextView personName;
    ImageView photo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identification_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true); // для UpNavigation - возвращает в родительскую активность
        // которая определяется в AndroidManifest
        startService(new Intent(getApplicationContext(), KMService.class)); // срарт сервиса ключей
        Bundle bundle = new Bundle();
        bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_PERSONNEL); // запрос на курсор из таблици преподавателей
        getFragmentManager().beginTransaction().add(DbLoaderFragment.newInstance(bundle), "dbLoader").commit();

        Bundle bundle1 = new Bundle();
        bundle1.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_PERMISSION); // запрс на курсор из таблици разрешений
        bundle1.putLong(DbLoaderFragment.ID_PERSONNEL, InputDataActivity.idPerson);
        getFragmentManager().beginTransaction().add(DbLoaderFragment.newInstance(bundle1), "dbLoader").commit();

        personName = (TextView) findViewById(R.id.person_name_text);
        photo = (ImageView) findViewById(R.id.photo);
        grid = (GridView) findViewById(R.id.gridViewOfIdentification); // связываем View для вывода списка доступных ключей с активностью

        Button button = (Button) findViewById(R.id.invalid_photo_button);
        button.setOnClickListener(new NextClickListener(this, InvalidPersonInstructionActivity.class, null));
    }

    @Override
    public void getReturnResult(int result, Cursor cursor) { // получить данные из БД через курсор
        if (result == DbLoaderFragment.GET_PERSONNEL) {
            personnelByID = new HashMap<Integer, String>();
            while (cursor.moveToNext()) {
                Integer id = cursor.getInt(cursor.getColumnIndex(DbAdapter._ID));
                personnelByID.put(id, cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME)));
            }  // по id получаем из БД ФИО фото, и выводим на экран
            for (Map.Entry<Integer, String> person : personnelByID.entrySet()) {
                if (InputDataActivity.idPerson == person.getKey()) { // получить ФИО через id Person
                    personName.setText(person.getValue().toString()); // выводим ФИО на экран

                    File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            String.valueOf(InputDataActivity.idPerson));
                    if (photoFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        photo.setImageBitmap(myBitmap);
                    } else {
                        photo.setImageDrawable(getResources().getDrawable(R.drawable.no_photo)); // берем шаблон "фото нет" из ресурсов
                    }
                    break;
                }
            }
        }
        if (result == DbLoaderFragment.GET_PERMISSION) {// по id получаем из БД список доступных ключей
            auditoryButtonList = new ArrayList<AuditoryButton>();
            while (cursor.moveToNext()) {
                AuditoryButton auditoryButton = new AuditoryButton();
                auditoryButton.setId(cursor.getString(cursor.getColumnIndex(DbAdapter._ID))); // получить из БД id,
                auditoryButton.setStatus(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_STATUS))); // статус аудитории
                auditoryButton.setAuditoryName(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME))); // номер
                auditoryButton.setCampus(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CAMPUS))); // корпус
                auditoryButton.setSecurityAlarm(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_SECURITY_ALARM))); // наличие сигнализации
                auditoryButtonList.add(auditoryButton);
            }
            KeyButtonAdapter adp = new KeyButtonAdapter(this, auditoryButtonList);// перадаем список auditoryButtonList доступных преподователю
            grid.setAdapter(adp); // ключей для вывода в активность
        }
    }

    // читать фотографии с cdCard (каталог Photo) по id преподавателя как имени фотографии
    void readFileSD(String fileName) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + "Photo");
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, fileName);
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                Log.d(LOG_TAG, str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}