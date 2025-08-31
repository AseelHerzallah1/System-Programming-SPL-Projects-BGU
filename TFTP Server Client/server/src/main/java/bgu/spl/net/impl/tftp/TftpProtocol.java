package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


class holder{
    static ConcurrentHashMap<String, Integer> names_login = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, String> ids_login = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, File> WRQfile = new ConcurrentHashMap<>();
    //static File WRQfile;
    static ConcurrentHashMap<Integer, String> WRQfileName = new ConcurrentHashMap<>();
    //static String WRQfileName;
    static ConcurrentHashMap<Integer, byte[]> RRQdataLeft = new ConcurrentHashMap<>();
    //static byte[] RRQdataLeft;
    static ConcurrentHashMap<Integer, Short> RRQblockNum = new ConcurrentHashMap<>();
    //static short RRQblockNum;
    static ConcurrentHashMap<Integer, Short> RRQblockesLeft = new ConcurrentHashMap<>();
    //static short RRQblockesLeft;
    static ConcurrentHashMap<Integer, byte[]> DIRQdataLeft = new ConcurrentHashMap<>();
    //static byte[] RRQdataLeft;
    static ConcurrentHashMap<Integer, Short> DIRQblockNum = new ConcurrentHashMap<>();
    //static short RRQblockNum;
    static ConcurrentHashMap<Integer, Short> DIRQblockesLeft = new ConcurrentHashMap<>();
    
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate = false;
    private int connectionId;
    Connections<byte[]> connections;


    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        //holder.ids_login.put(connectionId, true);
        //holder.WRQfile = null;
        //holder.WRQfileName = null;
        //holder.RRQblockesLeft = -1;
        //holder.RRQblockNum = -1;
    }

    @Override
    public void process(byte[] message) {
        // TODO implement this
        byte [] opCode = new byte []{message[0] , message[1]};
        short opCode_short = (short) (((short) message[0]) << 8 | (short) (message[1]) & 0x00ff);
         
        if(opCode_short != 7){ // checks if the user is not logged in yet 
            if(!holder.ids_login.containsKey(connectionId)){
                sendERROR((short) 6, "User not logged in");
                return;
            }
            
        }

        if(opCode_short == 1){ // RRQ
            
            byte [] fileName = Arrays.copyOfRange(message, 2, message.length - 1);
            String fileNameString = new String(fileName, StandardCharsets.UTF_8);
            Path currentPath = Paths.get("").toAbsolutePath();
            String currentPathString = currentPath.toString();
            String pathString = currentPathString + "\\Flies\\" + fileNameString; // TODO: check if works

            try {
                FileInputStream file = new FileInputStream(pathString);
                int fileSize = file.available();
                int numOfBlocks = fileSize/512;
                short packetSize = 512;
                if(fileSize > 512 && fileSize % 512 > 0){
                    numOfBlocks++;
                }
                if(fileSize <= 512){
                    numOfBlocks = 1;
                    packetSize = (short) fileSize;
                }
                byte[] dataLeft = new byte[fileSize];
                holder.RRQblockesLeft.put(connectionId, (short) numOfBlocks);
                file.read(dataLeft);
                holder.RRQdataLeft.put(connectionId, dataLeft);
                Short one = new Short((short)1);
                holder.RRQblockNum.put(connectionId, one);
                sendDATA(packetSize, holder.RRQblockNum.get(connectionId) , Arrays.copyOfRange(dataLeft, 0, packetSize));
                numOfBlocks--;
                holder.RRQblockesLeft.remove(connectionId);
                holder.RRQblockesLeft.put(connectionId, (short)numOfBlocks);
                dataLeft = Arrays.copyOfRange(dataLeft, packetSize, dataLeft.length);
                holder.RRQdataLeft.remove(connectionId);
                holder.RRQdataLeft.put(connectionId, dataLeft);
                // todo: add file.close
                // read the data from the file and send a data packet to the client
                // wait for an ack packet with the same block num and then send the next data packet
                

            } catch (IOException e) {}
            // send a data packet to the client with the file, using connections.send
            
        }

        if(opCode_short == 4){ //ACK
            if(holder.RRQblockNum.containsKey(connectionId)){ 
                byte [] blockNum = new byte []{message[2] , message[3]};
                short blockNum_short = (short) (((short) blockNum[0]) << 8 | (short) (blockNum[1]) & 0x00ff);
                byte[] dataLeft = holder.RRQdataLeft.get(connectionId);
                if(blockNum_short == holder.RRQblockNum.get(connectionId)){
                    if(holder.RRQblockesLeft.get(connectionId) == 0){
                        holder.RRQblockNum.remove(connectionId);
                        holder.RRQblockesLeft.remove(connectionId);
                        holder.RRQdataLeft.remove(connectionId);
                        
                    }
                    else{
                        Short currBlock = holder.RRQblockNum.get(connectionId);
                        currBlock++;
                        holder.RRQblockNum.remove(connectionId);
                        holder.RRQblockNum.put(connectionId, currBlock);
                        short packetSize = 512;
                        if(dataLeft.length <  512){
                            packetSize = (short) dataLeft.length;
                        }
                        sendDATA(packetSize, currBlock , Arrays.copyOfRange( dataLeft, 0, packetSize));
                        Short numOfBlocks = holder.RRQblockesLeft.get(connectionId);
                        numOfBlocks--;
                        holder.RRQblockesLeft.remove(connectionId);
                        holder.RRQblockesLeft.put(connectionId, numOfBlocks);
                        dataLeft = Arrays.copyOfRange(dataLeft, packetSize, dataLeft.length);
                        holder.RRQdataLeft.remove(connectionId);
                        holder.RRQdataLeft.put(connectionId, dataLeft);
                    }
                }
                //else return an error
            }
            else if(holder.DIRQblockNum.containsKey(connectionId)){
                byte [] blockNum = new byte []{message[2] , message[3]};
                short blockNum_short = (short) (((short) blockNum[0]) << 8 | (short) (blockNum[1]) & 0x00ff);
                byte[] dataLeft = holder.DIRQdataLeft.get(connectionId);
                if(blockNum_short == holder.DIRQblockNum.get(connectionId)){
                    if(holder.DIRQblockesLeft.get(connectionId) == 0){
                        holder.DIRQblockNum.remove(connectionId);
                        holder.DIRQblockesLeft.remove(connectionId);
                        holder.DIRQdataLeft.remove(connectionId);
                        
                    }
                    else{
                        Short currBlock = holder.DIRQblockNum.get(connectionId);
                        currBlock++;
                        holder.DIRQblockNum.remove(connectionId);
                        holder.DIRQblockNum.put(connectionId, currBlock);
                        short packetSize = 512;
                        if(dataLeft.length <  512){
                            packetSize = (short) dataLeft.length;
                        }
                        sendDATA(packetSize, currBlock , Arrays.copyOfRange( dataLeft, 0, packetSize));
                        Short numOfBlocks = holder.DIRQblockesLeft.get(connectionId);
                        numOfBlocks--;
                        holder.DIRQblockesLeft.remove(connectionId);
                        holder.DIRQblockesLeft.put(connectionId, numOfBlocks);
                        dataLeft = Arrays.copyOfRange(dataLeft, packetSize, dataLeft.length);
                        holder.DIRQdataLeft.remove(connectionId);
                        holder.DIRQdataLeft.put(connectionId, dataLeft);
                    }
                }
            }
        }

        if(opCode_short == 2){ // WRQ
            byte [] fileName = Arrays.copyOfRange(message, 2, message.length - 1);
            String fileNameString = new String(fileName, StandardCharsets.UTF_8);

            Path currentPath = Paths.get("").toAbsolutePath();
            String currentPathString = currentPath.toString();
            String pathString = currentPathString + "\\Flies\\" + fileNameString;
            Path path = Paths.get(pathString);
            File file = new File(pathString);
            if(file.exists()){
                sendERROR((short) 5, "File already exists");
            }
            else{
                try {
                    Files.createFile(path);
                } catch (IOException e) {}
                sendACK((short) 0);;
                holder.WRQfile.put(connectionId, file);
                holder.WRQfileName.put(connectionId, fileNameString);
            }
        }

        if(opCode_short == 3){ //DATA packet
            byte [] packetSize = new byte []{message[2] , message[3]};
            short packetSize_short = (short) (((short) packetSize[0]) << 8 | (short) (packetSize[1]) & 0x00ff);
            byte [] blockNumber = new byte []{message[4] , message[5]};
            short blockNumber_short = (short) (((short) blockNumber[0]) << 8 | (short) (blockNumber[1]) & 0x00ff);
            int end = message.length - 1;
            if(packetSize_short == 0){
                end = message.length;
            }
            byte[] data = Arrays.copyOfRange(message, 6, end );
            if(holder.WRQfile.containsKey(connectionId)){
                try {
                    FileOutputStream fileWriter = new FileOutputStream(holder.WRQfile.get(connectionId));
                    fileWriter.write(data);
                    fileWriter.close(); // check if correct
                } catch (IOException e) {}
                sendACK(blockNumber_short);
                if(packetSize_short < 512){
                    if(holder.WRQfileName.containsKey(connectionId)){
                        holder.WRQfile.remove(connectionId);
                        // String msgString = "WRQ " + holder.WRQfileName.get(connectionId) + " complete";
                        // byte[] msg;
                        // try {
                        //     msg = msgString.getBytes("UTF8");
                        //     connections.send(connectionId, msg );
                        // } catch (UnsupportedEncodingException e) {} 
                        byte[] fileName = holder.WRQfileName.get(connectionId).getBytes();
                        sendBCAST((byte)1, fileName);
                        holder.WRQfileName.remove(connectionId);
                    }
                    else{
                        sendERROR((short) 0, "WRQfileName is null");
                    }
                    
                }
            }
            else{
                sendERROR((short) 0, "WRQfile is null");
            }
        }

            
        

        if(opCode_short == 7){ //LOGRQ
            byte [] name = Arrays.copyOfRange(message, 2, message.length - 1);
            String nameString = new String(name, StandardCharsets.UTF_8);
            if(holder.names_login.containsKey(nameString)){
                //return an error packet
                sendERROR((short) 7, "User already logged in");
            }
            else{
                holder.names_login.put(nameString,connectionId);
                holder.ids_login.put(connectionId, nameString);
                //return an ACK packet 
                sendACK((short)0);
            }
        }

        if(opCode_short == 8){ //DELRQ
            
            byte [] fileName = Arrays.copyOfRange(message, 2, message.length - 1);
            String fileNameString = new String(fileName, StandardCharsets.UTF_8);
            Path currentPath = Paths.get("").toAbsolutePath();
            String currentPathString = currentPath.toString();
            String pathString = currentPathString + "\\Flies\\" + fileNameString;            
            File file = new File(pathString);
            if(file.exists()){
                boolean deleted = file.delete();
                if(deleted){
                    sendACK((short)0);
                    //send a BCAST message to all the clients that a file was deleted
                    sendBCAST((byte)0, fileName);
                }
            }
            else{
                //send an error packet - file not found
                sendERROR((short)1, "File not found");
            }
        }

        if(opCode_short == 6){ // DIRQ
            Path currentPath = Paths.get("").toAbsolutePath();
            String currentPathString = currentPath.toString();
            String pathString = currentPathString + "\\Flies";
            File folder = new File(pathString);
            File[] listOfFiles = folder.listFiles();
            String[] filesNames = new String[listOfFiles.length];
            for (int i = 0 ; i <listOfFiles.length; i++){
                filesNames[i] = listOfFiles[i].getName();
            }
            try {
                byte[] data = {};
                for(int i= 0; i < filesNames.length ; i++){
                    
                    byte[] fileName = filesNames[i].getBytes("UTF8");
                    int oldLength = data.length;
                    data = Arrays.copyOfRange(data, 0, fileName.length + data.length + 1);
                    for(int j = oldLength ; j < data.length - 1; j++){
                        data[j] = fileName[j - oldLength];
                    }
                    data[data.length-1] =(byte) 0;
                    if(i == filesNames.length - 1){
                        data = Arrays.copyOfRange(data, 0 , data.length - 1);
                    }
                }
                int numOfBlocks = data.length/512;
                short packetSize = 512;
                if(data.length > 512 && data.length % 512 > 0){
                    numOfBlocks++;
                }
                if(data.length <= 512){
                    numOfBlocks = 1;
                    packetSize = (short) data.length;
                }
                holder.DIRQblockesLeft.put(connectionId, (short) numOfBlocks);
                
                holder.DIRQdataLeft.put(connectionId, data);
                Short one = new Short((short)1);
                holder.DIRQblockNum.put(connectionId, one);
                sendDATA(packetSize, holder.DIRQblockNum.get(connectionId) , Arrays.copyOfRange(data, 0, packetSize));
                numOfBlocks--;
                holder.DIRQblockesLeft.remove(connectionId);
                holder.DIRQblockesLeft.put(connectionId, (short)numOfBlocks);
                data = Arrays.copyOfRange(data, packetSize, data.length);
                holder.DIRQdataLeft.remove(connectionId);
                holder.DIRQdataLeft.put(connectionId, data);
                //sendDATA((short)data.length , (short)1, data);
            } catch (UnsupportedEncodingException e) {}


        }




        if(opCode_short == 10){//DISC
            String name = holder.ids_login.get(connectionId);
            holder.names_login.remove(name);
            holder.ids_login.remove(connectionId); //check if we need to remove the client form the names_login
            sendACK((short) 0);
        } 

    }

    private void sendACK(short blockNum){
        short ack = 4;
        byte [] ack_bytes = new byte []{( byte ) ( ack >> 8) , ( byte ) ( ack & 0xff ) ,( byte ) ( blockNum >> 8) , ( byte ) ( blockNum & 0xff )};
        connections.send(connectionId, ack_bytes);
    }

    private void sendBCAST(byte addedOrDeleted, byte[] fileName){
        short opBCAST = 9;
        int msgSize = 4 + fileName.length;
        byte[] msg = new byte[msgSize];
        msg[0] = ( byte ) ( opBCAST >> 8);
        msg[1] =  ( byte ) ( opBCAST & 0xff );
        msg[2] = addedOrDeleted;
        for(int i = 3; i < msgSize -1; i++){
            msg[i] = fileName[i-3];
        }
        msg[msgSize - 1] = 0;
        Set<Integer> idSet = holder.ids_login.keySet();
        for (Integer id : idSet) {
            connections.send(id, msg);
        }
        
    }

    private void sendERROR(short errorCode, String errorMsg){
        try {
            byte[] errorMsgBytes = errorMsg.getBytes("UTF8");
            short opERROR = 5;
            int msgSize = 5 + errorMsgBytes.length;
            byte[] msg = new byte[msgSize];
            msg[0] = ( byte ) ( opERROR >> 8);
            msg[1] =  ( byte ) ( opERROR & 0xff );
            msg[2] = ( byte ) ( errorCode >> 8);
            msg[3] =  ( byte ) ( errorCode & 0xff );
            for(int i = 4; i < msgSize -1; i++){
                msg[i] = errorMsgBytes[i-4];
            }
            msg[msgSize - 1] = 0;
            connections.send(connectionId, msg);
        } catch (UnsupportedEncodingException e) {}
    }

    private void sendDATA(short packetSize, short blockNum, byte[] data){
        short opDATA = 3;
        int msgSize = 6 + data.length;
        byte[] msg = new byte[msgSize];
        msg[0] = ( byte ) ( opDATA >> 8);
        msg[1] =  ( byte ) ( opDATA & 0xff );
        msg[2] = ( byte ) ( packetSize >> 8);
        msg[3] =  ( byte ) ( packetSize & 0xff );
        msg[4] = ( byte ) ( blockNum >> 8);
        msg[5] =  ( byte ) ( blockNum & 0xff );
        for(int i = 6; i < msgSize; i++){
                msg[i] = data[i-6];
            }
        connections.send(connectionId, msg);   
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        //this.connections.disconnect(this.connectionId);
        //holder.ids_login.remove(this.connectionId);
        //holder.names_login
        //shouldTerminate = true; ???
        return shouldTerminate;
    } 


    
}
