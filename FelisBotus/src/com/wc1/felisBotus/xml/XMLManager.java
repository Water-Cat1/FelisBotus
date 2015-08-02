package com.wc1.felisBotus.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.wc1.felisBotus.FelisBotus;
import com.wc1.felisBotus.Main;
import com.wc1.felisBotus.irc.IRCChannel;
import com.wc1.felisBotus.irc.IRCServer;

/**
 * Class for managing the input and output of XML files for the bot.
 * Just two methods, one for compiling and generating the file, the other for reading the file.
 * @author Reece
 *
 */
public class XMLManager {

	
	
	/**
	 * compiles an XML file based on the supplied list of bots and map of commands
	 * @param bots List of bots to save
	 * @param commands Map of commands to save
	 * @return Returns true if save successful
	 * @throws IOException If error is encountered attempting to save the file
	 */
	public static boolean compileConfigFile(List<FelisBotus> bots, Map<String, String> commands) throws IOException {
		Document doc = new Document();
		//root setup
		Element elemRoot = new Element("FelisBotusConfig");//<FelisBotusConfig>
		Element elemBotList = new Element("BotList");//<BotList>
		for (FelisBotus currBot:bots){
			//save bot name
			Element elemCurrBot = new Element(currBot.getName());//<Bot Owner="" Login="" [LoginPass=""]>
			elemCurrBot.setAttribute("Owner", currBot.getOwner());
			elemCurrBot.setAttribute("Login", currBot.getLogin());
			String loginPass = currBot.getLoginPass();
			if (loginPass != null){
				elemCurrBot.setAttribute("LoginPass", loginPass);
			}
			//save single server associated with this bot
			IRCServer currServer = currBot.getIRCServer(); 
			Element elemCurrServer = new Element("Server");//<Server Address="">
			elemCurrServer.setAttribute("Address", currServer.getServerAddress());
			//add support here for servers with passwords if I need it
			//add channels associated with this server
			Element currServerChannels = new Element("Channels");//<Channels>
			for (IRCChannel currChannel:currServer.getChannels()){
				Element elemCurrChannel = new Element(currChannel.getName().substring(1)); //<Channel> //Do channels have certain things like passwords?
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

	/**
	 * Returns a savedata object from the config file found at Main.config
	 * @return SaveData generated from config file
	 * @throws JDOMException If error encounted parsing XML file
	 * @throws IOException If error encounted reading the file
	 */
	public static SaveData loadConfigFile() throws JDOMException, IOException {
		List<FelisBotus> bots = new ArrayList<FelisBotus>(); //new list  of bots

		Element elemRoot = (new SAXBuilder().build(new File(Main.configFile))).getRootElement();
		//TODO verify that file is of correct structure
		Element elemBotList = elemRoot.getChild("BotList");
		for (Element elemCurrBot:elemBotList.getChildren()){
			String currBotName = elemCurrBot.getName();
			String currBotOwner = elemCurrBot.getAttributeValue("Owner");
			String currLogin = elemCurrBot.getAttributeValue("Login");
			String currLoginPass = elemCurrBot.getAttributeValue("LoginPass"); //will return null if no password is saved
			Element elemCurrServer = elemCurrBot.getChild("Server");
			String currServerAddress = elemCurrServer.getAttributeValue("Address");
			Element elemChannels = elemCurrServer.getChild("Channels");
			Set<IRCChannel> channels = new HashSet<IRCChannel>();
			for (Element elemCurrChannel:elemChannels.getChildren()){
				String currChannelName = "#" + elemCurrChannel.getName();
				Element elemOps = elemCurrChannel.getChild("Ops");
				Set<String> ops = new HashSet<String>();
				for (Element elemCurrOp:elemOps.getChildren()){
					ops.add(elemCurrOp.getName());
				}
				IRCChannel currChannel = new IRCChannel(currChannelName, ops);
				channels.add(currChannel);
			}
			IRCServer currServer = new IRCServer(currServerAddress, channels);
			FelisBotus currBot = new FelisBotus(currBotName, currBotOwner, currLogin, currLoginPass, currServer);
			bots.add(currBot);
		}
		Map<String, String> commands = new HashMap<String,String>(); //new map for custom commands
		Element elemCommands = elemRoot.getChild("Commands");
		for (Element currCommand:elemCommands.getChildren()){
			commands.put(currCommand.getName(), currCommand.getAttributeValue("Response"));
		}
		return new SaveData(bots,commands);
	}
}
