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
       
        return 0;
    }

    @Override
    public String regularUpdate(String username) throws TException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String ret = "";
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
                    ret = 1;
                    System.err.println(username + " has been used !");
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
                    System.out.println(cursor.next().get(username) + " has been soft deleted!");
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
    
    public String GetMessages(String username){
        String ret ="";
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB( "mirc" );
            DBCollection coll = db.getCollection("inbox");
            BasicDBObject query = new BasicDBObject("username", username);
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            DBCursor cursor = coll.find(query);
            
            try {
                while (cursor.hasNext()){
                    DBObject temp = cursor.next();
                    arr.add(temp);
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
            BasicDBObject query = new BasicDBObject("channel", channelname);
            DBCursor cursor = coll2.find(query);
            
            try{
                java.util.Date date= new java.util.Date();
                while (cursor.hasNext()){
                    BasicDBObject temp = (BasicDBObject) cursor.next();
                    String target = temp.get("username").toString();
                    BasicDBObject put = new BasicDBObject("target",target)
                                        .append("usermane", username)
                                        .append("channel", channelname)
                                        .append("message", msg)
                                        .append("timestamp", date);
                    coll.insert(put);
                }
            } finally {
                cursor.close();
            }
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(miniIRCHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
