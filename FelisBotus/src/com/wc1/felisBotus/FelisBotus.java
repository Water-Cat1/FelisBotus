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

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.wc1.felisBotus.irc.IRCChannel;
import com.wc1.felisBotus.irc.IRCServer;

public class FelisBotus extends PircBot {

	private boolean voiceUsers = true;

	private String owner;
	private IRCServer server; // this thing will contain all info on the server,
	// channels and ops in said channels.
	private String loginPass;

	public FelisBotus(String botName, String owner, String login,
			String loginPass) {
		this.setName(botName);
		this.owner = owner;
		this.setLogin(login);
		this.loginPass = loginPass;
		this.setVersion(Main.version);
	}

	public FelisBotus(String botName, String owner, String login,
			String loginPass, IRCServer currServer) {
		this.setName(botName);
		this.owner = owner;
		this.setLogin(login);
		this.loginPass = loginPass;
		this.server = currServer;
		this.setVersion(Main.version);
	}

	/**
	 * Call to connect bots to default server assigned to them. Assumes call is from console and will ask console for missing information.
	 */
	public void connectInitial(){
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
			System.out.println(pass);
			//identify(pass);
			Thread.sleep(1000);
			if (server.getChannels().size() == 0){ //if no default channels then connect to a new ones
				String newChannel = System.console().readLine("Please enter a channel name to connect to.\n");
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
	public void connectNew(IRCServer newServer){

	}


	public void checkUserPermissions() {

	}


	public IRCServer getIRCServer() {
		return server;
	}

	public String getLoginPass() {
		return loginPass.toString();
	}

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
		if (message.equalsIgnoreCase("!time")) {
			String time = new java.util.Date().toString();
			sendMessage(channel, sender + ": The time is now " + time);

		}
		if (message.equalsIgnoreCase("!borg")) {
			sendMessage(
					channel,
					"We are the Borg. Lower your shields and surrender your ships. We will add your biological and technological distinctiveness to our own. Your culture will adapt to service us. Resistance is futile.");
		}
		if (message.equalsIgnoreCase("!botleave")) {
			sendMessage(channel, "I am un-wanted and will now leave.");
			quitServer();
		}

	}

	public void onPrivateMessage(String sender, String login, String hostname,
			String message) {
		
	}

	public void onUserList(String channel, User[] users) {// TODO op people who
		// are in this list
		// and are on the op
		// list
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			String nick = user.getNick();
			System.out.println(nick);
		}
	}

	public void setVoiceUsers(boolean voiceUsers) {
		this.voiceUsers = voiceUsers;
	}

}