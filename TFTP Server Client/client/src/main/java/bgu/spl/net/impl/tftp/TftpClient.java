package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TftpClient {

    private static Boolean recivedAck = false;
    private static Boolean recivedError = false;
    private static Object lock = new Object();
    private static Boolean inRRQ = false;
    private static Boolean inWRQ = false;
    private static File RRQfile =  null;
    private static File WRQfile =  null;
    private static byte[] WRQdataLeft = null;
    private static short WRQblockNum = -1;
    private static short WRQblockesLeft = -1;
    private static boolean terminate = false;
    private static boolean inDISC = false;
    private static  BlockingQueue<Object> actions = new LinkedBlockingQueue<>(1);


    private static BlockingQueue<byte[]> messagesQueue = new LinkedBlockingQueue<>();
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) {
        try (Socket sock = new Socket(args[0], 7777)){ // check if the args[0] is correct
            System.out.println("connected to the server");
            BufferedInputStream in =  new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
            
            while(!terminate){
 
                Scanner sc = new Scanner(System.in);
                String input = sc.nextLine();
                System.out.println("input is " + input );
                String[] words = input.split("\\s+");
                
                if(words.length > 0){
                    String command = words[0];
                    System.out.println(command );
                    String fileName;
                    String userName;
                    short opCode;
                    if(command.equals("RRQ")){ 
                        inRRQ = true;
                        fileName =  words[1];
                        opCode = 1;
                        Path currentPath = Paths.get("").toAbsolutePath();
                        String currentPathString = currentPath.toString();
                        String pathString = currentPathString + fileName;
                        Path path = Paths.get(pathString);
                        File file = new File(pathString);
                        if(file.exists()){
                            System.out.println("file already exists");
                        }
                        else{
                            try {
                                Files.createFile(path);
                            } catch (IOException e) {}
                            RRQfile = file;
                            //holder.WRQfileName.put(connectionId, fileNameString);
                        
                            byte[] fileNameBytes;
                            try {
                                fileNameBytes = fileName.getBytes("UTF8");
                                int msgSize = fileNameBytes.length + 3;
                                byte[] msg = new byte[msgSize];
                                msg[0] = ( byte ) ( opCode >> 8);
                                msg[1] =  ( byte ) ( opCode & 0xff );
                                for(int i = 2; i < msgSize -1; i++){
                                    msg[i] = fileNameBytes[i-2];
                                }
                                msg[msgSize - 1] = 0;
                                actions.put(new Object());
                                messagesQueue.offer(msg);
                                
                                recivedAck = false;
                                recivedError = false;
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        
                    }
                    if(command.equals("WRQ")){// check if the file exists
                        
                        fileName =  words[1];
                        opCode = 2;
                        byte[] fileNameBytes;
                        
                        Path currentPath = Paths.get("").toAbsolutePath();
                        String currentPathString = currentPath.toString();
                        String pathString = currentPathString + fileName;
                        Path path = Paths.get(pathString);
                        File file = new File(pathString);
                        if(file.exists()){
                            inWRQ = true; 
                            WRQfile = file;
                            try {
                                fileNameBytes = fileName.getBytes("UTF8");
                                int msgSize = fileNameBytes.length + 3;
                                byte[] msg = new byte[msgSize];
                                msg[0] = ( byte ) ( opCode >> 8);
                                msg[1] =  ( byte ) ( opCode & 0xff );
                                for(int i = 2; i < msgSize -1; i++){
                                    msg[i] = fileNameBytes[i-2];
                                }
                                msg[msgSize - 1] = 0;
                                actions.put(new Object());
                                messagesQueue.offer(msg);
                                recivedAck = false;
                                recivedError = false;
                                
                                FileInputStream fileInputStream = new FileInputStream(pathString);
                                int fileSize = fileInputStream.available();
                                int numOfBlocks = fileSize/512;
                                short packetSize = 512;
                                if(fileSize > 512 && fileSize % 512 > 0){
                                    numOfBlocks++;
                                }
                                if(fileSize <= 512){
                                    numOfBlocks = 1;
                                    packetSize = (short) fileSize;
                                }
                                WRQdataLeft = new byte[fileSize];
                                WRQblockesLeft = (short) numOfBlocks;
                                fileInputStream.read(WRQdataLeft);
                                WRQblockNum = 0;
                                
                                
                                // read the data from the file and send a data packet to the client
                                // wait for an ack packet with the same block num and then send the next data packet
                                

            
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else{
                            System.out.println("file not exists");
                        }
                    }
                    if(command.equals("DIRQ")){
                        opCode = 6;
                        byte[] msg = new byte []{( byte )  ( opCode >> 8) , ( byte ) ( opCode & 0xff ) };
                        actions.put(new Object());
                        messagesQueue.offer(msg);
                        recivedAck = false;
                        recivedError = false;

                    }
                    if(command.equals("LOGRQ")){
                        System.out.println("handeling LOGRQ" );
                        opCode = 7;
                        userName = words[1];
                        byte[] userNameBytes;
                        try {
                            userNameBytes = userName.getBytes("UTF8");
                            int msgSize = userNameBytes.length + 3;
                            byte[] msg = new byte[msgSize];
                            msg[0] = ( byte ) ( opCode >> 8);
                            msg[1] =  ( byte ) ( opCode & 0xff );
                            for(int i = 2; i < msgSize -1; i++){
                                msg[i] = userNameBytes[i-2];
                            }
                            msg[msgSize - 1] = 0;
                            actions.put(new Object());
                            messagesQueue.offer(msg);
                            System.out.println("adding " + msg.toString() + " to msgQueue" );
                            recivedAck = false;
                            recivedError = false;
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        

                    }

                    if(command.equals("DELRQ")){
                        fileName =  words[1];
                        opCode = 8;
                        byte[] fileNameBytes;
                        try {
                            fileNameBytes = fileName.getBytes("UTF8");
                            int msgSize = fileNameBytes.length + 3;
                            byte[] msg = new byte[msgSize];
                            msg[0] = ( byte ) ( opCode >> 8);
                            msg[1] =  ( byte ) ( opCode & 0xff );
                            for(int i = 2; i < msgSize -1; i++){
                                msg[i] = fileNameBytes[i-2];
                            }
                            msg[msgSize - 1] = 0;
                            actions.put(new Object());
                            messagesQueue.offer(msg);
                            recivedAck = false;
                            recivedError = false;
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(command.equals("DISC")){
                        System.out.println("handeling DISC" );
                        opCode = 10;
                        byte[] msg = new byte []{( byte )  ( opCode >> 8) , ( byte ) ( opCode & 0xff ) };
                        actions.put(new Object());
                        messagesQueue.offer(msg);
                        System.out.println("adding " + msg.toString() + " to msgQueue" );
                        inDISC = true;
                        recivedAck = false;
                        recivedError = false;

                    }

                }

  

                    
                    try {
                        if(!messagesQueue.isEmpty()){
                            
                            byte[] msg =messagesQueue.poll();
                            System.out.println("sending message to server: " + msg.toString());
                            out.write(msg); 
                            out.flush();
                        }
                    } catch (IOException e) {}
                    //out.newLine();
                    // synchronized(lock){
                    //     while(!recivedAck && !recivedError){
                    //         try {
                    //             System.out.println("waiting");
                    //             lock.wait();
                    //         } catch (InterruptedException e) {
                    //         }
                        
                    //     }
                    // }
                    
        

            Thread listeningThread = new Thread(){
            public void run(){
                int read;
                System.out.println("listening thread started");
                TftpEncoderDecoder encdec = new TftpEncoderDecoder();
                try {
                    while ((read = in.read()) >= 0) {
                        
                        byte[] nextMessage = encdec.decodeNextByte((byte) read);
                        if(nextMessage != null){
                            System.out.println("got msg from the server " + nextMessage.toString() );
                            
                                short opCode = (short) (((short) nextMessage[0]) << 8 | (short) (nextMessage[1]) & 0x00ff) ;
                                if(opCode == 4){//ACK
                                    System.out.println("handeling ACK from server");
                                    
                                    recivedAck = true;
                                    try {
                                        actions.take();
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    if(inDISC){
                                        terminate = true;
                                    }

                                    if(inWRQ){
                                        

                                        short blockNum = (short) (((short) nextMessage[2]) << 8 | (short) (nextMessage[3]) & 0x00ff) ;
                                        if(blockNum == WRQblockNum){
                                            WRQblockNum++;
                                            lock.notifyAll();
                                            short packetSize = 512;
                                            if(WRQdataLeft.length < 512){
                                                packetSize = (short) WRQdataLeft.length;
                                            }
                                            byte[] data = Arrays.copyOfRange(WRQdataLeft, 0, packetSize);
                                            short opDATA = 3;
                                            int msgSize = 6 + data.length;
                                            byte[] msg = new byte[msgSize];
                                            msg[0] = ( byte ) ( opDATA >> 8);
                                            msg[1] =  ( byte ) ( opDATA & 0xff );
                                            msg[2] = ( byte ) ( packetSize >> 8);
                                            msg[3] =  ( byte ) ( packetSize & 0xff );
                                            msg[4] = ( byte ) ( WRQblockNum >> 8);
                                            msg[5] =  ( byte ) ( WRQblockNum & 0xff );
                                            for(int i = 6; i < msgSize; i++){
                                                    msg[i] = data[i-6];
                                                }
                                            
                                            WRQblockesLeft--; 
                                            WRQdataLeft = Arrays.copyOfRange(WRQdataLeft, packetSize, WRQdataLeft.length);

                                            if(WRQblockesLeft == 0){
                                                inWRQ = false;
                                                WRQblockesLeft = -1;
                                                WRQblockNum = -1;
                                                WRQdataLeft = null;
                                                System.out.println("WRQ " + WRQfile.getName() + " completed");
                                                WRQfile = null;
                                            }

                                        }
                    
                                    }
                                    // synchronized(lock){
                                    //     lock.notifyAll();
                                    // }
                                    
                                    
                                }   

                                if(opCode == 5){//error
                                    System.out.println("hadeling error");
            
                                    recivedError = true;
                                    try {
                                        actions.take();
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    short errorCode = (short) (((short) nextMessage[2]) << 8 | (short) (nextMessage[3]) & 0x00ff) ;
                                    byte [] errorMsg = Arrays.copyOfRange(nextMessage, 4, nextMessage.length - 1);
                                    String errorMsgString = new String(errorMsg, StandardCharsets.UTF_8);
                                    System.out.println("Error " + errorCode + errorMsgString);
                                    // synchronized(lock){
                                    //     lock.notifyAll();
                                    // }
                                }

                                if(opCode == 3){// data
                                    System.out.println("Handaling data");
                                    short packetSize = (short) (((short) nextMessage[2]) << 8 | (short) (nextMessage[3]) & 0x00ff) ;
                                    short blockNum = (short) (((short) nextMessage[4]) << 8 | (short) (nextMessage[5]) & 0x00ff) ;
                                    byte [] data = Arrays.copyOfRange(nextMessage, 6, nextMessage.length - 1);
                                    if(inRRQ){
                                        if(RRQfile != null){
                                            try {
                                            FileOutputStream fileWriter = new FileOutputStream(RRQfile);
                                            fileWriter.write(data);
                                            fileWriter.close(); 
                                            } catch (IOException e) {}
                                            short four = 4;
                                            byte [] ACKmsg = new byte []{( byte ) ( four >> 8) , ( byte ) ( four & 0xff ) ,( byte ) ( blockNum >> 8) , ( byte ) ( blockNum & 0xff )};
                                            messagesQueue.offer(ACKmsg);
                                            if(packetSize < 512){
                                                
                                                inRRQ = false;
                                                System.out.println("RRQ "+ RRQfile.getName() + " completed");
                                                RRQfile = null;
                                            }
                                        }
                                        else{
                                            // send an error
                                        }
                                    }
                                    else{ // in DIRQ
                                        System.out.println("in DIRQ");
                                        short four = 4;
                                        byte [] ACKmsg = new byte []{( byte ) ( four >> 8) , ( byte ) ( four & 0xff ) ,( byte ) ( blockNum >> 8) , ( byte ) ( blockNum & 0xff )};
                                        messagesQueue.offer(ACKmsg);
                                        byte[] fileName;
                                        int from = 6;
                                        for(int i = 6; i < nextMessage.length; i++){
                                            if(nextMessage[i] == 0){
                                                fileName = Arrays.copyOfRange(nextMessage, from, i - 1);
                                                String fileNameString = new String(fileName, StandardCharsets.UTF_8);
                                                System.out.println(fileNameString);
                                                from = i + 1;
                                            }
                                        }
                                    }

                                }
                                
                                
                                
                                if(opCode == 9){// bcast
                                    String addOrDel = "add";
                                    if(nextMessage[2] == 0){
                                        addOrDel ="del";
                                    }
                                    byte [] fileName = Arrays.copyOfRange(nextMessage, 3, nextMessage.length - 1);
                                    String fileNameString = new String(fileName, StandardCharsets.UTF_8);
                                    System.out.println("BCAST " + addOrDel + " " + fileNameString);
                                    
                                }
                            
                            }
                            if(!messagesQueue.isEmpty()){
                                System.out.println("sending message to server");
                                out.write(messagesQueue.poll());
                                out.flush();
                            }
                        }
                    
                
                    } catch (IOException e) {}
                }
            };

        listeningThread.start();
        
        }  
     
        }
    
        catch (Exception e) {}
      
    return;
    
    }
    
}
