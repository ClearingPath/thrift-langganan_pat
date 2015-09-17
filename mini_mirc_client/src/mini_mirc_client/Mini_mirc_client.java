/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini_mirc_client;

/**
 *
 * @author Jonathan
 */

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import mini_mirc_client.miniIRC;

public class Mini_mirc_client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
	try{
	    TTransport transport;
	    transport = new TSocket("localhost", 2121);
	    transport.open();
	    
	    TProtocol protocol = new TBinaryProtocol(transport);
	    miniIRC.Client client = new miniIRC.Client (protocol);
	    
	    perform(client);
	    
	    transport.close();
	    
	} catch (Exception E){
	    E.printStackTrace();
	}
    }
    
    private static void perform (miniIRC.Client client ) throws TException {
	String uname = "sampleuser";
	client.regUser(uname);
	System.out.println("Registered user: " + uname);
    }
    
    
    
    
}
