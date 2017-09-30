package net.mksat.gan.keysmanager.service;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import net.mksat.gan.keysmanager.R;
import ua.edu.nuos.androidtraining2013.kms.enums.KeyStatus;

import java.util.List;

/**
 * Created by 2 on 19.06.2014.
 */
public class KeyButtonAdapter extends ArrayAdapter<AuditoryButton> {

    List<AuditoryButton> auditoryButtons;
    Activity activity;

    public KeyButtonAdapter(Activity activity, List<AuditoryButton> buttonNames) {
        super(activity, 0, buttonNames);
        this.activity = activity;
        this.auditoryButtons = buttonNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Создаем объект класса LayoutInflater, который может автоматические создавать структуру объектов
        // соответствующих элементам интерфейса (кнопкам, контейнерам и т.д.) из данного лайаута.
        LayoutInflater inflater = activity.getLayoutInflater();

        // Создаем ("надуваем") структуру объектов из лайаута key_button_blue. Метод inflate() всегда возвращает объект
        // соответствующий корневому элементу интерфейса. В нашем случае это FrameLayout (см. лайаут).
        View buttonContainer;
        boolean isAccepted = auditoryButtons.get(position).getStatus().equals(KeyStatus.ACCEPTED.toString());
        if (isAccepted)
            buttonContainer = inflater.inflate(R.layout.key_button_blue, parent, false);
        else
            buttonContainer = inflater.inflate(R.layout.key_button_red, parent, false);

        // Находим объект соответствующей кнопке. Этот объект на самом деле является дочерним к FrameLayout - в смысле
        // структуры обьектов в лайауте (а НЕ в смысле наследования классов).
        Button button = (Button) buttonContainer.findViewById(R.id.button);

        // Находим объект типа AuditoryButton соответствующий кпопке, которую мы тут создаем для GridView. Для того
        // чтобы его найти мы пользуемся параметром position, который передает нам GridView, и списком всех таких
        // объектов которые мы создаем ранее в IdentificationActivity или StatusOfKeysActivity. Мы этот список сохранили
        // в конструкторе класса KeyButtonAdapter как поле этого класса auditoryButtons.
        AuditoryButton auditoryButton = auditoryButtons.get(position);

        // Назначаем кнопке текст, который будет отображаться на экране. Для этого соединяем разные поля объекта типа
        // AuditoryButton, который мы нашли выше.
        if (isAccepted)
            button.setText(auditoryButton.getCampus() + "\n" + auditoryButton.getAuditoryName() + "\n" +
                    activity.getText(R.string.key_status_accept));
        else
            button.setText(auditoryButton.getCampus() + "\n" + auditoryButton.getAuditoryName() + "\n" +
                    activity.getText(R.string.key_status_taken));

        // Создаем и назначаем кнопке объект типа KeyButtonListener, который будет обрабатывать нажатия на кнопку. Для
        // того, чтобы этот объект-обработчик знал с какой кнопной он имеет дело - мы передаем ему соответствующей
        // объект типа AuditoryButton в конструкторе. Созданный объект KeyButtonListener запоминает этот объект и сможет
        // им пользоваться когда кнопка будет вызывать его метод onClick в ответ на нажатие кнопки.
        KeyButtonListener listener = new KeyButtonListener(auditoryButton, activity);
        button.setOnClickListener(listener);

        // Возвращаем корневой объект FrameLayout назад в GridView, чтобы он мог поместить его в соответствующую ячейку
        // в таблице кнопок. Этот объект связан (содержит) также и объект кнопки, которую мы настроили.
        return buttonContainer;
    }
}
