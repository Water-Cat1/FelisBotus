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
			String newServer = System.console().readLine("Please enter a server address.\n");
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
				pass = new String(System.console().readPassword("\nPlease enter a password to verify the bot on %s\n", this.server.getServerAddress()));
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

	public boolean isVoiceUsers() {
		return voiceUsers;
	}

	@Override
	public void log(String line) {

		System.out.println(line + "\n");
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
					}
				}
			}
		}
	}

	public void shutDown(){
		shuttingdown = true;
		if(isConnected()){
			for (IRCChannel channel:server.getChannels()){
				partChannel(channel.getName(), "Shutting Down");
			}
			disconnect();
		}
		dispose();
	}

	@Override
	protected void onJoin(String channel, String sender, String login,
			String hostname) {

		if (sender != this.getNick()) {
			if (sender.equals(owner)){
				sendNotice(sender, "Greetings commander! The qube monkeys are ready for testing!");
			}
			else{
				sendNotice(sender, "Hello " + sender
						+ " Welcome to the Qubed C3 IRC Channel- (I am a Bot)");
			}
		}
		if (isVoiceUsers()) {
			this.voice(channel, sender);
		}

	}

	//	public void onKick(String channel, String kickerNick, String login,
	//			String hostname, String recipientNick, String reason) {
	//		if (recipientNick.equalsIgnoreCase(getNick())) {
	//			joinChannel(channel);
	//			sendMessage(channel, "Guess who is baaaaack!");
	//		}
	//	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.startsWith(commandStart)){
			boolean isOp = server.getChannel(channel).checkOP(sender);
			String lowercaseCommand = message.toLowerCase(Locale.ROOT).split(" ")[0];
			String[] splitMessage = {""};
			switch(lowercaseCommand.substring(commandStart.length())){ //substring removes the command section of the string
			case("addcommand"):
				splitMessage = message.split(" ",3);
			if (isOp && splitMessage.length >= 3){
				String result = Main.putCommand(splitMessage[1].toLowerCase(Locale.ROOT), splitMessage[2]);
				if (result !=null){
					sendNotice(sender, "Command successfully overwritten :]. Previous response was '" +result+"'");
				}
				else{
					sendNotice(sender, "Command successfully added :]");
				}
				try {
					Main.save();
				} catch (IOException e) {
					sendNotice(sender, "Failed to save command. Command will be lost on bot restart :[");
					System.out.printf("\nFailed to save bot!\n");
					e.printStackTrace();
				}
			}
			else if (splitMessage.length < 3){
				sendNotice(sender, "Syntax Error. Correct usage is " + commandStart +"addcommand <newCommand> <Response>");
			}
			else{
				sendNotice(sender, "You must be an OP to use this command");
			}
			break;
			case("removecommand"):
				if (isOp){
					splitMessage = message.split(" ",3);
					if (splitMessage.length == 2){
						String result = Main.removeCommand(splitMessage[1]);
						if (result==null){
							sendNotice(sender, splitMessage[1] + " was never a saved command");
						}
						else{
							sendNotice(sender, "Command successfully removed! :]");
							try {
								Main.save();
							} catch (IOException e) {
								sendNotice(sender, "Failed to save command. Command will come back on bot restart :[");
								System.out.printf("\nFailed to save bot!\n");
								e.printStackTrace();
							}
						}
					}
					else{
						sendNotice(sender, "Syntax Error. Correct usage is " + commandStart +"removecommand <oldCommand>");
					}
				}
				else{
					sendNotice(sender, "You must be an OP to use this command");
				}
			break;
			case("leavechannel"):
				if (isOp){
					splitMessage = message.split(" ");
					if ((!splitMessage[1].startsWith("#")) || splitMessage.length > 2){
						sendNotice(sender, "Syntax Error. Correct usage is " + commandStart +"leavechannel [channel]. "
								+ "Channel must be prefxed by a #. If no channel is supplied then bot will leave this channel");
					}
					else if (splitMessage.length == 1 || splitMessage[1].equals(channel)){
						partChannel(channel, "I don't hate you");
						server.removeChannel(channel);
						if (server.getChannels().size() == 0){ //not connected to any channels, disconnect from the server
							shuttingdown = true;
							disconnect();
						}
					}
					else{
						if (server.getChannels().contains(splitMessage[1])){
							partChannel(splitMessage[1], "I must go, my people need me");
							server.removeChannel(splitMessage[1]);
						}
						else{
							sendNotice(sender, "I am not connected to this channel");
						}
					}
				}
				else{
					sendNotice(sender, "You must be an OP to use this command");
				}
			break;
			case("leaveserver"):
				if (isOp){
					splitMessage = message.split(" ");
					if (splitMessage.length > 2){
						sendNotice(sender, "Syntax Error. Correct usage is " + commandStart +"leaveserver [server]. "
								+ "If no server is supplied then bot will leave this server");
					}
					else if (splitMessage.length == 1 || splitMessage[1].equals(server.getServerAddress())){
						Main.removeBot(this);
					}
					else{
						FelisBotus botToDisconnect = Main.getBotConnectedTo(splitMessage[1]);
						if (botToDisconnect == null){
							sendNotice(sender, "I am not connected to that server");
						}
						else{
							Main.removeBot(botToDisconnect);
							sendNotice(sender, "Successfully disconnected from " + splitMessage[1]);
						} 
					}
				}else{
					sendNotice(sender, "You must be an OP to use this command");
				}
			break;
			case("joinchannel"):

				break;
			case("joinserver"):
				break;
			case("shutdown"):
				if (isOp){
					splitMessage = message.split(" ");
					if (splitMessage.length == 2 && splitMessage[1].equalsIgnoreCase("force")){
						try {
							Main.shutItDown(true);
						} catch (IOException e) {
							//Will never throw exception here
						}
					}
					else if (splitMessage.length == 1){
						try {
							Main.shutItDown(false);
						} catch (IOException e) {
							sendNotice(sender, "Error while attempting to save before shutdown :[ \n"
									+ "If you wish to ignore this use " + commandStart + "shutdown force.");
							System.out.printf("Error encounted while attempting to save while shuting down\n");
							e.printStackTrace();
						}
					}
					else{
						sendNotice(sender, "Syntax Error. Correct usage is " + commandStart +"shutdown [force]. "
								+ "If the word 'force' is supplied then bot will shutdown even if an error occurs.");
					}
				}else{
					sendNotice(sender, "You must be an OP to use this command");
				}
			break;
			default:
				String response = Main.getResponse(lowercaseCommand.substring(commandStart.length()));
				if (response != null){
					sendMessage(channel, response);
				}
				else{
					sendNotice(sender, "Invalid command, please ensure it is spelled correctly");
				}

			}
		}

	}



	/* (non-Javadoc)
	 * @see org.jibble.pircbot.PircBot#onNotice(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onNotice(String sourceNick, String sourceLogin,
			String sourceHostname, String target, String notice) {
		// TODO Auto-generated method stub
		super.onNotice(sourceNick, sourceLogin, sourceHostname, target, notice);
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
			StringBuilder output = new StringBuilder("Bot initialized.");
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