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
import org.apache.thrift.TException;
import org.bson.BSONObject;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 *
 * @author Jonathan
 */
public class miniIRCHandler implements miniIRC.Iface {

    @Override
    public int join(String username, String channelname) throws TException {
        return AddChannel(username,channelname);
    }

    @Override
    public int regUser(String username) throws TException {
        return AddUser(username);
    }

    @Override
    public int leave(String username, String channelname) throws TException {
        return DeleteChannelUser(channelname,username);
    }

    @Override
    public int exit(String username) throws TException {
        System.out.println(username + " exiting...");
//        return SoftDelete(username);
        return 0;
    }

    @Override
    public int message(String username, String channelname, String msg) throws TException {
        int ret=0;
        if (channelname.equalsIgnoreCase("*")){
            ret=PutMessageWild(username,msg);
        }
        else{
            ret=PutMessage(username,channelname,msg);
        }
        return ret;
    }

    @Override
    public String regularUpdate(String username) throws TException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String ret = GetMessage(username);
        return ret;
    }
    /**
     * Add a user to a channel (can check if the channel is a new channel)
     * @param Username
     * @param Channel
     * @return code
     */
    private int AddChannel(String Username, String Channel){
        int ret = 0;
        try {
            
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("channelCollection");
            BasicDBObject query = new BasicDBObject("username", Username)
                    .append("channel", Channel);
            
            DBCursor cursor = coll.find(query);
            
            try {
                if(cursor.hasNext()) {
                    ret = 1;
                    System.err.println(Username + " has joined Channel : " + Channel + "!");
                }
                else {
                    query = new BasicDBObject ("channel", Channel);
                    DBCursor cursor2 = coll.find(query);
                    try{
                        if (!cursor2.hasNext()){
                            ret = 2;
                        }
                    } finally {
                        cursor2.close();
                    }
                    BasicDBObject doc = new BasicDBObject ("username",Username)
                            .append("channel", Channel);
                    coll.insert(doc);
                    System.out.println(Username + " joined Channel : " + Channel);
                }
            } finally {
                cursor.close();
            }
            
        }   catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    /**
     * Delete a user from a channel (used in leave method)
     * @param Channel
     * @param Username
     * @return code
     */
    private int DeleteChannelUser(String Channel,String Username){
        int ret = 0;
        try {
            
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("channelCollection");
            BasicDBObject query = new BasicDBObject("username", Username)
                                       .append("channel",Channel);
            
            DBCursor cursor = coll.find(query);
            
            try {
                while(cursor.hasNext()) {
                    coll.remove(cursor.next());
                }
            } finally {
                cursor.close();
            }
            
        }   catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(Username + " has leaved Channel : " + Channel);
        return ret;
    }
    
    /**
     * Delete a user in all channel (used in cleaning)
     * @param Username
     * @return code
     */
    public int DeleteUserInChannel (String Username){
        int ret = 0;
        try {
            
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("channelCollection");
            BasicDBObject query = new BasicDBObject("username", Username);
            
            DBCursor cursor = coll.find(query);
            
            try {
                while(cursor.hasNext()) {
                    coll.remove(cursor.next());
                }
            } finally {
                cursor.close();
            }
            
        }   catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(Username + " has been deleted in Channel Collection!");
        return ret;
    }
    
    /**
     * Register a user to the server (used in RegUser)
     * @param username
     * @return code
     */
    private int AddUser (String username){
        int ret = 0;
        try {
            
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("activeUser");
            BasicDBObject query = new BasicDBObject("username", username);

            DBCursor cursor = coll.find(query);
            
            try {
                if(cursor.hasNext()) {
                    Date now = new Date();
                    long timestamp_now = now.getTime();
                    long treshold = timestamp_now - (1000 * 10); //10
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    Date time_temp = (Date) temp.get("timestamp");
                    long timestamp_temp = time_temp.getTime();
                    if (timestamp_temp < treshold){
                        ret=2;
                        System.out.println(username + " has joined back!");
                    }else {
                        ret = 1;
                    System.err.println(username + " has been used !");
                    }
                }
                else {
                    java.util.Date date= new java.util.Date();
                    BasicDBObject doc = new BasicDBObject ("username", username)
                            .append("timestamp", date);
                    coll.insert(doc);
                    System.out.println(username + " online !");
                }
            } finally {
                cursor.close();
            }
            
        }   catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    private int UpdateLastActive (String username){
        int ret = 0;
        try {
            
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("activeUser");
            BasicDBObject query = new BasicDBObject("username", username);

            DBCursor cursor = coll.find(query);
            
            try {
                if(cursor.hasNext()) {
                    DBObject temp = cursor.next();
                    java.util.Date date= new java.util.Date();
                    temp.put("timestamp",date);
                    coll.save(temp);
                }
                else {
                    java.util.Date date= new java.util.Date();
                    BasicDBObject doc = new BasicDBObject ("username", username)
                            .append("timestamp", date);
                    coll.insert(doc);
                    System.out.println(username + " online !");
                }
            } finally {
                cursor.close();
            }
            
        }   catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /**
     * Moved active user to a passive user (soon to be deleted)
     * @param username
     * @return code
     */
    public int SoftDelete (String username){
        int ret = 0;
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("activeUser");
            DBCollection coll2 = db.getCollection("passiveUser");
            BasicDBObject query = new BasicDBObject("username", username);
            
            DBCursor cursor = coll.find(query);
            
            try {
                if (cursor.hasNext()){
                    DBObject temp = cursor.next();
                    coll2.insert(temp);
                    coll.remove(temp);
                }
                else {
                    ret = 1;
                }
            } finally{
                cursor.close();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public String GetMessage(String username){
        String ret ="";
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("inbox");
            BasicDBObject query = new BasicDBObject("target", username);
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            DBCursor cursor = coll.find(query);
            
            try {
                while (cursor.hasNext()){
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    JSONObject sav = new JSONObject();
                    sav.put("target", temp.getString("target"));
                    sav.put("username", temp.getString("username"));
                    sav.put("channel", temp.getString("channel"));
                    sav.put("message", temp.getString("message"));
                    sav.put("timestamp", temp.getLong("timestamp"));
                    arr.add(sav);
                    coll.remove(temp);
                }
                obj.put("msg",arr);
                ret = obj.toJSONString();
            } finally{
                cursor.close();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        UpdateLastActive(username);
        return ret;
    }
    
    private int PutMessage(String username, String channelname, String msg){
        int ret = 0;
        try {    
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("inbox");
            DBCollection coll2 = db.getCollection("channelCollection");
            BasicDBObject query2 = new BasicDBObject("channel", channelname);
            BasicDBObject query = new BasicDBObject("channel", channelname)
                                        .append("username", username);
            DBCursor cursor = coll2.find(query);
            try{
                if (cursor.hasNext()){
                    DBCursor cursor2 = coll2.find(query2);
                    System.out.println("Got message from " + username);
                    try{
                        java.util.Date date= new java.util.Date();
                        while (cursor2.hasNext()){
                            ret = 1;
                            BasicDBObject temp = (BasicDBObject) cursor2.next();
                            String target = temp.get("username").toString();
                            BasicDBObject put = new BasicDBObject("target",target)
                                                .append("username", username)
                                                .append("channel", channelname)
                                                .append("message", msg)
                                                .append("timestamp", date.getTime());
                            coll.insert(put);
                            ret = 0;
                        }
                    } finally {
                        cursor2.close();
                    }
                }
                else{
                    ret=2;
                    System.out.println(username + " not registered to Channel : " + channelname);
                }
            }finally{
                cursor.close();
            }
            
            
        } catch (UnknownHostException ex) {
            ret = 1;
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    private int PutMessageWild(String username,String msg){
        int ret = 0;
        
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll2 = db.getCollection("channelCollection");
            BasicDBObject query = new BasicDBObject("username", username);
            System.out.println("Wild message appear from " + username + " !");
            DBCursor cursor = coll2.find(query);
            try{
                while(cursor.hasNext()){
                    ret = 1;
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    String channelname = temp.getString("channel");
                    ret = PutMessage(username,channelname,msg);
                }
            } finally {
                cursor.close();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        
        return 0;
    }
}
