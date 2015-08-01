/*
 *
 * Reference = http://www.jibble.org/javadocs/pircbot/index.html
 *
 *
 *
 *
 */
package com.wc12.felisbotus2;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.wc12.felisbotus2.irc.IRCServer;

public class FelisBotus extends PircBot {       

	/*
	 * Options
	 */
	public boolean voiceUsers = true;

	private String owner;
	private IRCServer server; //this thing will contain all info on the server, channels and ops in said channels.
	private String loginPass;
	/*
	 *   ops.add(sender);
	 */


	public FelisBotus() {
		try{//TODO put this here or in main? I'm thinking move to main, put only one constructer where its put infomration in to initilize bot immediately
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			System.out.printf("No config file found\nInitilizing for first time use\n\nWhat is your IRC Name? (Not the bot's)");
			owner = input.readLine();
			System.out.printf("What would you like this bot to be called?\n");
			this.setName(input.readLine());
			System.out.printf("What is the login email address?\n");
			this.setLogin(input.readLine());
			System.out.printf("Would you like to save a password for authentication?\n(Otherwise you will be prompted everytime the bot starts)\n");
			String in = input.readLine();
			if (in.startsWith("y") || in.startsWith("Y")){
				System.out.printf("Please enter a password");
				loginPass = input.readLine();
			}
			this.setVersion(Main.version);
			//compileConfigFile();
		}
		catch(IOException e){}
	}



	public FelisBotus(File config) {
		try {
			Element rootElem = (new SAXBuilder().build(config)).getRootElement();//TODO move to XML manager as this is no longer handled by the bot

		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.equalsIgnoreCase("!time")) {
			String time = new java.util.Date().toString();
			sendMessage(channel, sender + ": The time is now " + time);

		}
		if (message.equalsIgnoreCase("!borg")){
			sendMessage(channel, "We are the Borg. Lower your shields and surrender your ships. We will add your biological and technological distinctiveness to our own. Your culture will adapt to service us. Resistance is futile.");
		}
		if (message.equalsIgnoreCase("!botleave")){
			sendMessage(channel, "I am un-wanted and will now leave.");
			quitServer();
		}

	}

	@Override
	public void onJoin(String channel, String sender, String login, String hostname){

		sendMessage(channel,  "Hello " + sender + " Welcome to the Qubed C3 IRC Channel- (I am a Bot)");

		if (voiceUsers){
			this.voice(channel, sender);
		}

	}

	@Override
	public void log(String line) {

		System.out.println(line + "\n");
	}

	public void messageSender(String message){//TODO ask jenny whats going on with this?
		String channels = "#c3";
		sendMessage("#c3", message);
		sendRawLine(message);

		// Debug code
		int queue = this.getOutgoingQueueSize();
		System.out.println("From Bot Message sent = " + channels + " : " + message);
		System.out.println(queue);
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		sendMessage(sender, "I am not available for private discussions at this time.");
	}

	public void onUserList(String channel, User[] users) {//TODO op people who are in this list and are on the op list
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			String nick = user.getNick();
			System.out.println(nick);
		}
	}

	public void onDisconnect() {
		while (!isConnected()) {
			try {
				reconnect();
			}
			catch (Exception e) {
				// Couldn’t reconnect.
				// Pause for a short while before retrying?
			}
		}
	}

	public void onKick(String channel, String kickerNick, String login, String hostname, String recipientNick, String reason) {
		if (recipientNick.equalsIgnoreCase(getNick())) {
			joinChannel(channel);
		}
	}


	public void checkUserPermissions(){

	}

	





	public void connectNew() {
		// TODO Auto-generated method stub

	}



	public void connectAuto() {
		// TODO Auto-generated method stub

	}



	public String getOwner() {
		return owner;
	}



	public String getLoginPass() {
		return loginPass;
	}
	
	public IRCServer getIRCServer() {
		return server;
	}


}