package ru.bit1.pointim.bot.api;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.bit1.pointim.bot.PointImBot;
import ru.bit1.pointim.bot.pojo.Message;
import ru.bit1.pointim.bot.pojo.User;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by dmitry on 04/07/15.
 */
public class PointWebSocketClient extends WebSocketClient {

    final static Logger log = Logger.getLogger(PointWebSocketClient.class);
    private String token;
    private JSONParser parser = new JSONParser();
    private User user;
    private PointImBot bot;
    private boolean connected = false;

    public PointWebSocketClient(URI serverURI, String token, User user, PointImBot bot) {
        super(serverURI,new Draft_10cust());
        //WebSocketImpl.DEBUG = true;
        this.token = token;
        this.user = user;
        this.bot = bot;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("On Open.");
        //FramedataImpl1 ping = new FramedataImpl1(Framedata.Opcode.PING);
        //this.send(ping.getPayloadData().array());
        this.send("Authorization: "+token);
        //this.send("Authorization: "+token);

    }

    @Override
    public void onMessage(String s) {
        log.info("On Message: " + s);
        if(!connected)
            connected = true;

        if("ping".equals(s))
            return;

        Map json;
        try {
            json  = (Map)parser.parse(s);
        } catch (ParseException e) {
            return;
        }

        if(json.containsKey("login")) {
            bot.putOutbound(Message.makeTextMessage(user, "Notifications enabled for user @"+json.get("login")+"!"));
            return;
        }

        String text = null, author = null, totext = null, post_id = null, html = null, tags = "", a = null, post_author="",post_text="";
        String comments = "", comment_id="";

        if(json.containsKey("comment_id") && json.get("comment_id") != null) {
            comment_id = json.get("comment_id").toString();
        }
        if(json.containsKey("comments") && json.get("comments") != null) {
            comments = json.get("comments").toString();
        }
        if(json.containsKey("post_text") && json.get("post_text") != null) {
            post_text = (String)json.get("post_text");
        }
        if(json.containsKey("post_author") && json.get("post_author") != null) {
            post_author = (String)json.get("post_author");
        }
        if(json.containsKey("a")) {
            a = (String)json.get("a");
        }
        if(json.containsKey("text")) {
            text = (String)json.get("text");
        }
        if(json.containsKey("author")) {
            author = (String)json.get("author");
        }
        if(json.containsKey("tags")) {
            for(Object tag : (List)json.get("tags")) {
                tags += "*"+tag.toString() + " ";
            }
        }
        if(json.containsKey("to_text")) {
            totext = (String)json.get("to_text");
        }
        if(json.containsKey("post_id")) {
            post_id = (String)json.get("post_id");
        }
        if(json.containsKey("html")) {
            html = (String)json.get("html");
        }

        StringBuilder post = new StringBuilder();
        if("rec".equals(a)) { //recommendation
            post.append("@");
            post.append(author);
            post.append(" recommends: ");
            post.append(text);
            post.append("\n");
            post.append("@");
            post.append(post_author);
            post.append(": ");
            post.append(tags);
            post.append("\n");
            if(post_text.length() < 1000) {
                post.append(post_text);
            } else {
                post.append(post_text.substring(0, 1000));
                post.append(" ...");
            }
        } else { //simple post or comment
            post.append("@");
            post.append(author);
            if("post_edited".equals(a)) {
                post.append(" edited ");
            }
            post.append(": ");
            post.append(tags);
            post.append("\n");
            if (totext != null && !"".equals(totext)) {
                post.append(">> ");
                if (totext.length() < 100) {
                    post.append(totext);
                } else {
                    post.append(totext.substring(0, 100));
                    post.append(" ...");
                }
                post.append("\n");
            }
            post.append(text);
        }
        post.append("\n#");
        post.append(post_id);
        if(comments.length() > 0) {
            post.append("/");
            post.append(comments);
        } else if(comment_id.length() > 0) {
            post.append("/");
            post.append(comment_id);
        }
        post.append(" ");
        post.append("http://point.im/");
        post.append(post_id);

        bot.putOutbound(Message.makeTextMessage(user, post.toString()));
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        connected = false;
        log.info("On Close: " + i + " " + s + " " + b);
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        connected = true;
        log.info("ping");
        super.onWebsocketPing(conn, f);
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        connected = true;
        log.info("pong");
        super.onWebsocketPong(conn, f);
    }

    @Override
    public void onError(Exception e) {
        connected = false;
        log.error("On Error.", e);
    }

    public boolean isConnected() {
        return this.connected;
    }
}
