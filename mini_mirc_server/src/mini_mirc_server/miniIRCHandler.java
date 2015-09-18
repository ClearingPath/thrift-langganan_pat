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
import com.mongodb.MongoClient;
import org.apache.thrift.TException;

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
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;
    }

    @Override
    public int exit(String username) throws TException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;
    }

    @Override
    public int message(String username, String channelname, String msg) throws TException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;
    }

    @Override
    public String regularUpdate(String username) throws TException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String ret = "";
        return ret;
    }
    
    private int AddChannel(String Username, String Channel){
        int ret = 0;
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
               BasicDBObject doc = new BasicDBObject ("username",Username)
                                              .append("Channel", Channel);
               coll.insert(doc);
               //Extend : Give message if new channel created
               System.out.println(Username + " joined Channel : " + Channel);
           }
        } finally {
           cursor.close();
        }
        return ret;
    }
    
    private int AddUser (String username){
        int ret = 0;
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
        return ret;
    }
}
