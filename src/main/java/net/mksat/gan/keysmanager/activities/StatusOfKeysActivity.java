package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.GridView;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.database.DbAdapter;
import net.mksat.gan.keysmanager.database.DbLoaderFragment;
import net.mksat.gan.keysmanager.service.AuditoryButton;
import net.mksat.gan.keysmanager.service.KMService;
import net.mksat.gan.keysmanager.service.KeyButtonAdapter;

import java.util.ArrayList;
import java.util.List;

public class StatusOfKeysActivity extends Activity implements DbLoaderFragment.ReturnResult {

    private List<AuditoryButton> auditoryButtonList;
    private GridView grid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_of_keys_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        startService(new Intent(getApplicationContext(), KMService.class)); // срарт сервиса ключей
        Bundle bundle = new Bundle();
        bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.GET_AUDITORIUM); // запрос на курсор из таблици аудиторий
        getFragmentManager().beginTransaction().add(DbLoaderFragment.newInstance(bundle), "dbLoader").commit();

        grid = (GridView) findViewById(R.id.gridViewOfStatusKeys);
    }

    @Override
    public void getReturnResult(int result, Cursor cursor) { // получить данные из БД через курсор
        auditoryButtonList = new ArrayList<AuditoryButton>();
        while (cursor.moveToNext()) {
            AuditoryButton auditoryButton = new AuditoryButton();// получить из БД
            auditoryButton.setStatus(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_STATUS))); // статус аудитории
            auditoryButton.setAuditoryName(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME))); // номер
            auditoryButton.setCampus(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CAMPUS))); // и корпус
            auditoryButtonList.add(auditoryButton);
        }
        KeyButtonAdapter adp = new KeyButtonAdapter(this, auditoryButtonList);// помещаем каждый элемент auditoryButtonList-а в кнопку
        grid.setAdapter(adp); // перадаем список кнопок (auditoryButtonList) доступных преподователю ключей для вывода в активность
    }
}