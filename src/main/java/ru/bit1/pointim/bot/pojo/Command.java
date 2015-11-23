package ru.bit1.pointim.bot.pojo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dmitry on 04/07/15.
 */
public class Command {

    public enum Type {
        HELP,
        LOGIN,
        LOGOUT,
        POST,
        PRIVATE_POST,
        SHOW_POST,
        PING,
        NOT_IMPLEMENTED,
        UNKNOWN,
        COMMENT,
        RECOMMEND
    }

    private static final HashMap<Type, String> description = new HashMap<Type, String>();
    private static final HashMap<String, Type> typeMap = new HashMap<String, Type>();
    private static final HashMap<Type, Integer> argcMap = new HashMap<Type, Integer>();
    private static final ArrayList<Type> describeOnly = new ArrayList<Type>();

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
        argcMap.put(Type.PING, 0);
        argcMap.put(Type.NOT_IMPLEMENTED, 0);
        argcMap.put(Type.UNKNOWN, 0);

        description.put(Type.PING, "pong");
        description.put(Type.LOGIN, "Login to point.im.\nUsage: login <user> <password>");
        description.put(Type.LOGOUT, "Logout to point.im.\nUsage: logout");
        description.put(Type.POST, "Create new post.\nUsage: post <tag1,tag2,tag3> <text>");
        description.put(Type.PRIVATE_POST, "Create new private post.\n"+
                "Usage: ppost <tag1,tag2,tag3> <text>\n" +
                "Usage: pm <tag1,tag2,tag3> <text>\n" +
                "Usage: p <tag1,tag2,tag3> <text>");
        description.put(Type.SHOW_POST, "Show post content.\nUsage: #<post_id>\nUsage: get <post_id>");
        description.put(Type.NOT_IMPLEMENTED, "This feauture currently not implemented.\n");
        description.put(Type.UNKNOWN, "Unknown command.");
        description.put(Type.HELP, "This is bot for point.im microblog service.\nCurrently implemented commands:\nhelp\nlogin\nlogout\npost\nget\npm\n");
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
