package ru.bit1.pointim.bot.api;

import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;
import ru.bit1.pointim.bot.pojo.Message;
import ru.bit1.pointim.bot.pojo.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by dmitry on 04/07/15.
 */
public class PointApi {
    final static Logger log = Logger.getLogger(PointApi.class);

    private JSONParser parser = new JSONParser();

    public PointApi() {

    }

    private String callApiGet(String method, Map<String,String> params, String token) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            sb.append("&");
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://point.im/api/"+method+ (sb.length()>0 ? sb.toString() : "")).openConnection();
        urlConnection.setRequestMethod("GET");
        if(token != null) urlConnection.setRequestProperty("Authorization", token);

        urlConnection.connect();

        Scanner reader = new Scanner(urlConnection.getInputStream());
        String responce = reader.useDelimiter("\\A").next();

        urlConnection.disconnect();
        return responce;
    }

    private String callApiPost(String method, Map<String,String> headers, String token, String csrf_token) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://point.im/api/"+method).openConnection();
        urlConnection.setRequestMethod("POST");

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,String> entry : headers.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            sb.append("&");
        }
        byte[] buff = sb.toString().getBytes("utf8");

        if(token != null) urlConnection.setRequestProperty("Authorization", token);
        if(token != null) urlConnection.setRequestProperty("X-CSRF", csrf_token);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty( "charset", "utf-8");
        urlConnection.setRequestProperty( "Content-Length", Integer.toString( buff.length ));
        urlConnection.setDoOutput(true);
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setUseCaches(false);

        try(DataOutputStream ws = new DataOutputStream(urlConnection.getOutputStream())) {
            ws.write(buff);
        }

        urlConnection.connect();

        Scanner reader = new Scanner(urlConnection.getInputStream());
        String responce = reader.useDelimiter("\\A").next();

        urlConnection.disconnect();
        return responce;
    }

    private String callApiPost(String method, Map<String,String> headers) throws IOException {
        return callApiPost(method, headers, null, null);
    }

    private String callApiPost(String method, Map<String,String> headers, String token) throws IOException {
        return callApiPost(method, headers, token, null);
    }

    public String login(User user, String login, String password) {
        if(user.isPointLoggedIn())
            return "Already logged in.";
        try {
            HashMap<String,String> params = new HashMap<String, String>();
            params.put("login", login);
            params.put("password", password);
            String responce = callApiPost("login", params);

            Map json  = (Map)parser.parse(responce);
            if(json.containsKey("token") && json.containsKey("csrf_token")) {
                user.setPointToken((String) json.get("token"));
                user.setPointCsrf_token((String) json.get("csrf_token"));
                return null;
            }

            return "Unable to login to point!";

        } catch (Exception e) {
            log.error("Unable to login to point.im!", e);
            return "Unable to make point.im api call!";
        }

    }

    public String logout(User user) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("csrf_token", user.getPointCsrf_token());
            String responce = callApiPost("logout", params, user.getPointToken());

            Map json = (Map) parser.parse(responce);
            if (json.containsKey("ok") && (boolean) json.get("ok")) {
                user.setPointCsrf_token(null);
                user.setPointToken(null);
                return null;
            }

        } catch (Exception e) {
            log.error("Unable to logout!", e);
            user.setPointCsrf_token(null);
            user.setPointToken(null);
        }
        return null;

    }

    public String post(User user, Collection<String> tags, String text) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            StringBuilder sb =new StringBuilder();
            for(String tag : tags) {
                sb.append(tag);
                sb.append(",");
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("text", text);
            params.put("tag", sb.toString());
            String responce = callApiPost("post", params, user.getPointToken(), user.getPointCsrf_token());

            Map json = (Map) parser.parse(responce);
            if (json.containsKey("id")) {
                return "Post #" + json.get("id") + " sent. http://point.im/"+json.get("id");
            }

        } catch (Exception e) {
            log.error("Unablel to create post!", e);
        }
        return "Unablel to create post!";
    }

    public String getpost(User user, String postid) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            String responce = callApiGet("post/" + postid, Collections.EMPTY_MAP, user.getPointToken());

            Map json = (Map) parser.parse(responce);
            if (json.containsKey("post")) {
                String author =  "@"+ (String) ((Map)((Map)json.get("post")).get("author")).get("login");
                String text = (String) ((Map)json.get("post")).get("text");
                String tagtext = " ";
                for(Object tag : (List)((Map)json.get("post")).get("tags")) {
                    tagtext += tag.toString() + " ";
                }
                return author + tagtext + "\n" + text;
            }

        } catch (Exception e) {
            log.error("Unablel to get post!", e);
        }
        return "Unablel to get post!";
    }
}
