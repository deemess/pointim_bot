package ru.bit1.pointim.bot;

import ru.bit1.pointim.bot.pojo.Command;
import ru.bit1.pointim.bot.pojo.Message;

import java.util.ArrayList;

/**
 * Created by dmitry on 04/07/15.
 */
public class CommandParser {

    public CommandParser() {
    }

    private boolean isSpace(char c) {
        if(c == ' ' || c == '\t' || c == '/')
            return true;
        return false;
    }

    public String getCommandText(Message msg) {
        StringBuilder sb = new StringBuilder();
        String text = msg.getText();
        boolean firstSpace = true;
        for(int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if(isSpace(c)) {
                if(firstSpace) {
                    continue;
                } else {
                    break;
                }
            }

            sb.append(c);
            firstSpace = false;
            if(c == '#')
                break;
        }
        return sb.toString();
    }

    private int skipCommandText(Message msg, int i) {
        String text = msg.getText();
        boolean firstSpace = true;
        while(i < text.length()) {
            char c = text.charAt(i);
            if(isSpace(c)) {
                i++;
                if(firstSpace) {
                    continue;
                } else {
                    break;
                }
            }
            firstSpace = false;
            i++;
            if(c == '#')
                break;
        }

        return i;
    }

    private int getNextArg(Message msg, int i, Command cmd) {
        String text = msg.getText();
        StringBuilder sb = new StringBuilder();
        boolean firstSpace = true;
        while(i < text.length()) {
            char c = text.charAt(i);
            if(isSpace(c)) {
                i++;
                if(firstSpace) {
                    continue;
                } else {
                    break;
                }
            }
            sb.append(c);
            firstSpace = false;
            i++;
        }

        if(sb.length() > 0)
            cmd.getArgs().add(sb.toString());
        return i;
    }

    private void parseArgs(Message msg, Command cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int argc = cmd.getArgc();
        int p = 0;
        p = skipCommandText(msg, p);
        if(argc == 0)
            return;

        while(args.size() < argc-1 && p < msg.getText().length()) {
            p = getNextArg(msg, p, cmd);
        }

        if(p < msg.getText().length())
            cmd.getArgs().add(msg.getText().substring(p, msg.getText().length()));
    }

    private void parseTextCommand(Message msg, Command cmd) {
        String commandText = getCommandText(msg).toLowerCase();
        Command.Type type = Command.getType(commandText);
        cmd.setType(type);
        cmd.setArgc(Command.getArgCountForType(type));

        if(cmd.getArgc() > 0) {
            parseArgs(msg, cmd);
        }
    }

    public void parseCommand(Message msg, Command cmd) {
        cmd.setArgc(0);
        cmd.setType(Command.Type.NOT_IMPLEMENTED);
        cmd.getArgs().clear();

        switch (msg.getType()) {
            case TEXT:
                parseTextCommand(msg, cmd);
                break;
            default:
                cmd.setType(Command.Type.NOT_IMPLEMENTED);
                cmd.setArgc(0);
                break;
        }
    }
}
