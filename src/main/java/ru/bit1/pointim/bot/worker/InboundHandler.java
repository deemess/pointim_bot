package ru.bit1.pointim.bot.worker;

import org.apache.log4j.Logger;
import ru.bit1.pointim.bot.CommandParser;
import ru.bit1.pointim.bot.PointImBot;
import ru.bit1.pointim.bot.pojo.Command;
import ru.bit1.pointim.bot.pojo.Message;

import java.util.Arrays;

/**
 * Created by dmitry on 04/07/15.
 */
public class InboundHandler implements Runnable {

    final static Logger log = Logger.getLogger(InboundHandler.class);

    private final PointImBot bot;
    private final CommandParser parser =  new CommandParser();
    private final Command cmd = new Command();

    public InboundHandler(PointImBot bot) {
        this.bot = bot;
    }

    public void run() {
        try {
            Message msg = this.bot.getInbound();
            if (msg == null)
                return;

            switch (msg.getType()) {
                case TEXT:
                case COMMENT:
                    handleTextMessage(msg);
                    break;
                case IMAGE:
                    handlePhotoMessage(msg);
                    break;
            }

        } catch (Exception e) {
            log.error("Error in inbound queue!", e);
        }
    }

    private void handlePhotoMessage(Message msg) {
        //get telegram photo file
        byte[] file = bot.getTelegram().getFile(msg.getFileId());
        //put file in the dump bitcheese
        String savedImage = bot.getBitcheese().dumpImage(file, "image/jpeg"/*URLConnection.guessContentTypeFromName()*/,Long.toHexString(System.currentTimeMillis()));
        msg.getUser().getPendingImages().add(savedImage);
        bot.putOutbound(Message.makeTextResponce(msg, "Image saved to " + savedImage + "\nWill be used in the next post automatically.\nTo check pending images type command: images "));
    }

    private void handleTextMessage(Message msg) {
        parser.parseCommand(msg, cmd);
        if(cmd.getType() != Command.Type.LOGIN)
            log.debug("Parsed command: " + cmd);

        if (cmd.getArgc() != cmd.getArgs().size() || cmd.isDescribeOnly()) {
            bot.putOutbound(Message.makeTextResponce(msg, Command.describe(cmd)));
            return;
        }

        String error = null;
        String text = null;
        switch (cmd.getType()) {
            case LOGIN:
                error = bot.getPoint().login(msg.getUser(), cmd.getArgs().get(0), cmd.getArgs().get(1));
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Logged in." : error));
                if(error == null) {
                    msg.getUser().enableNotifications(bot);
                }
                break;
            case LOGOUT:
                error = bot.getPoint().logout(msg.getUser());
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Logged out." : error));
                if(error == null) {
                    msg.getUser().disableNotifications();
                }
                break;
            case POST:
                text = cmd.getArgs().get(1);
                if(msg.getUser().getPendingImages().size() > 0) {//add pending images to the post
                    text += "\n";
                    for(String imageurl : msg.getUser().getPendingImages()) {
                        text += imageurl + "\n";
                    }
                }
                error = bot.getPoint().post(msg.getUser(), Arrays.asList(cmd.getArgs().get(0).split(",")), text, false);
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Post sent." : error));
                break;
            case PRIVATE_POST:
                error = bot.getPoint().post(msg.getUser(), Arrays.asList(cmd.getArgs().get(0).split(",")), cmd.getArgs().get(1), true);
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Post sent." : error));
                break;
            case SHOW_POST:
                error = bot.getPoint().getpost(msg.getUser(), cmd.getArgs().get(0));
                bot.putOutbound(Message.makeTextResponce(msg, error));
                break;
            case COMMENT:
                error = bot.getPoint().comment(msg.getUser(), cmd.getArgs().get(0), cmd.getArgs().get(1), cmd.getArgc() == 3 ? cmd.getArgs().get(2) : null);
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Commend added." : error));
                break;
            case RECOMMEND:
                error = bot.getPoint().recommend(msg.getUser(), cmd.getArgs().get(0), cmd.getArgs().get(1), cmd.getArgc() == 3 ? cmd.getArgs().get(2):null);
                bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Recommended." : error));
                break;
            case SHOW_IMAGES:
                StringBuilder sb = new StringBuilder();
                sb.append("Current pending images for next post:\n");
                for(String imageurl : msg.getUser().getPendingImages()) {
                    sb.append(imageurl);
                    sb.append("\n");
                }
                bot.putOutbound(Message.makeTextResponce(msg, sb.toString()));
                break;
            default:
                break;
        }
    }
}
