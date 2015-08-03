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
 * @author Reece
 *
 */
public class FelisBotus extends PircBot {

	private boolean voiceUsers = true;

	private String owner;
	private IRCServer server; // this thing will contain all info on the server,
	// channels and ops in said channels.
	private String loginPass;

	public static final String version = "C3 Java IRC Bot - V0.2.W";

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
			String newServer = System.console().readLine("Please enter a server address.\n");
			server = new IRCServer(newServer);
		}
		try {
			this.connect(server.getServerAddress());//TODO add support for saving port numbers and server passwords
			while (!isConnected()){//wait till successfully connected
				Thread.sleep(1000);
			}
			//verify login
			String pass;
			if (loginPass != null){
				pass = loginPass;
			}
			else{
				pass = new String(System.console().readPassword("Please enter a password to verify the bot on %s\n", this.server.getServerAddress()));
			}
			if(!this.getName().equals(this.getNick())){//bot has a secondary name. GHOST primary nickname and then take it!
				sendMessage("NickServ", "GHOST " + pass.toString());
				changeNick(this.getName());
			}
			identify(pass);
			Thread.sleep(1000);
			if (server.getChannels().size() == 0){ //if no default channels then connect to a new ones
				String newChannel = System.console().readLine("Please enter a channel name to connect to.\n");
				while (!newChannel.startsWith("#")){
					newChannel = System.console().readLine("Channel name requires a '#' symbol at the start.\n");
				}
				server.addChannel(new IRCChannel(newChannel));
				this.joinChannel(newChannel);
			}else{//Connect to all default channels
				for (IRCChannel channel:server.getChannels()){
					this.joinChannel(channel.getName()); //TODO support for channels with keys
				}
			}
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


	public void checkUserPermissions() {

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

	public boolean isVoiceUsers() {
		return voiceUsers;
	}

	@Override
	public void log(String line) {

		System.out.println(line + "\n");
	}

	public void messageSender(String message) {// TODO ask jenny whats going on
		// with this?
		String channels = "#c3";
		sendMessage("#c3", message);
		sendRawLine(message);

		// Debug code
		int queue = this.getOutgoingQueueSize();
		System.out.println("From Bot Message sent = " + channels + " : "
				+ message);
		System.out.println(queue);
	}

	public void onDisconnect() {
		while (!isConnected()) {
			try {
				reconnect();
			} catch (Exception e) {
				// Couldn’t reconnect.
				// Pause for a short while before retrying?
			}
		}
	}

	@Override
	public void onJoin(String channel, String sender, String login,
			String hostname) {

		if (sender != this.getNick()) {
			sendNotice(sender, "Hello " + sender
					+ " Welcome to the Qubed C3 IRC Channel- (I am a Bot)");
		}
		if (isVoiceUsers()) {
			this.voice(channel, sender);
		}

	}

	public void onKick(String channel, String kickerNick, String login,
			String hostname, String recipientNick, String reason) {
		if (recipientNick.equalsIgnoreCase(getNick())) {
			joinChannel(channel);
		}
	}

	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.equalsIgnoreCase("\\checkUsers")){
		}
		//if (message.equalsIgnoreCase("!time")) {
		//	String time = new java.util.Date().toString();
		//	sendMessage(channel, sender + ": The time is now " + time);
		//		}
		//		if (message.equalsIgnoreCase("!borg")) {
		//			sendMessage(
		//					channel,
		//					"We are the Borg. Lower your shields and surrender your ships. We will add your biological and technological distinctiveness to our own. Your culture will adapt to service us. Resistance is futile.");
		//		}
		//		if (message.equalsIgnoreCase("!botleave")) {
		//			sendMessage(channel, "I am un-wanted and will now leave.");
		//			quitServer();
		//		}

	}

	public void onPrivateMessage(String sender, String login, String hostname,
			String message) {

	}

	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin,
		String sourceHostname, String recipient) {
		IRCChannel currChannel = server.getChannel(channel);
		Set<String> opList = currChannel.getOpList();
		if (recipient.equals(this.getNick())){
			server.getChannel(channel).setBotIsOp(true);
			List<String> addedToList = new ArrayList<String>();
			User[] users = getUsers(channel);
			for (int i = 0; i < users.length; i++) {
				User user = users[i];
				String nick = user.getNick();
				if (opList.contains(nick)){
					if(!user.isOp()){//user is on OP list but is not op'd, so op them
						op(channel, nick);
					}
				}
				else{
					if(user.isOp() && nick != this.getNick()){//user is op'd but is not on bots op list, so add them to the list
						currChannel.addOp(nick);
						addedToList.add(nick);
					}
				}
			}
			StringBuilder output = new StringBuilder("Bot settings saved.");
			if (addedToList.size() > 0){
				output.append(" Added " + String.join(", ", addedToList.toArray(new String[addedToList.size()])) + " to saved list of Ops");
			}
			try {
				if(Main.save())sendMessage(channel, output.toString());
			} catch (IOException e) {
				sendMessage(channel, "Error occured while saving bot config. :[");
				e.printStackTrace();
			}
		}
		else{
			if(!opList.contains(recipient)){
				currChannel.addOp(recipient);
				try {
					if(Main.save())sendMessage(channel, recipient + " has been added to the saved Op list");
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
					if(Main.save())sendMessage(channel, recipient + " has been removed from the saved Op list");
				} catch (IOException e) {
					sendMessage(channel, "Error occured while saving bot config. :[");
					e.printStackTrace();
				}
			}
		}
	}

	public void onUserList(String channel, User[] users) {


	}

	public void setVoiceUsers(boolean voiceUsers) {
		this.voiceUsers = voiceUsers;
	}

}