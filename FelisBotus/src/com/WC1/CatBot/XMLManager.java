package com.WC1.CatBot;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class XMLManager {

	private void compileConfigFile(List<FelisBotus> bots) {
		Document doc = new Document();
		//root setup
		Element elemRoot = new Element("FelisBotusConfig");
		Element elemBotList = new Element("BotList");
		elemRoot.addContent(elemBotList);
		for (FelisBotus currBot:bots){
			//save bot name
			Element elemCurrBot = new Element(currBot.getName());
			//save bot owner
			elemCurrBot.setAttribute("Owner", currBot.getOwner());
			//save bot login
			elemCurrBot.setAttribute("Login", currBot.getLogin());
			//save password if it is to be saved
			String loginPass = currBot.getLoginPass();
			if (loginPass != null){
				elemCurrBot.setAttribute("Pass", loginPass);
			}
			elemBotList.addContent(elemCurrBot);
			IRCServer currServer = currBot.getIRCServer();
			Element elemCurrServer = new Element(currServer.getServerName());
			
			
		}


		//elemRoot.addContent(elemPass);
		//Save server
		//if (server != null){
//			Element elemServer = new Element("Server");
//			elemServer.setAttribute("Name", server.getServerName());
//			elemServer.setAttribute("Address", server.getServerAddress());
//			Element elemChannelList = new Element("Chennels");
//			//channels of the server
//			for(String currChannel:server.getChannels()){
//				Element elemCurrChannel = new Element(currChannel);
//			}
//		}
	}
}
