package com.q3.qubedBot.streamAPIs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * Api class used to read Json from URL.
 * @author JennyLeeP
 *
 */
public class JsonHelper {

	public static Gson gson = new Gson();

	/**
	 * Opens a buffered reader, reads the URL and closes the buffered reader.
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	public static JsonObject readJsonFromUrl(String urlString) throws IOException {
		InputStreamReader inStream = null;
		try {
			URL url = new URL(urlString);
			inStream = new InputStreamReader(url.openStream());
			return gson.fromJson(inStream, JsonObject.class);
		} finally {
			if (inStream != null)
				inStream.close();
		}
	}
}
