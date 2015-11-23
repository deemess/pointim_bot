package ru.bit1.pointim.bot;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import ru.bit1.pointim.bot.api.PointApi;
import ru.bit1.pointim.bot.api.TelegramApi;
import ru.bit1.pointim.bot.pojo.Cache;
import ru.bit1.pointim.bot.pojo.Message;
import ru.bit1.pointim.bot.worker.CachePersister;
import ru.bit1.pointim.bot.worker.InboundHandler;
import ru.bit1.pointim.bot.worker.OutboundHandler;

import java.util.List;
import java.util.concurrent.*;

public class PointImBot implements Runnable {

    final static Logger log = Logger.getLogger(TelegramApi.class);

    private final TelegramApi telegram;
    private final PointApi point;
    private final Cache cache;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    private final ArrayBlockingQueue<Message> inqueue = new ArrayBlockingQueue<Message>(100);
    private final ArrayBlockingQueue<Message> outqueue = new ArrayBlockingQueue<Message>(100);
    private boolean run = true;

    public PointImBot(String telegramToken) {
        this.cache = new Cache(this);
        this.telegram = new TelegramApi(this.cache, telegramToken);
        this.point = new PointApi();
        this.executorService.scheduleAtFixedRate(new InboundHandler(this), 1, 1, TimeUnit.MILLISECONDS);
        this.executorService.scheduleAtFixedRate(new OutboundHandler(this), 1, 1, TimeUnit.MILLISECONDS);
        this.executorService.scheduleAtFixedRate(new CachePersister(this.cache), 10000, 10000, TimeUnit.MILLISECONDS);
    }

    public void run() {
        do {
            List<Message> messages = this.telegram.getUpdates();
            boolean put;
            for (Message msg : messages) {
                put = false;
                while (!put) {
                    try {
                        inqueue.put(msg);
                        put = true;
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error(e);
                run = false;
            }
        } while (run);
        executorService.shutdownNow();
    }

    public Message getInbound() {
        try {
            return this.inqueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void putOutbound(Message msg) {
        boolean put = false;
        while(!put) {
            try {
                this.outqueue.put(msg);
                put = true;
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    public Message getOutbound() {
        try {
            return this.outqueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public TelegramApi getTelegram() {
        return telegram;
    }

    public PointApi getPoint() {
        return point;
    }

    public static void main(String[] args) {
        try {
            //BasicConfigurator.configure();
            DOMConfigurator.configure("log4j.xml");
            Thread telegramThread  = new Thread(new PointImBot(args[0]));
            telegramThread.start();
            telegramThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
