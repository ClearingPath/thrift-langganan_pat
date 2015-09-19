/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini_mirc_server;

import org.apache.thrift.server.TServer; 
import org.apache.thrift.server.TServer.Args; 
import org.apache.thrift.server.TSimpleServer; 
import org.apache.thrift.transport.TServerSocket; 
import org.apache.thrift.transport.TServerTransport; 

/**
 *
 * @author Jonathan
 */
public class Mini_mirc_server {
    
    public static miniIRCHandler handler;
    
    public static miniIRC.Processor processor;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            handler = new miniIRCHandler();
            processor = new miniIRC.Processor(handler);
            
            Runnable simple = new Runnable() {
                public void run(){
                    simple(processor);
                }
            };
            
            new Thread(simple).start();
        } catch (Exception x){
            x.printStackTrace();
        }
    }
    
    public static void simple(miniIRC.Processor processor){
        try {
            TServerTransport serverTransport = new TServerSocket(2121);
            TServer server =new TSimpleServer(new Args(serverTransport).processor(processor));
            
            System.out.println("Starting simple server...");
            server.serve();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
