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
    
    public static String username = "";
    public static String newMsg;
    public static void main(String[] args) {
        
	try{
	    TTransport transport;
	    transport = new TSocket("localhost", 2121);
	    
	    TProtocol protocol = new TBinaryProtocol(transport);
	    final miniIRC.Client client = new miniIRC.Client (protocol);
	    
	    Runnable updateThread;
	    updateThread = new Runnable(){
		public void run(){
		    try{
			updateMsg(client);
			Thread.sleep(1000);
		    } catch (Exception E){
			E.printStackTrace();
		    }
		}
	    };
	    
            //new Thread(updateThread).start();
	    
	    perform(transport, client);
	    
	} catch (Exception E){
	    E.printStackTrace();
	}
    }
    
    public static void generateUname(){
	String commonUsername[] = {"Earthshaker", "Sven", "Tiny", "Kunkka", "Beastmaster", "DragonKnight", "Axe", "Pudge", "SandKing", "Slardar", "Tidehunter", "WraithKing"};
	String uname;
	
	int randIndex = (int) Math.round(Math.random() * (commonUsername.length - 1));
	int randEnd = (int) (Math.random() * 999);
	uname = commonUsername[randIndex] + randEnd;
	System.out.println("Status: Generated new username: " + uname);
	
	username = uname;
    }
    
    private static void perform (TTransport transport, miniIRC.Client client) throws TException {
    //private static void perform () throws TException {
	boolean exit = false;
	Scanner input = new Scanner(System.in);
	
	generateUname();
	//auto-regis
	transport.open();
	int res = client.regUser(username);
	transport.close();
	
	if (res == 0){
	    System.out.println("Status: Registered user: " + username);
	} else {
	    System.out.println("Error: Unidentified error on register!");
	}
	
	while (!exit){
	    System.out.print("> ");
	    String command = input.nextLine();
	    
	    String resSplit[] = command.split(" ", 2);
    	    String commandWord = resSplit[0].toUpperCase();
	    
	    transport.open();
	    
	    switch (commandWord){
		case "/NICK":
		    System.out.println("Status: Registering user: " + resSplit[1]);
		    res = client.regUser(resSplit[1]);
		    
		    if (res == 0){
			System.out.println("Status: Registered user: " + resSplit[1]);
			username = resSplit[1];
		    }
		    else {
			System.out.println("Error: Unidentified error on register!");
		    }
		    break;
		case "/JOIN": 
		    System.out.println("Status: Checking channel: " + resSplit[1]);
		    
		    res = client.join(username, resSplit[1]);
		    if (res == 0){
			System.out.println("Status: Joined channel: " + resSplit[1]);
		    } else {
			if (res == 1){
			    System.out.println("Error: Channel " + resSplit[1] + " already joined!");
			}
		    }
		    
		    break;
		case "/LEAVE":
		    if (username.isEmpty()){
			System.out.println("Error: Unregistered user");
		    } else {
			System.out.println("Status: " + username + " exiting channel " + resSplit[1]);
			res = client.leave(username, resSplit[1]);
			if (res == 0) System.out.println("Status: Success"); 
			else System.out.println("Error: Channel error!");
		    }
		    break;
		    
		case "/EXIT":
		    System.out.println("Status: " + username + " closing...");
		    
		    res = client.exit(username);
		    
		    if (res == 0) {
			System.out.println("Status: Exit success"); 
			username = "";
			exit = true;
		    }
		    else {
			System.out.println("Error: Channel error! Error code #" + res);
		    }
		    break;
		    
		default:
		    if (resSplit[0].startsWith("@")){ // message
			res = client.message(username, resSplit[0].substring(1), resSplit[1]);
			if (res == 0) {
			    System.out.println("Status: Msg to " + resSplit[0].substring(1) + " sent"); 
			} 
		    } else {
			System.out.println("Error: Wrong command " + resSplit[0]);
		    }
		    break;
	    }
	    transport.close();
	
	}
	
    }
    
    public static void updateMsg(miniIRC.Client client) throws TException {
	newMsg = client.regularUpdate(username);
	System.out.println("Got update!");
	System.out.println(newMsg);
    }
    
}
