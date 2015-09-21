package ru.bit1.pointim.bot.worker;

import org.apache.log4j.Logger;
import ru.bit1.pointim.bot.pojo.Cache;

import java.io.*;

/**
 * Created by dmitry on 04/07/15.
 */
public class CachePersister implements Runnable {
    final static Logger log = Logger.getLogger(CachePersister.class);

    private final Cache cache;

    public CachePersister(Cache cache) {
        this.cache = cache;
        restoreCache();
    }

    private void restoreCache() {
        log.info("Restoring cache from file.");
        try {
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream("cache.bin"), "UTF-8"));
            try {
                cache.restore(in);
            } finally {
                in.close();
            }
            log.info("Done.");
        } catch (Exception e) {
            log.error("Unable to restore cache from file!", e);
        }
    }

    @Override
    public void run() {
        if(!cache.isChanged())
            return;

        log.info("Saving cache to file.");
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cache.bin"), "UTF-8"));
            try {
                cache.dump(out);
            } finally {
                out.close();
            }
            log.info("Done.");
        } catch (IOException e) {
            log.error("Unable to save cache to file!", e);
        }

    }
}
