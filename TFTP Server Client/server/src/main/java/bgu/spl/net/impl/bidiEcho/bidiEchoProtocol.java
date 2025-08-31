package bgu.spl.net.impl.bidiEcho;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}

public class bidiEchoProtocol implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate = false;
    private int connectionId;
    Connections<String> connections;

    @Override
    public void start(int connectionId, Connections<String> connections){
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        holder.ids_login.put(connectionId, true);
    }


    @Override
    public void process(String msg) {
        shouldTerminate = "bye".equals(msg);
        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        for (Integer k : holder.ids_login.keySet()){
            connections.send(k, createEcho(msg));
        }
    }

    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    @Override
    public boolean shouldTerminate() {
        this.connections.disconnect(this.connectionId);
        holder.ids_login.remove(this.connectionId);
        return shouldTerminate;
    }
}
