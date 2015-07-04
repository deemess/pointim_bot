package ru.bit1.pointim.bot.worker;

import ru.bit1.pointim.bot.PointImBot;
import ru.bit1.pointim.bot.pojo.Message;

/**
 * Created by dmitry on 04/07/15.
 */
public class OutboundHandler implements Runnable {

    private final PointImBot bot;

    public OutboundHandler(PointImBot bot) {
        this.bot = bot;
    }

    public void run() {
        Message msg = bot.getOutbound();
        if(msg == null)
            return;

        bot.getTelegram().sendMessage(msg);
    }
}
