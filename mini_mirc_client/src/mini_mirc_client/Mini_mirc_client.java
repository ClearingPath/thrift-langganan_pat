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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import mini_mirc_client.miniIRC;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Mini_mirc_client {

    /**
     * @param args the command line arguments
     */
    
    public static String username = "";
    public static String newMsg;
    public static boolean update = true;
    public static JSONArray allMsg = new JSONArray();
    
    public static void main(String[] args) {
        
	try{
	    final TTransport transport;
	    transport = new TSocket("localhost", 2121);
	    
	    TProtocol protocol = new TBinaryProtocol(transport);
	    final miniIRC.Client client = new miniIRC.Client (protocol);
	    
	    Runnable updateThread;
	    updateThread = new Runnable(){
		public void run(){
		    try{
			while (update){
			    Thread.sleep(5000);
			    synchronized(transport){
				transport.open();
				updateMsg(client);
				transport.close();
			    }
			}
		    } catch (Exception E){
			E.printStackTrace();
		    }
		}
	    };
	    
            new Thread(updateThread).start();
	    
	    perform(transport, client);
	    
	} catch (Exception E){
	    E.printStackTrace();
	}
    }
    
    public static void generateUname(){
	String commonUsername[] = {"Earthshaker", "Sven", "Tiny", "Kunkka", "Beastmaster", "DragonKnight", "Axe", "Pudge", "SandKing", "Slardar", "Tidehunter", "WraithKing", "Bloodseeker", "Windranger", "StormSpirit", "Lina", "ShadowFiend", "AntiMage", "PhantomAssassin"};
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
	int res;
	synchronized(transport){
	    transport.open();
	    res = client.regUser(username);
	    transport.close();
	    if (res == 0){
		System.out.println("Status: Registered user: " + username);
	    } else {
		System.out.println("Error: Unidentified error on register!");
		
		generateUname();
		transport.open();
		res = client.regUser(username);
		if (res == 0)
		    System.out.println("Status: Registered user: " + username);
		transport.close();
	    }
	}
	
	while (!exit){
	    System.out.print("> ");
	    String command = input.nextLine();
	    
	    String resSplit[] = command.split(" ", 2);
    	    String commandWord = resSplit[0].toUpperCase();
	    
	    synchronized(transport){
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
			if (res == 0 || res == 2){
			    System.out.println("Status: Joined channel: " + resSplit[1]);
			} else {
			    if (res == 1){
				System.out.println("Error: Channel " + resSplit[1] + " already joined!");
			    }
			    else {
				System.out.println("Error: code #" + res + " on channel join");
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
			    update = false;
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
		
		showMsg();
	    }
	}
	
    }
    
    public static void updateMsg(miniIRC.Client client) throws TException {
	String temp = client.regularUpdate(username);
	//System.out.println(temp);
	temp = temp.replaceAll("\\{\"msg\":\\[\\]\\}", "");
	if (!temp.isEmpty() && temp.length() > 6){
	    
	    try{
		//System.out.println(temp);
		JSONParser J = new JSONParser();
		JSONArray jArray = new JSONArray ();
		jArray = ((JSONArray) J.parse(temp));
		
		for (int i = 0; i < jArray.size(); i++){
		    JSONObject jeson = new JSONObject();
		    jeson = (JSONObject) jArray.get(i);
		    allMsg.add(jeson);
		}
	    } catch (Exception E) {E.printStackTrace();}
	}
    }
    
    public static void showMsg(){
	
	synchronized(allMsg){
	    System.out.println(allMsg.toJSONString());
	    for(int i = 0; i < allMsg.size(); i++){
		JSONObject temp = (JSONObject) allMsg.get(i);
		
		System.out.println("@" + temp.get("channel").toString() + " " + temp.get("username") + " " + temp.get("message"));
	    }
	    allMsg = new JSONArray();
	    
	}
    }
    
}
