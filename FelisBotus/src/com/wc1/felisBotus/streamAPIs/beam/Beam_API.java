package com.wc1.felisBotus.streamAPIs.beam;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wc1.felisBotus.streamAPIs.JsonHelper;

public class Beam_API {

	public static Gson gson = new Gson();
	
	public static Beam_Stream getStream(String channelName) {
		try {
			JsonObject userJsonObject = JsonHelper.readJsonFromUrl("https://beam.pro/api/v1/channels/"+channelName);
			Beam_Stream stream = new Beam_Stream();
			
			if (userJsonObject.getAsJsonPrimitive("online").getAsBoolean()){
				stream.setOnline(true);
				stream.load(userJsonObject);	
			}
			else
			{
				stream.setOnline(false);
			}
			return stream;
		} catch (Exception e) {
			System.out.println(e+"Caused by getStream in Beam_Api");
			e.printStackTrace();
		}
		return null;
	}
}
