package com.q3.qubert.streamAPIs.twitch;

import com.google.gson.JsonObject;
import com.q3.qubert.streamAPIs.JsonHelper;
/**
 * <pre>
 * Api Class used for getting a Json response from Twitch.tv using their API.
 * Twitch API info - https://github.com/justintv/Twitch-API 
 * Google Gson Lib - http://mvnrepository.com/artifact/com.google.code.gson/gson 
 * Google Gson javadocs - https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/Gson.html 
 * Gson user guide - https://sites.google.com/site/gson/gson-user-guide
 *</pre>
 * @author JennyLeeP
 *
 */
public class Twitch_API {

	
	static JsonObject liveObject = new JsonObject();
/**
 * Method that trys to get a Json response from a given URL, catches error.
 * Gets if a stream is live from first URL, if Stream is live uses second URL to retrieve info about stream.
 * If stream is ionline - sets online(true) else online(false)
 * @param channelname
 * @return
 */
	public static Twitch_Stream getStream(String channelname){
		try{
			//Twitch_Stream live = new Twitch_Stream();
			JsonObject lO = JsonHelper.readJsonFromUrl("https://api.twitch.tv/kraken/streams/"+channelname);
			Twitch_Stream stream = new Twitch_Stream();

			if (lO.get("stream").isJsonNull()){
				/* 
				 * Stream = Offline
				 */
				stream.setOnline(false);
				return stream;
			}else
			{
				/*
				 * Stream = Online
				 */
				JsonObject jb = JsonHelper.readJsonFromUrl("https://api.twitch.tv/kraken/channels/"+channelname);
				stream.setOnline(true);
				stream.load(jb);
				return stream;
			}
		} catch (Exception error){
			error.printStackTrace();
		}
		return null; 
	}
}
