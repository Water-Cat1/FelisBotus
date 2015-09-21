/*
 *
 * Reference = http://www.jibble.org/javadocs/pircbot/index.html

 *Used foundation of CyBot made by JennyLeeP to get me started. Many thanks to her help and support!
 *
 *
 *
 */
package com.q3.qubedBot;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.q3.qubedBot.irc.IRCChannel;
import com.q3.qubedBot.irc.IRCServer;

/**
 * Bot for the program. Each instance can only connect to one server, so several instances will need to be created to connect to several servers.
 * Stores the username for the owner of this bot, server this bot will connect to, password to identify this bot (if it is being saved), login address and bot name.
 * Bot will always listen to its owner or Ops in the channel. (yet to be implemented)
 * @author Water_Cat1
 * @author JennyLeeP
 */
public class ServBot extends PircBot {

	private boolean voiceUsers = true;

	private String owner;
	private IRCServer server; // this thing will contain all info on the server,
	// channels and ops in said channels.
	private String loginPass;
	private BotCommandHelper commandHelper = new BotCommandHelper(this);

	private boolean shuttingdown = false;
	private boolean savePass = false;

	/**Version of the bot*/
	public static final String version = "C3 Java IRC Bot - V0.5.W";
	/**String that this bot will recognize as a command to it*/
	public static final String commandStart = "!";

	/**
	 * Constructor for when bot without server information (can be added later through other methods) Used mainly for first time creation
	 * @param botName Name for this bot
	 * @param owner Username for the owner of this bot. Bot will always recognize commands from this user
	 * @param login Login address for the bot
	 * @param loginPass Password to identify this bot (can be null)
	 */
	public ServBot(String botName, String owner, String login,
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
	public ServBot(String botName, String owner, String login,
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
		if (isConnected()) return;
		this.setAutoNickChange(true);
		if (server == null){
			String newServer = Main.readConsole("Please enter a server address.\n");
			server = new IRCServer(newServer);
		}
		try {
			this.connect(server.getServerAddress(), server.getServerPort());//TODO add support for saving port numbers and server passwords
			while (!isConnected()){//wait till successfully connected
				Thread.sleep(5000);
			}
			//verify login
			String pass;
			if (loginPass != null){
				pass = loginPass;
				savePass = true;
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
			if (!pass.isEmpty())identify(pass);
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
		} catch (IOException e){//TODO how to manage exceptions? return to console?
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
	public boolean connectCommand(){
		if (isConnected()) return false;
		if (server == null) return false;
		this.setAutoNickChange(true);
		boolean identPass = true; //true if supplied pass is for nickserv, false if pass is for server
		try {
			connect(server.getServerAddress(), server.getServerPort());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} catch (IrcException e1) {
			if (loginPass == null) return false;
			try {
				connect(server.getServerAddress(), server.getServerPort(), loginPass);
				identPass = false;
			} catch (IOException | IrcException e) {
				e.printStackTrace();
				return false;
			}
		}
		try{
			while (!isConnected()){//wait till successfully connected
				Thread.sleep(5000);
			}
			//verify login
			String pass;
			if (loginPass != null && identPass){
				pass = loginPass;
				if(!this.getName().equals(this.getNick())){//bot has a secondary name. GHOST primary nickname and then take it!
					sendMessage("NickServ", "GHOST " + this.getName() + " " + pass.toString());
					Thread.sleep(1000);
					changeNick(this.getName());
				}
				identify(pass);
			}
			Main.save();
		} catch (IOException e){//TODO how to manage exceptions? return to console?
			// failed to save
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
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
	 * @return the savePass
	 */
	public boolean isPassSaved() {
		return savePass;
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

	public void onDisconnect() { //TODO 
		if (!shuttingdown){
			int retryCount = 0;
			while (!isConnected()) {
				try {
					reconnect();
					//ghost old bot? onConnect?
				} catch (Exception e) {
					retryCount++;
					if (retryCount > 6){
						shuttingdown = true;
						return;
					}
					try {
						Thread.sleep(5000); //retry connection in 5 seconds
					} catch (InterruptedException e1) {
						e1.printStackTrace();
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
		IRCChannel currChannel = server.getChannel(channel);
		if (sender != this.getNick()) {
			if (currChannel.getBotIsOp() && currChannel.checkOp(sender)){
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
		if (currChannel.getBotIsOp() && isVoiceUsers()) {
			this.voice(channel, sender);
		}

	}

	protected void onKick(String channel, String kickerNick, String login,
			String hostname, String recipientNick, String reason) {
		if (recipientNick.equalsIgnoreCase(getNick())) {
			joinChannel(channel);
			sendMessage(channel, "Guess who is baaaaack!");
			sendNotice(kickerNick, "Please use the shutdown command to safely shut me down. I don't like being kicked.");
		}
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.startsWith(commandStart)){

			String lowercaseCommand = message.toLowerCase(Locale.ROOT).split(" ")[0].substring(ServBot.commandStart.length());
			commandHelper.runBotCommand(this, channel, sender, message, lowercaseCommand);
		}

	}

	/* (non-Javadoc)
	 * @see org.jibble.pircbot.PircBot#onPrivateMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		String lowercaseCommand = message.toLowerCase(Locale.ROOT).split(" ")[0];
		if (lowercaseCommand.startsWith(commandStart)){
			lowercaseCommand = lowercaseCommand.substring(commandStart.length()); //supports inclusion or not of the command character on private message.
		}
		commandHelper.runBotCommand(this, sender, sender, message, lowercaseCommand); //considers user the channel to send response back to.
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
		boolean opAdded = false;
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			String nick = user.getNick();
			if (!opList.contains(nick)){
				if(user.isOp() && nick != this.getNick()){//user is op'd but is not on bots op list, so add them to the list
					currChannel.addOp(nick);
					opAdded = true;
				}
			}
		}
		StringBuilder output = new StringBuilder("Bot initialized.");
		if (opAdded){
			output.append(" Updated this bots known Ops");
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
	public boolean joinIRCChannel(String channel){
		joinChannel(channel);
		server.addChannel(new IRCChannel(channel)); //this is added anyway as onjoin and other methods need this to exist.
		int numAttempts = 0;
		while (true){
			for(String currChannel:getChannels()){
				if (currChannel.equalsIgnoreCase(channel)){
					return true;
				}

			}
			numAttempts++;
			if (numAttempts > 10){
				server.removeChannel(channel);
				return false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method to have the bot join a channel in the current server. Use this instead of pircbot joinChannel.
	 * @param channel Channel to join
	 * @param pass password for the channel
	 */
	public boolean joinIRCChannel(String channel, String pass){
		joinChannel(channel, pass);
		server.addChannel(new IRCChannel(channel, pass));
		int numChecks = 0;
		while (true){
			for(String currChannel:getChannels()){
				if (currChannel.equalsIgnoreCase(channel)){
					return true;
				}

			}
			numChecks++;
			if (numChecks > 10){
				server.removeChannel(channel);
				return false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		if (!(obj instanceof ServBot))
			return false;
		ServBot other = (ServBot) obj;
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