/*
 *
 * Reference = http://www.jibble.org/javadocs/pircbot/index.html

 *Used foundation of CyBot made by JennyLeeP to get me started. Many thanks to her help and support!
 *
 *
 *
 */
package com.wc1.felisBotus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.wc1.felisBotus.irc.IRCChannel;
import com.wc1.felisBotus.irc.IRCServer;

/**
 * Bot for the program. Each instance can only connect to one server, so several instances will need to be created to connect to several servers.
 * Stores the username for the owner of this bot, server this bot will connect to, password to identify this bot (if it is being saved), login address and bot name.
 * Bot will always listen to its owner or Ops in the channel. (yet to be implemented)
 * @author Water_Cat1
 * @author JennyLeeP
 */
public class FelisBotus extends PircBot {

	private boolean voiceUsers = true;

	private String owner;
	private IRCServer server; // this thing will contain all info on the server,
	// channels and ops in said channels.
	private String loginPass;
	private BotCommandHelper commandHelper = new BotCommandHelper(this);

	private boolean shuttingdown = false;

	/**Version of the bot*/
	public static final String version = "C3 Java IRC Bot - V0.5.W";
	/**String that this bot will recognize as a command to it*/
	public static final String commandStart = "\\";

	/**
	 * Constructor for when bot without server information (can be added later through other methods) Used mainly for first time creation
	 * @param botName Name for this bot
	 * @param owner Username for the owner of this bot. Bot will always recognize commands from this user
	 * @param login Login address for the bot
	 * @param loginPass Password to identify this bot (can be null)
	 */
	public FelisBotus(String botName, String owner, String login,
			String loginPass) {
		this.setName(botName);
		this.owner = owner;
		this.setLogin(login);
		this.loginPass = loginPass;
		this.setVersion(version);
	}

	/**
	 * Constructer to create this bot, including server information. Used mainly for loading from the XML file.
	 * @param botName Name for this bot
	 * @param owner Username for the owner of this bot. Bot will always recognize commands from this user
	 * @param login Login address for the bot
	 * @param loginPass Password to identify this bot (can be null)
	 * @param currServer Server information for this bot
	 */
	public FelisBotus(String botName, String owner, String login,
			String loginPass, IRCServer currServer) {
		this.setName(botName);
		this.owner = owner;
		this.setLogin(login);
		this.loginPass = loginPass;
		this.server = currServer;
		this.setVersion(version);
	}

	/**
	 * Call to connect bots to default server assigned to them. Assumes call is from console and will ask console for missing information.
	 */
	public void connectConsole(){
		this.setAutoNickChange(true);
		if (server == null){
			String newServer = Main.readConsole("Please enter a server address.\n");
			server = new IRCServer(newServer);
		}
		try {
			this.connect(server.getServerAddress());//TODO add support for saving port numbers and server passwords
			while (!isConnected()){//wait till successfully connected
				Thread.sleep(5000);
			}
			//verify login
			String pass;
			if (loginPass != null){
				pass = loginPass;
			}
			else{
				Thread.sleep(5000);
				pass = new String(Main.readConsolePass(String.format("\nPlease enter a password to verify the bot on %s\n", this.server.getServerAddress())));
			}
			if(!this.getName().equals(this.getNick())){//bot has a secondary name. GHOST primary nickname and then take it!
				sendMessage("NickServ", "GHOST " + this.getName() + " " + pass.toString());
				Thread.sleep(1000);
				changeNick(this.getName());
			}
			identify(pass);
			Thread.sleep(1000);
			if (server.getChannelNames().size() == 0){ //if no default channels then connect to a new ones
				String newChannel = Main.readConsole("Please enter a channel name to connect to.\n");
				while (!newChannel.startsWith("#")){
					newChannel = Main.readConsole("Channel name requires a '#' symbol at the start.\n");
				}
				server.addChannel(new IRCChannel(newChannel));
				this.joinChannel(newChannel);
			}else{//Connect to all default channels
				for (IRCChannel channel:server.getChannels()){
					if (channel.getPass()==null || channel.getPass().isEmpty()){
						this.joinChannel(channel.getName());
					} else{
						this.joinChannel(channel.getName(), channel.getPass());
					}
				}
			}
			Main.save();
		} catch (IOException e){//TODO how to manage exceptions? return to console/
			//
		} catch (IrcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	/**
	 * Method to make bot connect to supplied server. 
	 * @param newServer New server to connect to
	 */
	public void connectCommand(IRCServer newServer){
		//TODO make this and make exception to be thrown if already connected to a server.
		//do i make this recieve a server or use the one saved by the bot? It does need a server otherwise it won't be controllable.
	}


	/**
	 * Returns the server associated with this instance of the bot
	 * @return server for this bot
	 */
	public IRCServer getIRCServer() {
		return server;
	}

	/**
	 * Get the login password stored by this bot 
	 * @return
	 */
	public String getLoginPass() {
		return loginPass;
	}

	/**
	 * Get the username for the owner of this pot
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param shuttingdown the shuttingdown to set
	 */
	public void setShuttingdown() {
		this.shuttingdown = true;
	}

	public boolean isVoiceUsers() {
		return voiceUsers;
	}

	public void onDisconnect() {
		if (shuttingdown){
			Main.removeBot(this);
		}
		else{
			int retryCount = 0;
			while (!isConnected()) {
				try {
					reconnect();
					//ghost old bot?
				} catch (Exception e) {
					retryCount++;
					if (retryCount > 5){
						shuttingdown = true;
						Main.removeBot(this);
						break;
					}
				}
			}
		}
	}

	public void shutDown(){
		shuttingdown = true;
		if(isConnected()){
			for (String channel:server.getChannelNames()){
				partChannel(channel, "Shutting Down");
			}
			disconnect();
		}
		dispose();
	}

	@Override
	protected void onJoin(String channel, String sender, String login,
			String hostname) {

		if (sender != this.getNick()) {
			IRCChannel currChannel = server.getChannel(channel);
			if (currChannel.getBotIsOp() && currChannel.checkOP(sender)){
				op(channel, sender);
			}
			if (sender.equals(owner)){
				sendNotice(sender, "Greetings commander! The qube monkeys are ready for testing!");
			}
			else{
				//sendNotice(sender, "Hello " + sender
				//		+ " Welcome to the Qubed C3 IRC Channel!");
			}
		}
		if (isVoiceUsers()) {
			this.voice(channel, sender);
		}

	}

	protected void onKick(String channel, String kickerNick, String login,
				String hostname, String recipientNick, String reason) {
			if (recipientNick.equalsIgnoreCase(getNick())) {
				joinChannel(channel);
				sendMessage(channel, "Guess who is baaaaack!");
				sendNotice(kickerNick, "Please use the shutdown command to safely shut down me. I don't like being kicked.");
			}
		}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.startsWith(commandStart)){
			
			String lowercaseCommand = message.toLowerCase(Locale.ROOT).split(" ")[0].substring(FelisBotus.commandStart.length());
			commandHelper.runBotCommand(this, channel, sender, message, lowercaseCommand);
		}

	}

