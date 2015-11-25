package ru.bit1.pointim.bot.api;

import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;
import ru.bit1.pointim.bot.pojo.Cache;
import ru.bit1.pointim.bot.pojo.Message;
import ru.bit1.pointim.bot.pojo.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by dmitry on 04/07/15.
 */
public class TelegramApi {

    final static Logger log = Logger.getLogger(TelegramApi.class);

    private final ArrayList<Message> messages = new ArrayList<Message>();
    private final Cache cache;
    private final JSONParser parser = new JSONParser();
    private final String token;

    private long offset = 0;


    public TelegramApi(Cache cache, String token) {
        this.cache = cache;
        this.token = token;
    }

    public byte[] getFile(String fileId) {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://api.telegram.org/bot" + token + "/getFile?file_id=" + fileId).openConnection();
            urlConnection.connect();

            Scanner reader = new Scanner(urlConnection.getInputStream());
            String responce = reader.useDelimiter("\\A").next();

            Map json = (Map) parser.parse(responce);
            if(json.containsKey("ok")) {
                Map res = (Map)json.get("result");
                String fileUrl = "https://api.telegram.org/file/bot"+token+"/"+res.get("file_path").toString();

                HttpsURLConnection urlFileConnection = (HttpsURLConnection) new URL(fileUrl).openConnection();
                urlFileConnection.connect();
                ByteArrayOutputStream buffstream = new ByteArrayOutputStream(1*1024);

                try(DataInputStream is = new DataInputStream(urlConnection.getInputStream())) {
                    final byte data[] = new byte[1024];
                    int count;
                    while ((count = is.read(data, 0, 1024)) != -1) {
                        buffstream.write(data, 0, count);
                    }
                }
                byte[] file = buffstream.toByteArray();
                log.debug("Received image:"+fileId+" size:"+file.length);
                return file;
            }

        } catch (Exception e) {
            log.error("Unable to get file from telegram!", e);
        }
        return null;
    }

    public List<Message> getUpdates() {
        messages.clear();

        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://api.telegram.org/bot"+token+"/getUpdates?offset="+ Long.valueOf(offset)).openConnection();
            urlConnection.connect();

            Scanner reader = new Scanner(urlConnection.getInputStream());
            String responce = reader.useDelimiter("\\A").next();

            Map json  = (Map)parser.parse(responce);
            if(json.containsKey("ok")) {
                for(Map res : ((List<Map>)json.get("result"))) {
                    offset = (Long)res.get("update_id") + 1;
                    Map message = (Map)res.get("message");
                    if(message != null && message.containsKey("text")) {
                        String text = (String) message.get("text");
                        String replyTo = null;
                        Map reply_to_message = (Map) message.get("reply_to_message");
                        if(reply_to_message != null && reply_to_message.containsKey("text")) {
                            replyTo = (String)reply_to_message.get("text");
                        }
                        String from = (String) ((Map)message.get("from")).get("first_name");
                        Long chatid = (Long) ((Map)message.get("chat")).get("id");
                        User user = this.cache.getUser(chatid, from);
                        Message msg = new Message();
                        msg.setUser(user);
                        msg.setType(Message.MessageType.TEXT);
                        msg.setText(text);
                        msg.setReplyTo(replyTo);
                        if(replyTo != null && replyTo.length() > 0) {
                            msg.setType(Message.MessageType.COMMENT);
                        }
                        messages.add(msg);
                        //log.debug("Received Message: " + msg);
                    } else if(message != null && message.containsKey("photo")) {
                        log.debug("Received photo message: "+message);
                        String from = (String) ((Map)message.get("from")).get("first_name");
                        Long chatid = (Long) ((Map)message.get("chat")).get("id");
                        User user = this.cache.getUser(chatid, from);
                        Message msg = new Message();
                        msg.setUser(user);
                        msg.setType(Message.MessageType.IMAGE);
                        long maxheight = 0;
                        for(Map photo : ((List<Map>)message.get("photo"))) { //find the biggest photo
                            long height = (Long)photo.get("height");
                            if(height > maxheight) {
                                maxheight = height;
                                msg.setFileId((String)photo.get("file_id"));
                            }
                        }
                        messages.add(msg);
                    }
                }
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            log.error("Unable to get updates from telegram!", e);
        }
        return messages;
    }

    private void sendTextMessage(Message msg) {
        try {
            log.debug("Sending Message: " + msg);
            if(msg.getText() == null || "".equals(msg.getText())) {
                log.error("Ignoring message with empty text: "+msg);
                return;
            }
            String text = URLEncoder.encode(msg.getText(), "UTF-8");
            String chatid = Long.toString(msg.getUser().getChatid());
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://api.telegram.org/bot" + token + "/sendMessage?chat_id="+chatid+"&text=" + text).openConnection();
            urlConnection.connect();

            Scanner reader = new Scanner(urlConnection.getInputStream());
            String responce = reader.useDelimiter("\\A").next();
            if(urlConnection.getResponseCode() != 200)
                log.error("Unable to send message! Return code" + urlConnection.getResponseCode()+ "! Response: " + responce);

            urlConnection.disconnect();

        } catch (Exception e) {
            log.error("Error sending Message: "+msg, e);
        }
    }

    public void sendMessage(Message msg) {
        switch (msg.getType()) {
            case TEXT:
                sendTextMessage(msg);
                break;
            default:
                log.error("Unsupported message type. Message: " + msg);
                break;
        }
    }
}
