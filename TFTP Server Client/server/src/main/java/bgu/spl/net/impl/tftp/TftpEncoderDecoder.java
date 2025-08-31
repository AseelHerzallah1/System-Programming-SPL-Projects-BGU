package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder
    
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short opCode = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this - done
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if(len < 1){ 
            pushByte(nextByte);
            return null; //not a line yet
        }
        
        if(len == 1){
            pushByte(nextByte);
            byte [] opCode_bytes = new byte []{bytes[0] , bytes[1]};
            opCode = (short) (((short) opCode_bytes[0]) << 8 | (short) (opCode_bytes[1]) & 0x00ff) ;
            if(opCode == 10 || opCode == 6){
                byte[] msg = Arrays.copyOfRange(bytes, 0, len); 
                len = 0;
                return msg;
            }
            else{
                return null; //not a line yet
            }
            
        }
        if(opCode == 1 || opCode == 2 || opCode == 9 || opCode == 5 || opCode == 7 || opCode == 8){
            if(nextByte == (byte)0){
                pushByte(nextByte);
                byte[] msg = Arrays.copyOfRange(bytes, 0, len); 
                len = 0;
                return msg;
            }
            else{
                pushByte(nextByte);
                return null; //not a line yet
            }
        }

        if(opCode == 3){
            if(len >= 4){
                byte [] packetSize = new byte []{bytes[2] , bytes[3]};
                short packetSize_short = (short) (((short) packetSize[0]) << 8 | (short) (packetSize[1]) & 0x00ff) ; 
                if(len == packetSize_short + 5){ //TODO: check this
                    pushByte(nextByte);
                    byte[] msg = Arrays.copyOfRange(bytes, 0, len); 
                    len = 0;
                    return msg;
                }
                else{
                    pushByte(nextByte);
                    return null; //not a line yet
                }
            }
            else{
                pushByte(nextByte);
                return null; //not a line yet
            }
        }

        if(opCode == 4){
            if(len == 3){
                pushByte(nextByte);
                byte[] msg = Arrays.copyOfRange(bytes, 0, len); 
                len = 0;
                return msg;
            }
            else{
                pushByte(nextByte);
                return null; //not a line yet
            }
        }

        else{
            pushByte(nextByte);
            return null;
        }
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    // private String popString() {
    //     //notice that we explicitly requesting that the string will be decoded from UTF-8
    //     //this is not actually required as it is the default encoding in java.
    //     byte[] result = new byte[len](bytes, 0, len, StandardCharsets.UTF_8);
    //     len = 0;
    //     return result;
    // }

    @Override
    public byte[] encode(byte[] message) {
        //TODO: implement this
        return message;
    }
}