package com.WC1.CatBot;

import org.jdom2.Document;
import org.jdom2.Element;

public class XMLManager {

	private void compileConfigFile() {
		Document doc = new Document();
		Element elemRoot = new Element("FelisBotusConfig");
		//save owner name
		Element elemOwner = new Element("Owner");
		elemOwner.setText(owner);
		elemRoot.addContent(elemOwner);
		//save bot name
		Element elemBotName = new Element("BotName");
		elemBotName.setText(this.getName());
		elemRoot.addContent(elemBotName);
		//save login address
		Element elemLogin = new Element("Login");
		elemLogin.setText(this.getLogin());
		elemRoot.addContent(elemLogin);
		Element elemPass = new Element("Pass");
		elemPass.setText(pass);
		elemRoot.addContent(elemPass);
		//Save server
		if (server != null){
			Element elemServer = new Element("Server");
			elemServer.setAttribute("Name", server.getServerName());
			elemServer.setAttribute("Address", server.getServerAddress());
			Element elemChannelList = new Element("Chennels");
			//channels of the server
			for(String currChannel:server.getChannels()){
				Element elemCurrChannel = new Element(currChannel);
			}
		}
	}
}
