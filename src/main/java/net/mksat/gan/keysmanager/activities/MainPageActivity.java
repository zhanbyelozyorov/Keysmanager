package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.service.KMService;
import net.mksat.gan.keysmanager.service.NextClickListener;

public class MainPageActivity extends Activity {

    public static boolean keyAvailable;
    public static boolean keyIssued;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_activity);

        keyAvailable = false;
        keyIssued = false;

        Button button1 = (Button) findViewById(R.id.status_of_keys);
        button1.setOnClickListener(new NextClickListener(this, StatusOfKeysActivity.class, null));

        Button button2 = (Button) findViewById(R.id.give_key_button);// поменять статус ключа в БД на "выдан"
        button2.setOnClickListener(new View.OnClickListener() { // слушаем нажатие кнопки
            public void onClick(View v) {
                keyAvailable = true;
                Intent intent = new Intent(MainPageActivity.this, InputDataActivity.class);
                startActivity(intent);
            }
        });

        Button button3 = (Button) findViewById(R.id.get_key_button);
        button3.setOnClickListener(new View.OnClickListener() { // слушаем нажатие кнопки
            public void onClick(View v) {
                keyIssued = true;
                Intent intent = new Intent(MainPageActivity.this, InputDataActivity.class);
                startActivity(intent);
            }
        });

        Button button4 = (Button) findViewById(R.id.guard_change);
        button4.setOnClickListener(new NextClickListener(this, EntryActivity.class, null));

        Button button5 = (Button) findViewById(R.id.show_journal);// просмотр журнала
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences(KMService.SAVE, MODE_PRIVATE);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(preferences.getString(KMService.KEY_SPREADSHEET_URL, null)));
                startActivity(intent);
            }
        });
    }
}