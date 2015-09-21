/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini_mirc_client;

/**
 *
 * @author Feli
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
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
    public static boolean update = true;
    public static JSONArray allMsg = new JSONArray();
    
    public static void main(String[] args) {
        
	try{
	    TTransport transport;
	    transport = new TSocket("localhost", 2121);
	    
	    TProtocol protocol = new TBinaryProtocol(transport);
	    miniIRC.Client client = new miniIRC.Client (protocol);
	    
	    Runnable updateThread;
	    updateThread = new Runnable(){
		public void run(){
		    try{
			while (update){
			    Thread.sleep(3000);
			    
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
	System.out.println("# Generated new username: " + uname);
	
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
		System.out.println("# Registered user: " + username);
	    } else {
		System.out.println("!!!: Unidentified error on register!");
		
		generateUname();
		transport.open();
		res = client.regUser(username);
		if (res == 0)
		    System.out.println("# Registered user: " + username);
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
			System.out.println("# Registering user: " + resSplit[1]);
			res = client.regUser(resSplit[1]);

			if (res == 0){
			    System.out.println("# Registered user: " + resSplit[1]);
			    username = resSplit[1];
			}
			else {
			    System.out.println("!!!: Unidentified error on register!");
			}
			break;
		    case "/JOIN": 
			System.out.println("# Checking channel: " + resSplit[1]);

			res = client.join(username, resSplit[1]);
			if (res == 0 || res == 2){
			    System.out.println("# Joined channel: " + resSplit[1]);
			} else {
			    if (res == 1){
				System.out.println("!!!: Channel " + resSplit[1] + " already joined!");
			    }
			    else {
				System.out.println("!!!: code #" + res + " on channel join");
			    }
			}

			break;
		    case "/LEAVE":
			if (username.isEmpty()){
			    System.out.println("!!!: Unregistered user");
			} else {
			    System.out.println("# " + username + " exiting channel " + resSplit[1]);
			    res = client.leave(username, resSplit[1]);
			    if (res == 0) System.out.println("# Success"); 
			    else System.out.println("!!!: Channel error!");
			}
			break;

		    case "/EXIT":
			System.out.println("# " + username + " closing...");

			res = client.exit(username);
			if (res == 0) {
			    System.out.println("# Exit success"); 
			    username = "";
			    exit = true;
			    update = false;
			}
			else {
			    System.out.println("!!!: Channel error! Error code #" + res);
			}
			break;

		    default:
			if (resSplit[0].startsWith("@")){ // message to channel 
			    res = client.message(username, resSplit[0].substring(1), resSplit[1]); 
			    if (res == 0) {
				System.out.println("# Msg to " + resSplit[0].substring(1) + " sent"); 
			    } else if (res == 2){
				System.out.println("!!!: Not member of channel " + resSplit[0].substring(1));
			    }
			} else {
			    
			    res = client.message(username, "*", command);
			    if (res == 0) {
				System.out.println("# Msg to all channels sent"); 
			    } else {
				System.out.println("!!!: Connection problemo?");
			    }
			    
			}
			break;
		}
		transport.close();
		
	    }
	    showMsg();
	}
	
    }
    
    public static void updateMsg(miniIRC.Client client) throws TException {
	String temp = client.regularUpdate(username);
	temp = temp.replaceAll("\\{\"msg\":\\[\\]\\}", "");
	if (!temp.isEmpty() && temp.length() > 6){
	    
	    try{
		JSONParser J = new JSONParser();
		
		JSONObject jeson = new JSONObject();
		jeson = (JSONObject) J.parse(temp);
		allMsg.add(jeson);
		
	    } catch (Exception E) {E.printStackTrace();}
	}
    }
    
    public static void showMsg(){
	try {
	    synchronized(allMsg){
		
		if (!allMsg.isEmpty() && allMsg.size() > 0){
		    for(int i = 0; i < allMsg.size(); i++){
			JSONObject temp = (JSONObject) allMsg.get(i);
			JSONArray tempArr = (JSONArray) temp.get("msg");

			for(int j = 0; j < tempArr.size(); j++){
			    temp = (JSONObject) tempArr.get(j);
			    SimpleDateFormat formatDate = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			    
			    
			    Date sendDat = new Date();
			    sendDat.setTime((long) temp.get("timestamp"));
			    
			    System.out.println(">> [" + temp.get("channel").toString() 
				+ "] [" + temp.get("username") 
				+ "] " + temp.get("message")
				+ " || " + formatDate.format(sendDat));
			}
		    }
		}
		allMsg.clear();
	    }
	} catch (Exception E) { E.printStackTrace(); }
    }
    
}
