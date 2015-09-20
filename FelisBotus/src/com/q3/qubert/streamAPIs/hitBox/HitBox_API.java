package com.q3.qubert.streamAPIs.hitBox;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.q3.qubert.streamAPIs.JsonHelper;


public class HitBox_API {

	public static Gson gson = new Gson();

	public static HitBox_Stream getStream(String channelName){
		try {
			JsonObject userJsonObject = JsonHelper.readJsonFromUrl("http://api.hitbox.tv/user/"+channelName);
			JsonObject channelJsonObject = JsonHelper.readJsonFromUrl("http://api.hitbox.tv/media/live/"+channelName);
			HitBox_Stream stream = new HitBox_Stream();
			
			if (userJsonObject.getAsJsonPrimitive("is_live").getAsInt() != 0){
				stream.setOnline(true);
				stream.load(channelJsonObject);	
			}
			else
			{
				stream.setOnline(false);
			}
			return stream;
		} catch (Exception e) {
			System.out.println(e+"Caused by getStream in HitBox_Api");
			e.printStackTrace();
		}
		return null;
	}
}


