package net.mksat.gan.keysmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import net.mksat.gan.keysmanager.activities.EntryActivity;
import ua.edu.nuos.androidtraining2013.kms.dto.*;
import ua.edu.nuos.androidtraining2013.kms.enums.KeyStatus;

import java.text.DateFormat;
import java.util.*;

/**
 * Адаптер базы данных
 */
public class DbAdapter implements BaseColumns {

    // тег для логов
    public static final String TAG = "DatabaseAdapter";


    public static final String DATABASE_NAME = "keys_manager.db";

    // имена таблиц
    public static final String TABLE_PERSONNEL = "personnel";
    public static final String TABLE_AUDITORIUM = "auditorium";
    public static final String TABLE_JOURNAL_ENTRY = "journal_entry";
    public static final String TABLE_PERMISSION = "permission";
    public static final String TABLE_AUDITORIUM_ALL_PERSONNEL = "auditorium_all_personnel";

    //имена столбцов таблиц
    public static final String KEY_NAME = "name";
    public static final String KEY_DATE = "date";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ID_PERSONNEL = "id_personnel";
    public static final String KEY_ID_AUDITORIUM = "id_auditorium";
    public static final String KEY_ID_PERMISSION_ORDER = "id_permission_order";
    public static final String KEY_ID_JANITOR = "id_janitor";
    public static final String KEY_CAMPUS = "campus";
    public static final String KEY_CODE = "code";
    public static final String KEY_SECURITY_ALARM = "security_alarm";

    // запросы к базе
    private static final String QUERY_PERMISSION = "SELECT * FROM " + TABLE_AUDITORIUM + " WHERE _id IN (SELECT " + KEY_ID_AUDITORIUM
            + " FROM " + TABLE_PERMISSION + " WHERE " + KEY_ID_PERSONNEL + "=?) OR _id IN (SELECT _id  FROM " + TABLE_AUDITORIUM_ALL_PERSONNEL + ")";

    private static final String TABLE_AUDITORIUM_ALL_PERSONNEL_CREATE = "create table " + TABLE_AUDITORIUM_ALL_PERSONNEL + " (_id integer primary key);";

    private static final int DATABASE_VERSION = 3;

    // объект для работы с базой
    SQLiteDatabase db;

    // Конструктор и связываемся с базой, если она не создання то создается
    public DbAdapter(Context context) {
        try {
            db = new DbHelper(context).getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to connect database", e);
        }
    }

    // закрываем связь с базой
    public void close() {
        db.close();
    }

    /**
     * Метод возврашает курсор таблици отчетов
     *
     * @return курсор
     */
    public Cursor getJournalEntry() {
        try {
            return db.query(TABLE_JOURNAL_ENTRY, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_JOURNAL_ENTRY + " empty", e);
            return null;
        }
    }

    public List<JournalEntryContainer> getListJournalEntry(){
        Cursor cursor = getJournalEntry();
        List<JournalEntryContainer> list= new ArrayList<JournalEntryContainer>();
        while (cursor.moveToNext()){
            JournalEntryContainer container = new JournalEntryContainer();
            container.setAuditoriumId(cursor.getLong(3));
            container.setPersonnelId(cursor.getLong(1));
            container.setEventDate(new Date(cursor.getLong(2)));
            container.setStatus(KeyStatus.valueOf(cursor.getString(4)));
            list.add(container);
        }
        return list;
    }

