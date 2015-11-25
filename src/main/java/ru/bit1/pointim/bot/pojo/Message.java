package ru.bit1.pointim.bot.pojo;

/**
 * Created by dmitry on 04/07/15.
 */
public class Message {

    public enum MessageType {
        TEXT,
        COMMENT,
        IMAGE,
        AUDIO,
        DOCUMENT
    }

    private String text;
    private MessageType type;
    private User user;
    private String replyTo;
    private String fileId;

    public Message() {

    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getReplyTo() {
        return replyTo;
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
        return "{type="+type+"; user="+user+"; text="+text+"; reply_to="+replyTo+"; file_id="+fileId+"; }";
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
