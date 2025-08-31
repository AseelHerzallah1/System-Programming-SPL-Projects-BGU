package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> activeConnections = new ConcurrentHashMap<>();


    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        // TODO Auto-generated method stub
        activeConnections.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        // TODO Auto-generated method stub
        if(activeConnections.containsKey(connectionId)){
            activeConnections.get(connectionId).send(msg);
            return true;
        } 
        else{
            return false;
        }  
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO Auto-generated method stub
        if(activeConnections.contains(connectionId)){
            activeConnections.remove(connectionId);
        }
    }
    
}
