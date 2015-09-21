package ru.bit1.pointim.bot.pojo;

/**
 * Created by dmitry on 04/07/15.
 */
public class Message {
    public enum MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        DOCUMENT
    }

    private String text;
    private MessageType type;
    private User user;

    public Message() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "{type="+type+"; user="+user+"; text="+text+";}";
    }

    public static Message makeTextResponce(Message msg, String text) {
        Message ret = new Message();

        ret.setUser(msg.getUser());
        ret.setType(MessageType.TEXT);
        ret.setText(text);

        return ret;
    }

    public static Message makeTextMessage(User user, String text) {
        Message ret = new Message();

        ret.setUser(user);
        ret.setType(MessageType.TEXT);
        ret.setText(text);

        return ret;
    }

}
