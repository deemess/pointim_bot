package ru.bit1.pointim.bot.pojo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dmitry on 04/07/15.
 * Modified by netmoose on 23/11/15
 */
public class Command {

    public enum Type {
        HELP,
        LOGIN,
        LOGOUT,
        POST,
        PRIVATE_POST,
        SHOW_POST,
        SHOW_IMAGES,
        PING,
        NOT_IMPLEMENTED,
        UNKNOWN,
        COMMENT,
        RECOMMEND
    }

    private static final HashMap<Type, String> description = new HashMap<>();
    private static final HashMap<String, Type> typeMap = new HashMap<>();
    private static final HashMap<Type, Integer> argcMap = new HashMap<>();
    private static final ArrayList<Type> describeOnly = new ArrayList<>();

    {
        typeMap.put("start", Type.HELP);
        typeMap.put("help", Type.HELP);
        typeMap.put("about", Type.HELP);
        typeMap.put("?", Type.HELP);
        typeMap.put("ping", Type.PING);
        typeMap.put("login", Type.LOGIN);
        typeMap.put("logout", Type.LOGOUT);
        typeMap.put("post", Type.POST);
        typeMap.put("#", Type.SHOW_POST);
        typeMap.put("get", Type.SHOW_POST);
        typeMap.put("ppost", Type.PRIVATE_POST);
        typeMap.put("pm", Type.PRIVATE_POST);
        typeMap.put("p", Type.PRIVATE_POST);
        typeMap.put("images", Type.SHOW_IMAGES);

        describeOnly.add(Type.HELP);
        describeOnly.add(Type.PING);
        describeOnly.add(Type.UNKNOWN);
        describeOnly.add(Type.NOT_IMPLEMENTED);

        argcMap.put(Type.HELP, 0);
        argcMap.put(Type.LOGIN, 2);
        argcMap.put(Type.LOGOUT, 0);
        argcMap.put(Type.POST, 2);
        argcMap.put(Type.PRIVATE_POST, 2);
        argcMap.put(Type.SHOW_POST, 1);
        argcMap.put(Type.SHOW_IMAGES, 0);
        argcMap.put(Type.PING, 0);
        argcMap.put(Type.NOT_IMPLEMENTED, 0);
        argcMap.put(Type.UNKNOWN, 0);

        description.put(Type.PING, "pong");
        description.put(Type.LOGIN, "Login to point.im.\nUsage: login <user> <password>\n"+
                "You must be logged in with username and password to which you registered for the microblog service.\n"+
                "If you are not registered - do it in http://point.im/register");
        description.put(Type.LOGOUT, "Logout from point.im.\nUsage: logout\n"+"and goodbye my sweet prince.");
        description.put(Type.POST, "Create new post.\nUsage: post <tag1,tag2,tag3> <text>\n"+
                "Tags are written without spaces and without <> \n"+
                "(this is just for the convenience of the show)\n"+
                "and then a mandatory space and then a text of your message.");
        description.put(Type.PRIVATE_POST, "Create new private post. It will see only you or the specified @nicknames recipients.\n"+
                "Usage: ppost <tag1,tag2,tag3> [@nicknames] <text>\n" +
                "Usage: pm <tag1,tag2,tag3> [@nicknames] <text>\n" +
                "Usage: p <tag1,tag2,tag3> [@nicknames] <text>\n"+
                "[@nicknames] - do not required");
        description.put(Type.SHOW_POST, "Show post content.\nUsage: #<post_id>\nUsage: get <post_id>");
        description.put(Type.SHOW_IMAGES, "Show pending images.\n");
        description.put(Type.NOT_IMPLEMENTED, "This feauture currently not implemented.\n");
        description.put(Type.UNKNOWN, "Unknown command.");
        description.put(Type.HELP, "This is bot for point.im microblog service.\n"+
                "Currently implemented commands:\n"+
                "help - this help.\n"+
                "login - log in user profile on point.im. For more help send this command without parameters.\n"+
                "logout - log out and off sending messages from this bot.\n"+
                "post - send message. For more help send this command without parameters.\n"+
                "get - get above message from microblog feed. For more help send this command without parameters.\n"+
                "pm - send personal messages to user. For more help send this command without parameters.\n"+
                "images - show pending images for post.\n");
    }

    public static Type getType(String commandText) {
        if(typeMap.containsKey(commandText))
            return typeMap.get(commandText);

        return Type.UNKNOWN;
    }

    public static int getArgCountForType(Type type) {
        return argcMap.get(type);
    }

    public static String describe(Command cmd) {
        return description.get(cmd.getType());
    }

    private Type type;
    private int argc;
    private ArrayList<String> args = new ArrayList<String>();

    public Command() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getArgc() {
        return argc;
    }

    public void setArgc(int argc) {
        this.argc = argc;
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<String> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String arg : args) {
            sb.append(arg);
            sb.append("; ");
        }
        return "{type="+type+"; argc="+argc+"; args={"+sb.toString()+"}; }";
    }

    public boolean isDescribeOnly() {
        return describeOnly.contains(type);
    }
}
