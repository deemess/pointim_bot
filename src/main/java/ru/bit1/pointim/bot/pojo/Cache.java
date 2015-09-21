package ru.bit1.pointim.bot.pojo;

import ru.bit1.pointim.bot.PointImBot;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by dmitry on 04/07/15.
 */
public class Cache implements IObjectUpdateListener {

    private final HashMap<Long, User> users = new HashMap<Long, User>();
    private final Object lock = new Object();
    private boolean changed = false;
    private final PointImBot bot;

    public Cache(PointImBot bot) {
        this.bot = bot;
    }

    public PointImBot getBot() {
        return bot;
    }

    public User getUser(Long chatid, String from) {
        synchronized (lock) {
            if(users.containsKey(chatid)) {
                return users.get(chatid);
            } else {
                User user = new User(this);
                user.setName(from);
                user.setChatid(chatid);

                users.put(chatid, user);
                changed = true;
                return user;
            }

        }
    }

    public void objectChanged() {
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public void dump(Writer w) throws IOException {
        synchronized (lock) {
            for(Map.Entry<Long,User> entry : users.entrySet() ) {
                w.write(User.serialize(entry.getValue())+"\n");
            }

            changed = false;
        }
    }

    public void restore(Reader r) throws UnsupportedEncodingException, URISyntaxException {
        synchronized (lock) {
            users.clear();

            Scanner scanner = new Scanner(r);
            while(scanner.hasNextLine()) {
                User user = User.deserialize(scanner.nextLine(), this, getBot());
                users.put(user.getChatid(), user);
            }

            changed = false;
        }
    }


}
