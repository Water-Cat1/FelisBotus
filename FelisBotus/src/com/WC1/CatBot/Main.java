package com.WC1.CatBot;

import java.io.File;

public class Main {

	private static FelisBotus bot;

	//public static String Network = "irc.esper.net";
	//public static String Channel = "#C3";
	public static final String version = "C3 Java IRC Bot - V0.2.W";
	public static final String configFile = "./config.xml";

	public static void main(String[] args) {


		File config = new File(configFile);
		if(!config.exists()){
			bot = new FelisBotus();//initilize new bot
		}
		else {
			//initilize bot with previous file
			bot = new FelisBotus(config); 
		}
		// Enable debugging output.
		for (int i = 0; i < args.length; i++){
			if (args[i].equalsIgnoreCase("-debug")){
				bot.setVerbose(true);
				break;
			}
		}


		// Connect to the IRC server.
		for (int i = 0; i <= args.length; i++){
			if (args.length == i){
				bot.connectAuto();
			}
			else if (args[i].equalsIgnoreCase("-manualconnect")){
				bot.connectNew();
				break;
			}
		}

	}
}