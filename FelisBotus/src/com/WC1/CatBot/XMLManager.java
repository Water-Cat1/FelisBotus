package com.WC1.CatBot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLManager {

	private boolean compileConfigFile(List<FelisBotus> bots, Map<String, String> commands) throws IOException {
		Document doc = new Document();
		//root setup
		Element elemRoot = new Element("FelisBotusConfig");//<FelisBotusConfig>
		Element elemBotList = new Element("BotList");//<BotList>
		for (FelisBotus currBot:bots){
			//save bot name
			Element elemCurrBot = new Element(currBot.getName());//<Bot Owner="" Login="" [Pass=""]>
			//save bot owner
			elemCurrBot.setAttribute("Owner", currBot.getOwner());
			//save bot login
			elemCurrBot.setAttribute("Login", currBot.getLogin());
			//save password if it is to be saved
			String loginPass = currBot.getLoginPass();
			if (loginPass != null){
				elemCurrBot.setAttribute("Pass", loginPass);
			}
			//save single server associated with this bot
			IRCServer currServer = currBot.getIRCServer(); 
			Element elemCurrServer = new Element(currServer.getServerName());//<Server address="">
			elemCurrServer.setAttribute("address", currServer.getServerAddress());
			//add support here for servers with passwords if I need it
			//add channels associated with this server
			Element currServerChannels = new Element("Channels");//<Channels>
			for (IRCChannel currChannel:currServer.getChannels()){
				Element elemCurrChannel = new Element(currChannel.getName()); //<Channel> //Do channels have certain things like passwords?
				//save ops for this channel
				Element elemCurrChannelOps = new Element("Ops");//<Ops>
				for (String currOp:currChannel.getOpList()){
					elemCurrChannelOps.addContent(new Element(currOp));//<opName/>
				}
				elemCurrChannel.addContent(elemCurrChannelOps);// </Ops>
				currServerChannels.addContent(elemCurrChannel); //</Channel>
			}
			elemCurrServer.addContent(currServerChannels);//</Channels>
			elemCurrBot.addContent(elemCurrServer);//</Server>
			elemBotList.addContent(elemCurrBot);//</Bot>
			
		}
		elemRoot.addContent(elemBotList);//</BotList>
		Element elemCommands = new Element("Commands");//<Commands>
		for (String key:commands.keySet()){
			Element elemCurrCommand = new Element(key);//<Command response=""/>
			elemCurrCommand.setAttribute("Response", commands.get(key));
			elemCommands.addContent(elemCurrCommand);
		}
		elemRoot.addContent(elemCommands);//</Commands>
		doc.setRootElement(elemRoot); //</FelisBotusConfig>
		
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		xmlOut.output(doc, new FileWriter(new File(Main.configFile)));
		return true;
	}
}
