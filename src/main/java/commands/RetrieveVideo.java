package commands;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.Videos;

import java.io.IOException;
import java.util.HashMap;

public class RetrieveVideo extends Command {

   public void execute() {
       HashMap<String, Object> props = parameters;

       Channel channel = (Channel) props.get("channel");
       JSONParser parser = new JSONParser();
       boolean getVideo = true;
       int id = 0;
       int channel_id = 0;
       try {
           JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
           System.out.println(body.toString());
           JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
           if(params.containsKey("id")){
                id = Integer.parseInt(params.get("id").toString());
                getVideo = true;
           }else{
               channel_id = Integer.parseInt(params.get("channel_id").toString());
               getVideo  = false;
           }
       } catch (ParseException e) {
           e.printStackTrace();
       }
       AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
       AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
       Envelope envelope = (Envelope) props.get("envelope");
       String response = "";
       if(getVideo) {
            response = Videos.getVideoByID(id);
       }else{
            response = Videos.getVideoChannelsByID(channel_id);
       }//Gets channels subscribed by id
       try {
           channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