    /**
     * Метод добавляет в таблицу отчетав строку
     *
     * @param container строка отчета
     * @return true добавило
     * false не добавило
     */
    public boolean add(JournalEntryContainer container) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID_PERSONNEL, container.getPersonnelId());
        cv.put(KEY_DATE, container.getEventDate().getTime());
        cv.put(KEY_ID_AUDITORIUM, container.getAuditoriumId());
        cv.put(KEY_STATUS, container.getStatus().name());
        db.beginTransaction();
        try {
            if (db.insert(TABLE_JOURNAL_ENTRY, _ID, cv) == -1) return false;
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Метод возврашает курсор на таблику ключей
     *
     * @return курсор
     */
    public Cursor getAllAuditorium() {
        try {
            return db.query(TABLE_AUDITORIUM, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_AUDITORIUM + " empty", e);
            return null;
        }
    }

    /**
     * Метод добавляет ключ в таблицу ключей
     *
     * @param auditorium объект ключа
     * @return результат положительний или отрицательный
     */
    public boolean add(AuditoriumContainer auditorium) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, auditorium.getId());
        cv.put(KEY_NAME, auditorium.getName());
        cv.put(KEY_CAMPUS, auditorium.getCampus());
        cv.put(KEY_STATUS, KeyStatus.ACCEPTED.name());
        cv.put(KEY_SECURITY_ALARM, auditorium.getSecurityDescription());
        db.beginTransaction();
        try {
            if (db.insert(TABLE_AUDITORIUM, _ID, cv) == -1) return false;
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        Log.i(TAG, "addAuditorium");
        return true;
    }

    public AuditoriumContainer getAuditorium(long id) {
        Cursor cursor = db.query(TABLE_AUDITORIUM, null, _ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        AuditoriumContainer container = null;
        if (cursor.moveToFirst()) {
            container = new AuditoriumContainer();
            container.setId(id);
            container.setName(cursor.getString(1));
            container.setCampus(cursor.getString(2));
            container.setSecurityDescription(cursor.getString(4));
        }
        cursor.close();
        return container;
    }

    public PersonnelContainer getPersonnel(long id) {
        Cursor cursor = db.query(TABLE_PERSONNEL, null, _ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        PersonnelContainer personnel = null;
        if (cursor.moveToFirst()) {
            personnel = new PersonnelContainer();
            personnel.setId(id);
            personnel.setName(cursor.getString(1));
            personnel.setCode(cursor.getString(2));
            personnel.setType(PersonnelType.valueOf(cursor.getString(3)));
        }
        return personnel;
    }

    /**
     * Метод удаляет по индикатору ключ с таблицы
     *
     * @param id индикатор ключа
     * @return результат удаления
     */
    public boolean removeAuditorium(long id) {
        return db.delete(TABLE_AUDITORIUM, _ID + "=" + id, null) > 0;
    }

    /**
     * Метод изменяет значения ключа
     *
     * @param auditorium
     * @return результат изменения
     */
    public boolean updateAuditorium(AuditoriumContainer auditorium) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, auditorium.getId());
        cv.put(KEY_NAME, auditorium.getName());
        cv.put(KEY_CAMPUS, auditorium.getCampus());
        cv.put(KEY_SECURITY_ALARM, auditorium.getSecurityDescription());
        db.beginTransaction();
        try {
            db.update(TABLE_AUDITORIUM, cv, _ID + "=" + auditorium.getId(), null);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Could not update the record", e);
            return false;
        } finally {
            db.endTransaction();
        }
        Log.i(TAG, "updateAuditorium");
        return true;
    }

    /**
     * Метод изменяет значения ключа
     *
     * @param auditorium
     * @return результат изменения
     */
    public boolean updateAuditorium(AuditoriumContainer auditorium, String status) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, auditorium.getId());
        cv.put(KEY_NAME, auditorium.getName());
        cv.put(KEY_CAMPUS, auditorium.getCampus());
        cv.put(KEY_STATUS, status);
        cv.put(KEY_SECURITY_ALARM, auditorium.getSecurityDescription());
        db.beginTransaction();
        try {
            db.update(TABLE_AUDITORIUM, cv, _ID + "=" + auditorium.getId(), null);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Could not update the record", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Метод возврашает курсор на таблицу с людми беруших ключи
     *
     * @return курсор
     */
    public Cursor getAllPersonnel() {
        try {
            return db.query(TABLE_PERSONNEL, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_PERSONNEL + " empty", e);
            return null;
        }
    }

    /**
     * Метод добавляет человека в таблицу
     *
     * @param personnel человек беруший ключи
     * @return результат добавления
     */
    public boolean add(PersonnelContainer personnel) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, personnel.getId());
        cv.put(KEY_NAME, personnel.getName());
        cv.put(KEY_CODE, personnel.getCode());
        cv.put(KEY_STATUS, personnel.getType().name());
        db.beginTransaction();
        try {
            if (db.replace(TABLE_PERSONNEL, _ID, cv) == -1) return false;
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Метод возврашает курсор на таблику ключей
     *
     * @return курсор
     */
    public Cursor getPersonnelType(PersonnelType type) {
        try {
            return db.query(TABLE_PERSONNEL, null, KEY_STATUS + "=?", new String[]{type.name()}, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_AUDITORIUM + " empty", e);
            return null;
        }
    }

    /**
     * Метод удаляет человека с таблицы
     *
     * @param id индекатор
     * @return результат удаления
     */
    public boolean removePersonnel(long id) {
        return db.delete(TABLE_PERSONNEL, _ID + "=" + id, null) > 0;
    }

    /**
     * Метод изменяет данные человека в таблице
     *
     * @param personnel данные о человеке
     * @return результат изменения
     */
    public boolean updatePersonnel(PersonnelContainer personnel) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, personnel.getId());
        cv.put(KEY_NAME, personnel.getName());
        cv.put(KEY_CODE, personnel.getCode());
        cv.put(KEY_STATUS, personnel.getType().name());
        db.beginTransaction();
        try {
            db.update(TABLE_PERSONNEL, cv, _ID + "=" + personnel.getId(), null);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Could not update the record", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Метод добавляет связь между таблицами
     *
     * @return
     */
    public boolean add(PermissionContainer permission) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, permission.getId());
        cv.put(KEY_ID_AUDITORIUM, permission.getAuditoriumId());
        cv.put(KEY_ID_PERSONNEL, permission.getPersonnelId());
        cv.put(KEY_ID_PERMISSION_ORDER, permission.getPermissionOrderId());
        db.beginTransaction();
        try {
            if (db.replace(TABLE_PERMISSION, _ID, cv) == -1) return false;
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Метод возврашает курсор на таблицу с людми беруших ключи
     *
     * @return курсор
     */
    public Cursor getAllPermission() {
        try {
            return db.query(TABLE_PERMISSION, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_PERMISSION + " empty", e);
            return null;
        }
    }

    public Cursor getPermission(long id) {
        try {
            return db.rawQuery(QUERY_PERMISSION, new String[]{String.valueOf(id)});
        } catch (SQLiteException e) {
            Log.e(TAG, "Database " + TABLE_PERMISSION + " empty", e);
            return null;
        }
    }

    /**
     * Метод изменяет данные человека в таблице
     *
     * @param permission
     * @return результат изменения
     */
    public boolean updatePermission(PermissionContainer permission) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, permission.getId());
        cv.put(KEY_ID_AUDITORIUM, permission.getAuditoriumId());
        cv.put(KEY_ID_PERSONNEL, permission.getPersonnelId());
        cv.put(KEY_ID_PERMISSION_ORDER, permission.getPermissionOrderId());
        db.beginTransaction();
        try {
            db.update(TABLE_PERMISSION, cv, _ID + "=" + permission.getId(), null);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean addAllPermission(long id) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, id);
        db.beginTransaction();
        try {
            if (db.replace(TABLE_AUDITORIUM_ALL_PERSONNEL, _ID, cv) == -1) return false;
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert row", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Проверяем либо заполняем базу данными с сервера
     *
     * @param catalogContainer
     */
    public void update(CatalogContainer catalogContainer) {
        Log.i(TAG, "update");
        for (AuditoriumContainer auditorium : catalogContainer.getAuditoriumList()) {
            if (!add(auditorium))
                updateAuditorium(auditorium);
        }
        for (PersonnelContainer personnel : catalogContainer.getPersonnelList()) {
            if (!add(personnel)) updatePersonnel(personnel);
        }
        for (PermissionContainer permission : catalogContainer.getPermissionList()) {
            if (!add(permission)) updatePermission(permission);
        }
        deleteOld(catalogContainer);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDITORIUM_ALL_PERSONNEL);
        db.execSQL(TABLE_AUDITORIUM_ALL_PERSONNEL_CREATE);
        Set<AuditoriumContainer> setAuditorium = new HashSet<AuditoriumContainer>();
        for (PermissionContainer permission : catalogContainer.getPermissionList()) {
            for (AuditoriumContainer auditorium : catalogContainer.getAuditoriumList()) {
                if (auditorium.getId() == permission.getAuditoriumId()) {
                    setAuditorium.add(auditorium);
                    break;
                }
            }
        }
        catalogContainer.getAuditoriumList().removeAll(setAuditorium);
        for (AuditoriumContainer auditorium : catalogContainer.getAuditoriumList()) {
            addAllPermission(auditorium.getId());
        }

    }

    /**
     * Удаляем данные с таблици которых нет на сервере
     *
     * @param catalogContainer
     */
    private void deleteOld(CatalogContainer catalogContainer) {
        Cursor cursor = getAllPersonnel();
        while (cursor.moveToNext()) {
            PersonnelContainer personnel = new PersonnelContainer();
            personnel.setId(cursor.getLong(0));
            personnel.setName(cursor.getString(1));
            personnel.setCode(cursor.getString(2));
            personnel.setType(PersonnelType.valueOf(cursor.getString(3)));
            if (!catalogContainer.getPersonnelList().contains(personnel)) removePersonnel(cursor.getLong(0));
        }
        cursor = getAllAuditorium();
        while (cursor.moveToNext()) {
            AuditoriumContainer container = new AuditoriumContainer();
            container.setId(cursor.getLong(0));
            container.setName(cursor.getString(1));
            container.setCampus(cursor.getString(2));
            container.setSecurityDescription(cursor.getString(4));
            if (!catalogContainer.getAuditoriumList().contains(container)) removeAuditorium(cursor.getLong(0));
        }
        cursor = getAllPermission();
        while (cursor.moveToNext()) {
            PermissionContainer container = new PermissionContainer();
            container.setId(cursor.getLong(0));
            container.setAuditoriumId(cursor.getLong(1));
            container.setPersonnelId(cursor.getLong(2));
            container.setPermissionOrderId(cursor.getLong(3));
            if (!catalogContainer.getPermissionList().contains(container)) removeAuditorium(cursor.getLong(0));
        }
    }

    /**
     * Класс для создания базы данных
     */
    private static class DbHelper extends SQLiteOpenHelper {

        // строковые константы для создания таблиц
        private static final String TABLE_PERSONNEL_CREATE = "create table " + TABLE_PERSONNEL + " (_id integer primary key, "
                + KEY_NAME + " text not null, " + KEY_CODE + " text not null, " + KEY_STATUS + " text not null);";

        private static final String TABLE_AUDITORIUM_CREATE = "create table " + TABLE_AUDITORIUM + " (_id integer primary key, "
                + KEY_NAME + " text not null, " + KEY_CAMPUS + " text, " + KEY_STATUS + " text, " + KEY_SECURITY_ALARM + " text);";

        private static final String TABLE_JOURNAL_ENTRY_CREATE = "create table " + TABLE_JOURNAL_ENTRY + " (_id integer primary key autoincrement," +
                KEY_ID_PERSONNEL + " integer, " + KEY_DATE + " integer, " + KEY_ID_AUDITORIUM + " integer, " + KEY_STATUS + " text not null, " +
                "FOREIGN KEY (" + KEY_ID_PERSONNEL + ") REFERENCES " + TABLE_PERSONNEL + " (" + _ID + "), FOREIGN KEY (" + KEY_ID_AUDITORIUM + ") " +
                "REFERENCES " + TABLE_AUDITORIUM + " (" + _ID + "));";

        private static final String TABLE_PERMISSION_CREATE = "create table " + TABLE_PERMISSION + " (_id integer primary key, " +
                KEY_ID_AUDITORIUM + " integer, " + KEY_ID_PERSONNEL + " integer, " + KEY_ID_PERMISSION_ORDER + " integer, " +
                "FOREIGN KEY (" + KEY_ID_AUDITORIUM + ") REFERENCES " + TABLE_AUDITORIUM + " (" + _ID + "), FOREIGN KEY (" + KEY_ID_PERSONNEL + ")" +
                " REFERENCES " + TABLE_PERSONNEL + " (" + _ID + "));";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_PERSONNEL_CREATE);
            db.execSQL(TABLE_PERMISSION_CREATE);
            db.execSQL(TABLE_AUDITORIUM_ALL_PERSONNEL_CREATE);
            db.execSQL(TABLE_AUDITORIUM_CREATE);
            db.execSQL(TABLE_JOURNAL_ENTRY_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONNEL);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDITORIUM_ALL_PERSONNEL);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMISSION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL_ENTRY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDITORIUM);
            onCreate(db);
        }
    }
}
