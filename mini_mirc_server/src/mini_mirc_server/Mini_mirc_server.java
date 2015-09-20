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
                            cleaner();
                            Thread.sleep(1000 * 60 * 5);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Mini_mirc_server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
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
                if (hard_clean){
                    HardClean();
                }
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
            
                        for (int i =0; i < 4; i++){
                            DBCursor cursor2 = coll[i].find(query);

                            try {
                                while (cursor.hasNext()){
                                    DBObject temp2 = cursor.next();
                                    coll[i].remove(temp);
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
}
