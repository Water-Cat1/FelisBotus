package com.wc1.felisBotus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;

import com.wc1.felisBotus.xml.SaveData;
import com.wc1.felisBotus.xml.XMLManager;

public class Main {

	private static List<FelisBotus> bots;
	private static Map<String, String> commands;
	
	private static boolean noSave = false;

	//public static String Network = "irc.esper.net";
	//public static String Channel = "#C3";
	public static final String version = "C3 Java IRC Bot - V0.2.W";
	public static final String configFile = "./config.xml";

	public static void main(String[] args) {

		boolean reset = false;
		for(int i = 0; i < args.length;i++){
			if (args[i].equals("-reset")) reset = true;
			if (args[i].equals("-noSave")) noSave = true;
		}
		File config = new File(configFile);
		if(reset || !config.exists()){
			bots = new ArrayList<FelisBotus>();
			bots.add(new FelisBotus());//initilize new bot
		}
		else {
			//initilize bots with previous file
			SaveData loadedData;
			try {
				loadedData = XMLManager.loadConfigFile();
			
			bots = loadedData.getBots();
			commands = loadedData.getCommands();
			}
			catch (JDOMException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Enable debugging output.
		for (int i = 0; i < args.length; i++){
			if (args[i].equalsIgnoreCase("-debug")){
				for (FelisBotus bot:bots)bot.setVerbose(true);
				break;
			}
		}

		for (FelisBotus bot:bots){
			bot.connectAuto();
		}
		//TODO from here listen to console for specific commands
	}
	
	public static boolean save() throws IOException{
		if (noSave) return false;
		XMLManager.compileConfigFile(bots, commands);
		return true;
	}
	
	//TODO Method to reload all bots?
	
	public static String getCommand(String command){
		return commands.get(command);
	}
	
	public static String putCommand(String command, String response){
		return commands.put(command, response);
	}
	
	//TODO create a new bot on command
	//TODO remove a bot on command
}