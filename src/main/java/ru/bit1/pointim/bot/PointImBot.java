package ru.bit1.pointim.bot;

import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.codesnippets4all.json.parsers.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by dmitry on 01/07/15.
 */
public class PointImBot implements Runnable {

    private final JSONParser parser;
    private final String telegramToken;
    private int offset = 0;

    public PointImBot(String telegramToken) {
        this.telegramToken = telegramToken;
        JsonParserFactory factory= JsonParserFactory.getInstance();
        parser = factory.newJsonParser();

    }

    public void debug(String message) {
        System.out.print("["+new Date()+"] "+message + "\n");
    }

    public void run() {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://api.telegram.org/bot"+telegramToken+"/getUpdates?offset="+ Integer.valueOf(offset)).openConnection();
            urlConnection.connect();

            //Scanner reader = new Scanner(urlConnection.getInputStream());
            //String responce = reader.useDelimiter("\\A").next();

            Map json  = parser.parseJson(urlConnection.getInputStream(), "utf8");
            if(json.containsKey("ok")) {
                for(Map res : ((List<Map>)json.get("result"))) {
                    offset = Integer.parseInt((String)res.get("update_id")) + 1;
                    Map message = (Map)res.get("message");
                    if(message != null && message.containsKey("text")) {
                        String text = (String) message.get("text");
                        String user = (String) ((Map)message.get("from")).get("first_name");
                        String chatid = (String) ((Map)message.get("chat")).get("id");
                        debug("Message from "+user+" ("+chatid+"): "+text);
                    }
                }
            }

            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        try {
            Thread telegramThread  = new Thread(new PointImBot("115215457:AAHaqzxADZrx6pnGL6k5HlZxtGvNJw_o_9w"));
            telegramThread.start();
            telegramThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
