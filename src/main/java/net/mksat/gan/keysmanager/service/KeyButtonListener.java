package net.mksat.gan.keysmanager.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import net.mksat.gan.keysmanager.R;
import net.mksat.gan.keysmanager.activities.EntryActivity;
import net.mksat.gan.keysmanager.activities.InputDataActivity;
import net.mksat.gan.keysmanager.activities.MainPageActivity;
import net.mksat.gan.keysmanager.database.DbLoaderFragment;
import ua.edu.nuos.androidtraining2013.kms.enums.KeyStatus;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 2 on 12.07.2014.
 */
public class KeyButtonListener implements View.OnClickListener {

    private AuditoryButton auditoryButton;
    private DbLoaderFragment dbFragment;
    Activity activity;

    public KeyButtonListener(AuditoryButton auditoryButton, Activity activity) {
        this.auditoryButton = auditoryButton;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        if (MainPageActivity.keyAvailable) {// если ключ на вахте (статус "ПРИНЯТ") и его нужно выдать
            if (auditoryButton.getStatus().equals(KeyStatus.ACCEPTED.toString())) { //то меняем статус ключа на "ВЫДАН"
                Bundle bundle = new Bundle();
                bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.SET_STATUS_KEY);
                bundle.putLong(DbLoaderFragment.ID_JANITOR, EntryActivity.idJanitor);
                bundle.putLong(DbLoaderFragment.ID_PERSONNEL, InputDataActivity.idPerson);
                bundle.putLong(DbLoaderFragment.ID_AUDITORIUM, Long.valueOf(auditoryButton.getId()));
                bundle.putString(DbLoaderFragment.ID_STATUS, KeyStatus.TAKEN.name());

                dbFragment = DbLoaderFragment.newInstance(bundle);
                activity.getFragmentManager().beginTransaction().add(dbFragment, "dbLoader").commit();

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                if (auditoryButton.getSecurityAlarm() != null) {     // если сигнализация есть, то появляется диалоговое окно с инструкцией
                    builder.setTitle(R.string.key_status_changed)
                            .setMessage(activity.getString(R.string.alarm_off)
                                    + auditoryButton.getAuditoryName() + "!\n" + auditoryButton.getSecurityAlarm())
                            .setIcon(R.drawable.ic_launcher)
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(activity, MainPageActivity.class);
                                            activity.startActivity(intent);
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {       // сигнализация отсутствует
                    builder.setTitle(R.string.key_status_changed)
                            .setIcon(R.drawable.ic_launcher)
                            .setCancelable(false);
                    final AlertDialog alert = builder.create();
                    alert.show();
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        public void run() {
                            alert.dismiss();
                            timer.cancel();
                            Intent intent = new Intent(activity, MainPageActivity.class);
                            activity.startActivity(intent);
                        }
                    }, 2500);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.key_is_taken)
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
        // если статус "ВЫДАН" меняем на "ПРИНЯТ"
        if (MainPageActivity.keyIssued) {
            if (auditoryButton.getStatus().equals(KeyStatus.TAKEN.toString())) {
                Bundle bundle = new Bundle();
                bundle.putInt(DbLoaderFragment.ID_QUERY, DbLoaderFragment.SET_STATUS_KEY);
                bundle.putLong(DbLoaderFragment.ID_JANITOR, EntryActivity.idJanitor);
                bundle.putLong(DbLoaderFragment.ID_PERSONNEL, InputDataActivity.idPerson);
                bundle.putLong(DbLoaderFragment.ID_AUDITORIUM, Long.valueOf(auditoryButton.getId()));
                bundle.putString(DbLoaderFragment.ID_STATUS, KeyStatus.ACCEPTED.name());
                dbFragment = DbLoaderFragment.newInstance(bundle);
                activity.getFragmentManager().beginTransaction().add(dbFragment, "dbLoader").commit();

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                if (auditoryButton.getSecurityAlarm() != null) {     // если сигнализация есть, то появляется диалоговое окно с инструкцией
                    builder.setTitle(R.string.key_status_changed)
                            .setMessage(activity.getString(R.string.alarm_on)
                                    + auditoryButton.getAuditoryName() + "!\n" + auditoryButton.getSecurityAlarm())
                            .setIcon(R.drawable.ic_launcher)
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(activity, MainPageActivity.class);
                                            activity.startActivity(intent);
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {       // сигнализация отсутствует
                    builder.setTitle(R.string.key_status_changed)
                            .setIcon(R.drawable.ic_launcher)
                            .setCancelable(false);
                    final AlertDialog alert = builder.create();
                    alert.show();
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        public void run() {
                            alert.dismiss();
                            timer.cancel();
                            Intent intent = new Intent(activity, MainPageActivity.class);
                            activity.startActivity(intent);
                        }
                    }, 2500);
                }


            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.key_is_accepted)
                        .setIcon(R.drawable.ic_launcher)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                ;
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
