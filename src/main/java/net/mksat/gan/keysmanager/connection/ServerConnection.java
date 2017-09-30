package net.mksat.gan.keysmanager.connection;

import ua.edu.nuos.androidtraining2013.kms.dto.CatalogContainer;
import ua.edu.nuos.androidtraining2013.kms.dto.JournalEntryContainer;
import ua.edu.nuos.androidtraining2013.kms.util.JSONUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static ua.edu.nuos.androidtraining2013.kms.util.JSONUtil.toJSON;

/**
 * Created by sergey on 7/7/14.
 */
public class ServerConnection {


    public static CatalogContainer connectionParser(String httpURL) throws IOException {
        URL url = new URL(httpURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        InputStream inputStream = connection.getInputStream();
        String jsonFromURL = inputStreamToString(inputStream);
        inputStream.close();
        connection.disconnect();
        return JSONUtil.fromJson(jsonFromURL, CatalogContainer.class);
    }

    public static void outputJournalEntry(String httpURL, List<JournalEntryContainer> list) throws IOException {
        if(list.size() != 0) {
            URL url = new URL(httpURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();
            OutputStream os = new BufferedOutputStream(connection.getOutputStream());
            os.write(toJSON(list).getBytes());
            os.flush();
            os.close();
            connection.disconnect();
        }
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String read = bufferedReader.readLine();
        while (read != null) {
            stringBuilder.append(read);
            read = bufferedReader.readLine();
        }
        return stringBuilder.toString();
    }
}
