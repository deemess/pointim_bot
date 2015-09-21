package ru.bit1.pointim.bot.worker;

import org.apache.log4j.Logger;
import ru.bit1.pointim.bot.CommandParser;
import ru.bit1.pointim.bot.PointImBot;
import ru.bit1.pointim.bot.api.TelegramApi;
import ru.bit1.pointim.bot.pojo.Command;
import ru.bit1.pointim.bot.pojo.Message;

import java.util.ArrayList;
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

            parser.parseCommand(msg, cmd);
            log.debug("Parsed command: " + cmd);

            if (cmd.getArgc() != cmd.getArgs().size() || cmd.isDescribeOnly()) {
                bot.putOutbound(Message.makeTextResponce(msg, Command.describe(cmd)));
                return;
            }

            String error = null;
            switch (cmd.getType()) {
                case LOGIN:
                    error = bot.getPoint().login(msg.getUser(), cmd.getArgs().get(0), cmd.getArgs().get(1));
                    bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Logged in." : error));
                    break;
                case LOGOUT:
                    error = bot.getPoint().logout(msg.getUser());
                    bot.putOutbound(Message.makeTextResponce(msg, error == null ? "Logged out." : error));
                    break;
                case POST:
                    error = bot.getPoint().post(msg.getUser(), Arrays.asList(cmd.getArgs().get(0).split(",")), cmd.getArgs().get(1), false);
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
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("Error in inbound queue!", e);
        }
    }
}
