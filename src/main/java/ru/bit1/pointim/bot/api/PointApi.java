package ru.bit1.pointim.bot.api;

import org.apache.log4j.Logger;
import org.javatuples.Pair;
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

    private String callApiGet(String method, List<Pair<String,String>> params, String token) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(Pair<String,String> entry : params) {
            sb.append(entry.getValue0());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue1(), "UTF-8"));
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

    private String callApiPost(String method, List<Pair<String,String>> params, String token, String csrf_token) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://point.im/api/"+method).openConnection();
        urlConnection.setRequestMethod("POST");

        StringBuilder sb = new StringBuilder();
        for(Pair<String,String> entry : params) {
            sb.append(entry.getValue0());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue1(), "UTF-8"));
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

    private String callApiPost(String method, List<Pair<String,String>> params) throws IOException {
        return callApiPost(method, params, null, null);
    }

    private String callApiPost(String method, List<Pair<String,String>> params, String token) throws IOException {
        return callApiPost(method, params, token, null);
    }

    public String login(User user, String login, String password) {
        if(user.isPointLoggedIn())
            return "Already logged in.";
        try {
            ArrayList<Pair<String,String>> params = new ArrayList<>();
            params.add(new Pair<>("login", login));
            params.add(new Pair<>("password", password));
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

            ArrayList<Pair<String,String>> params = new ArrayList<>();
            params.add(new Pair<>("csrf_token", user.getPointCsrf_token()));
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

    public String post(User user, Collection<String> tags, String text, boolean isprivate) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            ArrayList<Pair<String,String>> params = new ArrayList<>();

            for(String tag : tags) {
                params.add(new Pair<>("tag", tag));
            }
            params.add(new Pair<>("text", text));
            if(isprivate)
                params.add(new Pair<>("private","true"));
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

    public String comment(User user, String text, String postid, String to_comment) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            ArrayList<Pair<String,String>> params = new ArrayList<>();

            params.add(new Pair<>("text", text));
            if(to_comment != null)
                params.add(new Pair<>("comment_id", to_comment));

            String responce = callApiPost("post/"+postid, params, user.getPointToken(), user.getPointCsrf_token());

            Map json = (Map) parser.parse(responce);
            if (json.containsKey("id")) {
                return "Comment #" + json.get("id") + " added. http://point.im/"+json.get("id");
            }

        } catch (Exception e) {
            log.error("Unable to create comment!", e);
        }
        return "Unable to create comment!";
    }

    public String recommend(User user, String text, String postid, String to_comment) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            ArrayList<Pair<String,String>> params = new ArrayList<>();

            params.add(new Pair<>("text", text));
            String url;
            if(to_comment != null) {
                url = "post/"+postid+"/"+to_comment+"/r";
            } else {
                url = "post/"+postid+"/r";
            }

            String responce = callApiPost(url, params, user.getPointToken(), user.getPointCsrf_token());

            Map json = (Map) parser.parse(responce);
            if (json.containsKey("ok") && (Boolean) json.get("ok")) {
                return null;
            }
            if (json.containsKey("comment_id")) {
                return "Recommended #"+postid+"/" + json.get("comment_id") + " . http://point.im/"+postid;
            }

        } catch (Exception e) {
            log.error("Unable to recommend!", e);
        }
        return "Unable to recommend!";
    }


    public String getpost(User user, String postid) {
        try {
            if (!user.isPointLoggedIn())
                return "Login first.";

            String responce = callApiGet("post/" + postid, Collections.EMPTY_LIST, user.getPointToken());

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
