package bgu.spl.net.impl.bidiEcho;

import bgu.spl.net.impl.bidiEcho.bidiEchoProtocol;
import bgu.spl.net.impl.bidiEcho.bidiEncoderDecoder;
import bgu.spl.net.srv.Server;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.Closeable;
import java.util.function.Supplier;


public class bidiEchoServer {
        public static void main(String[] args) {

        // // you can use any server... 
        // Server.threadPerClient(
        //         7777, //port
        //         () -> new BidiEchoProtocol(), //protocol factory
        //         bidiEncoderDecoder::new //message encoder decoder factory
        // ).serve();

    }
}


