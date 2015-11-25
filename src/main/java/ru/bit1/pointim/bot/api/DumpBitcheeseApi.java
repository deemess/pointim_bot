package ru.bit1.pointim.bot.api;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dmitry on 24/11/15.
 */
public class DumpBitcheeseApi {

    final Logger log = Logger.getLogger(PointApi.class);
    final String charset = "UTF-8";
    final String CRLF = "\r\n"; // Line separator required by multipart/form-data.
    final String urlToConnect = "http://dump.bitcheese.net/upload-image?simple";

    public DumpBitcheeseApi() {

    }

    public String dumpImage(byte[] image, String type, String filename) {
        try {

            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.

            HttpURLConnection connection = (HttpURLConnection)new URL(urlToConnect).openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

            // Send tfile.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"").append(CRLF);
            writer.append("Content-Type: "+type).append(CRLF); // Text file itself must be saved in this charset!
            writer.append(CRLF).flush();
            output.write(image);
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();

            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();

            if(responseCode == 302 && connection.getHeaderFields().containsKey("Location")) {
                String imageurl = connection.getHeaderFields().get("Location").get(0);
                log.info("Successfully uploaded image:"+filename+" size: " + image.length + " to: " + imageurl);
                return imageurl;
            } else {
                log.error("Received "+responseCode+" from dump.bitcheese.net!");
            }

        } catch (IOException e) {
            log.error("Unable to upload image to dump.bitcheese.net!", e);
            return null;
        }

        log.error("Unable to upload image to dump.bitcheese.net!");
        return null;
    }
}
