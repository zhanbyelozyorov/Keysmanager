package net.mksat.gan.keysmanager.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.service.KMService;
import net.mksat.gan.keysmanager.service.NextClickListener;

public class InvalidPersonInstructionActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invalid_person_instruction_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        WebView instruction = (WebView) findViewById(R.id.invalid_person_instruction_text);

        // получаем текст инструкции
        SharedPreferences preferences = getSharedPreferences(KMService.SAVE, MODE_PRIVATE);
        // очищаем полученый текст от html тегов и "каракулей"
        instruction.loadDataWithBaseURL(null, preferences.getString(KMService.KEY_INSTRUCTION, ""),
                "text/html", "UTF-8", null);

        Button button = (Button) findViewById(R.id.invalid_person_instruction_button);
        button.setOnClickListener(new NextClickListener(this, MainPageActivity.class, null));
    }
}