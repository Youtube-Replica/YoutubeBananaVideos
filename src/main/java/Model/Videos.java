package Model;

import Client.Client;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.Map;

public class Videos {
    static ArangoDB arangoDB;
    static Videos instance = new Videos();
    static String dbName = "scalable";
    static String collectionName = "video";

    private Videos(){
        arangoDB = new ArangoDB.Builder().build();
    }

    public static Videos getInstance(){
        return Videos.instance;
    }

    public void setDB(int i){
        arangoDB = new ArangoDB.Builder().maxConnections(i).build();
    }

    public static String getVideoByID(int id) {
        JSONObject videoObjectM = new JSONObject();
        try {
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            videoObjectM.put("VideoID",id);
            videoObjectM.put("ChannelID",myDocument.getAttribute("channel_id"));
            videoObjectM.put("Likes",myDocument.getAttribute("likes"));
            videoObjectM.put("Dislikes",myDocument.getAttribute("dislikes"));
            videoObjectM.put("Views",myDocument.getAttribute("views"));
            videoObjectM.put("Title",myDocument.getAttribute("title"));
            videoObjectM.put("Category",myDocument.getAttribute("category"));
            videoObjectM.put("Duration",myDocument.getAttribute("duration"));
            videoObjectM.put("Description",myDocument.getAttribute("description"));
            videoObjectM.put("Qualities",myDocument.getAttribute("qualities"));
            videoObjectM.put("Private",myDocument.getAttribute("private"));
            videoObjectM.put("url",myDocument.getAttribute("url"));
            videoObjectM.put("Date_Created",myDocument.getAttribute("date_created"));
            videoObjectM.put("Date_Modified",myDocument.getAttribute("date_modified"));
         } catch (ArangoDBException e) {
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        System.out.println(videoObjectM.toString());
        return videoObjectM.toString();
    }

    public static String getVideoChannelsByID(int channel_id) {
        JSONObject videoObject = new JSONObject();
        JSONArray videoArray = new JSONArray();
        String query = "FOR doc IN video\n" +
                 "        FILTER doc.`channel_id` like @value\n" +
                "        RETURN doc";
        Map<String, Object> bindVars = new MapBuilder().put("value", channel_id).get();

        ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                BaseDocument.class);

        if(cursor.hasNext()) {
            BaseDocument cursor2=null;
            for (; cursor.hasNext(); ) {
                cursor2 = cursor.next();
                JSONObject videoObjectM = new JSONObject();
                BaseDocument myDocument2 = arangoDB.db(dbName).collection("video").getDocument(cursor2.getKey(),
                        BaseDocument.class);
                int video_id= Integer.parseInt(cursor2.getKey());
                videoObjectM.put("VideoID", video_id);
                videoObjectM.put("ChannelID",myDocument2.getAttribute("channel_id"));
                videoObjectM.put("Likes",myDocument2.getAttribute("likes"));
                videoObjectM.put("Dislikes",myDocument2.getAttribute("dislikes"));
                videoObjectM.put("Views",myDocument2.getAttribute("views"));
                videoObjectM.put("Title",myDocument2.getAttribute("title"));
                videoObjectM.put("Category",myDocument2.getAttribute("category"));
                videoObjectM.put("Duration",myDocument2.getAttribute("duration"));
                videoObjectM.put("Description",myDocument2.getAttribute("description"));
                videoObjectM.put("Qualities",myDocument2.getAttribute("qualities"));
                videoObjectM.put("Private",myDocument2.getAttribute("private"));
                videoObjectM.put("url",myDocument2.getAttribute("url"));
                videoObjectM.put("Date_Created",myDocument2.getAttribute("date_created"));
                videoObjectM.put("Date_Modified",myDocument2.getAttribute("date_modified"));
                videoArray.add(videoObjectM);
            }
            videoObject.put("Channel  "+channel_id+" Videos:",videoArray);
        }
        else{
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> No Videos found for channel " + channel_id, CharsetUtil.UTF_8));
            videoObject.put("No videos found for channel  ",channel_id);
        }

        System.out.println(videoObject.toString());
        return videoObject.toString();

    }


    public static String postVideoByID(JSONObject params){
        BaseDocument myObject = new BaseDocument();
        myObject.addAttribute("channel_id", Integer.parseInt(params.get("channel_id").toString()));
        myObject.addAttribute("likes", new JSONArray());
        myObject.addAttribute("dislikes", new JSONArray());
        myObject.addAttribute("views", 0);
        myObject.addAttribute("title", params.get("title").toString());
        myObject.addAttribute("category", params.get("category").toString());
        myObject.addAttribute("duration", Integer.parseInt(params.get("duration").toString()));
        myObject.addAttribute("description", params.get("description").toString());
        myObject.addAttribute("qualities", params.get("qualities"));
        myObject.addAttribute("private", params.get("private"));
        myObject.addAttribute("date_created", new Timestamp(System.currentTimeMillis()));
        myObject.addAttribute("date_modified", new Timestamp(System.currentTimeMillis()));
        try{
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
        } catch (ArangoDBException e) {
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to create document " + e.getMessage(), CharsetUtil.UTF_8));
            System.err.println("Failed to create document. " + e.getMessage());
        }
        return "Document Created";
    }
    public static String deleteVideoByID(int id){
        try{
        arangoDB.db(dbName).collection(collectionName).deleteDocument(""+ id);
        }catch (ArangoDBException e){
            e.printStackTrace();
        }
        return "Deleted Video";
    }
    public static String updateVideo(JSONObject params){
        int id =  Integer.parseInt(params.get("id").toString());
        try {
        BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class);
        arangoDB.db(dbName).collection(collectionName).deleteDocument(""+id);
        myDocument.updateAttribute("channel_id",Integer.parseInt(params.get("channel_id").toString()));
        myDocument.updateAttribute("likes",params.get("likes"));
        myDocument.updateAttribute("dislikes",params.get("dislikes"));
        myDocument.updateAttribute("views",params.get("views"));
        myDocument.updateAttribute("title",params.get("title"));
        myDocument.updateAttribute("category",params.get("category"));
        myDocument.updateAttribute("duration",params.get("duration"));
        myDocument.updateAttribute("description",params.get("description"));
        myDocument.updateAttribute("qualities",params.get("qualities"));
        myDocument.updateAttribute("private", params.get("private"));
        myDocument.updateAttribute("date_modified", new Timestamp(System.currentTimeMillis()));
        arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument);
        }catch (ArangoDBException e){
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> ArangoDB exception " + e.getMessage(), CharsetUtil.UTF_8));
            e.printStackTrace();
        }
        return "Document "+ id + " updated";
    }
}