	/* (non-Javadoc)
	 * @see org.jibble.pircbot.PircBot#onNotice(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onNotice(String sourceNick, String sourceLogin,
			String sourceHostname, String target, String notice) {
		
		//sendNotice(sourceNick, "What are you trying to notify me about? Has timmy fallen down the well?!");
	}

	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin,
		String sourceHostname, String recipient) {
		IRCChannel currChannel = server.getChannel(channel);
		Set<String> opList = currChannel.getOpList();
		if (recipient.equals(this.getNick())){
			server.getChannel(channel).setBotIsOp(true);
			User[] users = getUsers(channel);
			for (int i = 0; i < users.length; i++) {
				User user = users[i];
				String nick = user.getNick();
				if (opList.contains(nick)){
					if(!user.isOp()){//user is on OP list but is not op'd, so op them
						op(channel, nick);
					}
				}
			}
			try {
				if(Main.save())sendMessage(channel, "OpBot initialized.");
			} catch (IOException e) {
				sendMessage(channel, "Error occured while saving bot config. :[");
				e.printStackTrace();
			}
		}
		else{
			if(!opList.contains(recipient)){
				currChannel.addOp(recipient);
				try {
					if(Main.save())sendMessage(channel, recipient + " has been added to this bots known Ops");
				} catch (IOException e) {
					sendMessage(channel, "Error occured while saving bot config. :[");
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onDeop(String channel, String sourceNick,
			String sourceLogin, String sourceHostname, String recipient) {
		if (recipient.equals(this.getNick())){
			server.getChannel(channel).setBotIsOp(false);
		}
		else{
			IRCChannel currChannel = server.getChannel(channel);
			Set<String> opList = currChannel.getOpList();
			if(opList.contains(recipient)){
				currChannel.removeOp(recipient);
				try {
					if(Main.save())sendMessage(channel, recipient + " has been removed from this bots known Ops");
				} catch (IOException e) {
					sendMessage(channel, "Error occured while saving bot config. :[");
					e.printStackTrace();
				}
			}
		}
	}

	protected void onUserList(String channel, User[] users) {
		IRCChannel currChannel = server.getChannel(channel);
		Set<String> opList = currChannel.getOpList();
		List<String> addedToList = new ArrayList<String>();
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			String nick = user.getNick();
			if (!opList.contains(nick)){
				if(user.isOp() && nick != this.getNick()){//user is op'd but is not on bots op list, so add them to the list
					currChannel.addOp(nick);
					addedToList.add(nick);
				}
			}
		}
		StringBuilder output = new StringBuilder("Bot initialized.");
		if (addedToList.size() > 0){
			output.append(" Added " + String.join(", ", addedToList.toArray(new String[addedToList.size()])) + " to this bots known Ops");
		}
		try {
			if(Main.save())sendMessage(channel, output.toString());
		} catch (IOException e) {
			sendMessage(channel, "Error occured while saving bot config. :[");
			e.printStackTrace();
		}
	}

	public void setVoiceUsers(boolean voiceUsers) {
		this.voiceUsers = voiceUsers;
	}
	
	/**
	 * Method to have the bot join a channel in the current server. Use this instead of pircbot joinChannel.
	 * @param channel Channel to join
	 */
	public void joinIRCChannel(String channel){
		joinChannel(channel);
		server.addChannel(new IRCChannel(channel));
	}
	
	/**
	 * Method to have the bot join a channel in the current server. Use this instead of pircbot joinChannel.
	 * @param channel Channel to join
	 * @param pass password for the channel
	 */
	public void joinIRCChannel(String channel, String pass){
		joinChannel(channel, pass);
		server.addChannel(new IRCChannel(channel, pass));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof FelisBotus))
			return false;
		FelisBotus other = (FelisBotus) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		return true;
	}

}