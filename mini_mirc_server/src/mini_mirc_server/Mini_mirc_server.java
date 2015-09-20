/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mini_mirc_server;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            
            Runnable cleaner = new Runnable() {
                public void run(){
                    try {
                        Thread.sleep(1000 * 60 * 5);
                        while (true){
                            System.out.println("cleaner running...");
                            cleaner();
                            Thread.sleep(1000 * 60 * 5);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            
            new Thread(cleaner).start();
            
            Runnable ultClean = new Runnable() {
                public void run(){
                    try {
                        while (true){
                            UltimateClean();
                            Thread.sleep(1000 * 60 * 60);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            
            new Thread(ultClean).start();
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
    
    public static void cleaner(){
        try {
            boolean hard_clean = false;
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("activeUser");

            DBCursor cursor = coll.find();
            try{
                Date now = new Date();
                long timestamp_now = now.getTime();
                long treshold = timestamp_now - (1000 * 60 * 5); //5 minutes
                while(cursor.hasNext()){
                    hard_clean = true;
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    Date time_temp = (Date) temp.get("timestamp");
                    long timestamp_temp = time_temp.getTime();
                    if (timestamp_temp < treshold){
                        String target = temp.getString("username");
                        handler.SoftDelete(target);
                    }
                }
                HardClean();
            } finally{
                cursor.close();
            }
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void HardClean(){
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll[] = new DBCollection[4];
            coll[0] = db.getCollection("channelCollection");
            coll[1] = db.getCollection("inbox");    
            coll[2] = db.getCollection("activeUser");
            coll[3] = db.getCollection("passiveUser");
            
            DBCursor cursor = coll[3].find();
            
            try {
                while (cursor.hasNext()){
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    String username = temp.getString("username");
                    BasicDBObject query = new BasicDBObject("username", username);
                    System.out.println("cleaning " + username);
                    for (int i =0; i < 4; i++){
                        DBCursor cursor2 = coll[i].find(query);

                        try {
                            while (cursor2.hasNext()){
                                DBObject temp2 = cursor2.next();
                                coll[i].remove(temp2);
                            }
                        } finally{
                            cursor2.close();
                        }
                    }
                }
            } finally{
                cursor.close();
            }
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * ONLY IF NO USER IS IN THE ACTIVE OR PASIVE COLLECTION;getting server to clean state
     */
    private static void UltimateClean(){
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll[] = new DBCollection[4];
            coll[0] = db.getCollection("activeUser");
            coll[1] = db.getCollection("passiveUser");
            coll[2] = db.getCollection("channelCollection");
            coll[3] = db.getCollection("inbox"); 
            
            DBCursor cursor[] = new DBCursor[4];
            cursor[0] = coll[0].find();
            cursor[1] = coll[1].find();
            cursor[2] = coll[2].find();
            cursor[3] = coll[3].find();
            try{
                if (!cursor[0].hasNext() && !cursor[1].hasNext() && cursor[2].hasNext() && cursor[3].hasNext()){
                    System.out.println("SYSTEM RESTARTING with ULTIMATE CLEANING !");
                    for(int i = 2; i <= 3; i ++){
                        coll[i].drop();
                    }
                    System.out.println("RESTART COMPLETE!");
                }
            } finally{
                cursor[0].close();
                cursor[1].close();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
