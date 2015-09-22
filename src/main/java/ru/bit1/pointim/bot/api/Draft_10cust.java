package ru.bit1.pointim.bot.api;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dmitry on 05/07/15.
 */
public class Draft_10cust extends Draft_10 {

    @Override
    public List<ByteBuffer> createHandshake(Handshakedata handshakedata, WebSocket.Role ownrole, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        if(handshakedata instanceof ClientHandshake) {
            bui.append("GET ");
            bui.append(((ClientHandshake)handshakedata).getResourceDescriptor());
            bui.append(" HTTP/1.1");
        } else {
            if(!(handshakedata instanceof ServerHandshake)) {
                throw new RuntimeException("unknow role");
            }

            bui.append("HTTP/1.1 101 " + ((ServerHandshake)handshakedata).getHttpStatusMessage());
        }

        bui.append("\r\n");
        Iterator it = handshakedata.iterateHttpFields();

        while(it.hasNext()) {
            String httpheader = (String)it.next();
            String content = handshakedata.getFieldValue(httpheader);
            bui.append(httpheader);
            bui.append(": ");
            bui.append(content);
            bui.append("\r\n");
        }

        bui.append("\r\n");
        byte[] httpheader1 = Charsetfunctions.asciiBytes(bui.toString());
        byte[] content1 = withcontent?handshakedata.getContent():null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content1 == null?0:content1.length) + httpheader1.length);
        bytebuffer.put(httpheader1);
        if(content1 != null) {
            bytebuffer.put(content1);
        }

        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }


}
