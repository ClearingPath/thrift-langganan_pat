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

import java.util.Scanner;
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
    
    private static void perform (miniIRC.Client client) throws TException {
	boolean exit = false;
	Scanner input = new Scanner(System.in);
	String command = input.nextLine();
	String username = "";
	
	while (!exit){
	    //parse command
	    String resSplit[] = command.split(" ", 2);
    	    String commandWord = resSplit[0].toUpperCase();
	    switch (commandWord){
		case "/NICK":
		    System.out.println("Status: Registering user: " + resSplit[1]);
		    if (client.regUser(resSplit[1]) == 0){
			System.out.println("Status: Registered user: " + resSplit[1]);
			username = resSplit[1];
		    }
		    else {
			System.out.println("Error: Unidentified error on register!");
			
		    }
		    break;
		case "/JOIN":
		    System.out.println("Status: Checking  channel: " + resSplit[1]);
		    int res = client.join(username, resSplit[1]);
		    if (res == 0){
			System.out.println("");
		    } else {
			if (res == 1){
			    System.out.println("Error: Channel " + resSplit[1] + " already joined!");
			}
		    }
		    
		    break;
		case "/LEAVE":
		    break;
		case "/EXIT":
		    exit = true;
		    break;
		    
	    }
	    
	}
    }
    
    
    
    
}
