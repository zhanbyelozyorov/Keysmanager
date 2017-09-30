package net.mksat.gan.keysmanager.database;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import net.mksat.gan.keysmanager.service.KMService;
import ua.edu.nuos.androidtraining2013.kms.dto.AuditoriumContainer;
import ua.edu.nuos.androidtraining2013.kms.dto.JournalEntryContainer;
import ua.edu.nuos.androidtraining2013.kms.dto.PersonnelContainer;
import ua.edu.nuos.androidtraining2013.kms.dto.PersonnelType;
import ua.edu.nuos.androidtraining2013.kms.enums.KeyStatus;

import java.util.Calendar;

/**
 * Created by sergey on 6/27/14.
 */
public class DbLoaderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * Интерфейс для обработки результата
     */
    public interface ReturnResult {
        public void getReturnResult(int result, Cursor cursor);
    }

    /**
     * обект интерфейса
     */
    ReturnResult result;

    // КОНСТАНТЫ ДЛЯ ЗАПРОСОВ К БАЗЕ
    public static final int GET_JANITOR = 0;
    public static final int GET_PERSONNEL = 1;
    public static final int GET_AUDITORIUM = 2;
    public static final int GET_PERMISSION = 3;
    public static final int GET_JOURNAL_ENTRY = 4;
    public static final int SET_STATUS_KEY = 5;

    public static final String ID_PERSONNEL = "id_personnel";
    public static final String ID_QUERY = "id_query";
    public static final String ID_AUDITORIUM = "id_auditorium";
    public static final String ID_JANITOR = "id_janitor";
    public static final String ID_STATUS = "id_status";

    private int idQuery;

    private static KMService service;
    private KMServiceConnection connect;

    /**
     * Статический метод для создания данного класса фрагментов
     *
     * @param args передача данных фрагменту
     * @return объект класса
     */
    public static DbLoaderFragment newInstance(Bundle args) {
        DbLoaderFragment dbLoaderFragment = new DbLoaderFragment();
        dbLoaderFragment.setArguments(args);
        return dbLoaderFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        connection(getArguments());
    }

    public void startQuery(Bundle bundle) {
        if (service == null) {
            connection(bundle);
        } else {
            idQuery = bundle.getInt(ID_QUERY);
            getLoaderManager().initLoader(idQuery, bundle, this);
        }
    }

    private void connection(Bundle bundle) {
        connect = new KMServiceConnection(bundle);
        getActivity().bindService(new Intent(getActivity(), KMService.class), connect, Context.BIND_AUTO_CREATE);
    }

    public void resetQuery(Bundle bundle) {
        getLoaderManager().restartLoader(bundle.getInt(ID_QUERY), bundle, this);
    }

    /**
     * передаем обработку активности фрагмента
     *
     * @param activity активность в которой создан фрагмент
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ReturnResult)
            result = (ReturnResult) activity;
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(connect);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new ResultLoader(getActivity(), idQuery, bundle);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (result != null)
            result.getReturnResult(cursorLoader.getId(), cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private static class ResultLoader extends CursorLoader {

        private int idQuery;
        private Bundle bundle;

        public ResultLoader(Context context, int idQuery, Bundle bundle) {
            super(context);
            this.idQuery = idQuery;
            this.bundle = bundle;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = null;
            long idPersonnel;
            long idAuditorium;
            switch (idQuery) {
                case GET_JANITOR:
                    cursor = service.getDbAdapter().getPersonnelType(PersonnelType.JANITOR);
                    break;
                case GET_AUDITORIUM:
                    cursor = service.getDbAdapter().getAllAuditorium();
                    break;
                case GET_PERMISSION:
                    idPersonnel = bundle.getLong(ID_PERSONNEL);
                    cursor = service.getDbAdapter().getPermission(idPersonnel);
                    break;
                case GET_JOURNAL_ENTRY:
                    cursor = service.getDbAdapter().getJournalEntry();
                    break;
                case GET_PERSONNEL:
                    cursor = service.getDbAdapter().getPersonnelType(PersonnelType.PERSONNEL);
                    break;
                case SET_STATUS_KEY:
                    idPersonnel = bundle.getLong(ID_PERSONNEL);
                    idAuditorium = bundle.getLong(ID_AUDITORIUM);
                    String status = bundle.getString(ID_STATUS);
                    AuditoriumContainer auditorium = service.getDbAdapter().getAuditorium(idAuditorium);
                    PersonnelContainer personnel = service.getDbAdapter().getPersonnel(idPersonnel);
                    service.getDbAdapter().updateAuditorium(auditorium, status);
                    JournalEntryContainer report = new JournalEntryContainer();
                    report.setPersonnelId(personnel.getId());
                    report.setAuditoriumId(auditorium.getId());
                    report.setEventDate(Calendar.getInstance().getTime());
                    report.setStatus(KeyStatus.valueOf(status));
                    service.getDbAdapter().add(report);
            }
            return cursor;
        }
    }

    private class KMServiceConnection implements ServiceConnection {

        private Bundle bundle;

        public KMServiceConnection(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((KMService.BindConnect) binder).getService();
            startQuery(bundle);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
