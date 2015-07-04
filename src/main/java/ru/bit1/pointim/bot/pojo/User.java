package ru.bit1.pointim.bot.pojo;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by dmitry on 04/07/15.
 */
public class User {
    private String name;
    private Long chatid;
    private String pointToken;
    private String pointCsrf_token;

    private final IObjectUpdateListener listener;

    public User(IObjectUpdateListener listener) {
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.listener.objectChanged();
    }

    public Long getChatid() {
        return chatid;
    }

    public void setChatid(Long chatid) {
        this.chatid = chatid;
        this.listener.objectChanged();
    }

    public String getPointToken() {
        return pointToken;
    }

    public void setPointToken(String pointToken) {
        this.pointToken = pointToken;
        this.listener.objectChanged();
    }

    public String getPointCsrf_token() {
        return pointCsrf_token;
    }

    public void setPointCsrf_token(String csrf_token) {
        this.pointCsrf_token = csrf_token;
        this.listener.objectChanged();
    }

    @Override
    public int hashCode() {
        return name.hashCode() + chatid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof User))
            return false;
        User u = (User)obj;
        return  this.getChatid().equals(u.getChatid())
                && this.getName().equals(u.getName());
    }

    @Override
    public String toString() {
        return "{name="+name+"; chatid="+chatid+";}";
    }

    public static String serialize(User user) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(user.getName());
        sb.append("\n");
        sb.append(user.getChatid());
        sb.append("\n");
        if(user.getPointToken() != null) sb.append(user.getPointToken());
        sb.append("\n");
        if(user.getPointCsrf_token() != null) sb.append(user.getPointCsrf_token());
        sb.append("\n");
        return DatatypeConverter.printBase64Binary(sb.toString().getBytes("utf8"));
    }

    public static User deserialize(String line, IObjectUpdateListener listener) throws UnsupportedEncodingException {
        String lines = new String(DatatypeConverter.parseBase64Binary(line), "utf8");
        Scanner scanner = new Scanner(lines);
        String name = scanner.nextLine();
        String chatid = scanner.nextLine();
        String ptoken = scanner.nextLine();
        String pcsrftoken = scanner.nextLine();

        User user = new User(listener);
        user.setName(name);
        user.setChatid(Long.valueOf(chatid));
        if(!"".equals(ptoken)) user.setPointToken(ptoken);
        if(!"".equals(pcsrftoken)) user.setPointCsrf_token(pcsrftoken);

        return  user;
    }

    public boolean isPointLoggedIn() {
        return (pointToken != null && !"".equals(pointToken)) &&
                (pointCsrf_token != null && !"".equals(pointCsrf_token));
    }
}
