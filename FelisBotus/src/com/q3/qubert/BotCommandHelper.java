package com.q3.qubert;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import com.q3.qubert.irc.IRCChannel;
import com.q3.qubert.irc.IRCServer;
import com.q3.qubert.streamAPIs.beam.Beam_API;
import com.q3.qubert.streamAPIs.beam.Beam_Stream;
import com.q3.qubert.streamAPIs.hitBox.HitBox_API;
import com.q3.qubert.streamAPIs.hitBox.HitBox_Stream;
import com.q3.qubert.streamAPIs.twitch.Twitch_API;
import com.q3.qubert.streamAPIs.twitch.Twitch_Stream;

public class BotCommandHelper {

	private ServBot parentBot;

	public BotCommandHelper(ServBot felisBotus) {
		parentBot = felisBotus;
	}

	public void runBotCommand(ServBot felisBotus, String channel, String sender, String message, String lowercaseCommand) {
		boolean isOp = false;
		if (channel.equalsIgnoreCase(sender)){ //private message!
			for (IRCChannel currChannel:parentBot.getIRCServer().getChannels()){
				if (currChannel.checkOp(sender)){
					isOp = true; //if future security requires it, save what channel they are oped on too.
					break;
				}
			}
		} else{
			isOp = parentBot.getIRCServer().getChannel(channel).checkOp(sender);
		}
		switch(lowercaseCommand){ //substring removes the command section of the string
		case("addcommand"):
			addCommand(sender, message, isOp);
		break;
		case("removecommand"):
			removeCommand(sender, message, isOp);
		break;
		case("leavechannel"):
			leaveChannel(channel, sender, message, isOp);
		break;
		case("leaveserver"):
			leaveServer(sender, message, isOp);
		break;
		case("join"):
			Thread thread = new Thread(new JoinRunnable(sender, message, isOp));
		thread.start();
		break;
		case("shutdown"):
			shutdownBot(sender, message, isOp);
		break;
		case("twitch"):
			twitch(channel, sender, message);
		break;
		case("hitbox"):
			hitbox(channel, sender, message);
		break;
		case("beam"):
			beam(channel, sender, message);
		break;
		case("commands"):
			//TODO parse command

			break;

		case("roll"):
			dice(channel, sender, message);
		break;
		case("listchannels"):
			getChannels(channel, message, sender);
		break;
		default:
			String response = Main.getResponse(lowercaseCommand);
			if (response != null){
				felisBotus.sendMessage(channel, response);
			}
			else{
				felisBotus.sendNotice(sender, lowercaseCommand + " is an invalid command, please ensure it is spelled correctly");
			}

		}
	}

	private void dice(String channel, String sender, String message) {
		String[] splitMessage = message.split(" ");
		if (!Pattern.matches("roll(\\s\\d+(\\s\\d+)?)?\\s*", message.substring(ServBot.commandStart.length()))){ //matches "roll[ num[ num]]"
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart + "roll [numOfdice] [numOfSides]."
					+ " If no numbers are given, two 6-sided dice are rolled. If only number of dice is given then 6-sided dice will be rolled");
		}
		else{
			Random rand = new Random();
			int numDice = splitMessage.length >=2 ? Integer.parseInt(splitMessage[1]) : 2;
			int numSides = splitMessage.length == 3 ? Integer.parseInt(splitMessage[2]) : 6;
			String[] rolls = new String[numDice];
			int total = 0;
			for (int i=0; i<numDice; i++){
				int currRoll = rand.nextInt(numSides) + 1; //nextint is 0 (inclusive) to bound (exclusive)
				rolls[i] = String.valueOf(currRoll);
				total += currRoll;
			}
			parentBot.sendMessage(channel, String.format("%s rolled a %d (Individual rolls: %s)", sender, total, String.join(", ", rolls)));
		}
	}

	private void beam(String channel, String sender, String message) {
		String[] splitMessage = message.split(" ");
		if (splitMessage.length == 2){
			String userName = splitMessage[1];
			Beam_Stream bStream = Beam_API.getStream(userName);

			String bStatus = String.format(bStream.getUser()+" is Live! | Title: "+bStream.getTitle()+" | Game: "+bStream.getGame()+" | Url: "+bStream.getUrl());
			if (bStream.getIsOnline()){
				parentBot.sendMessage(channel, bStatus);
			}else
			{
				parentBot.sendMessage(channel,"Stream is Currently Offline");
			}
		}else
		{
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart + "beam <channel>");
		}
	}

	private void hitbox(String channel, String sender, String message) {
		String[] splitMessage = message.split(" ");
		if (splitMessage.length == 2){
			String userName = splitMessage[1];
			HitBox_Stream hStream = HitBox_API.getStream(userName);

			String hStatus = String.format(hStream.getUser()+" is Live! | Title: "+hStream.getTitle()+" | Game: "+hStream.getGame()+" | Url: "+hStream.getUrl());
			if (hStream.getIsOnline()){
				parentBot.sendMessage(channel, hStatus);
			}else
			{
				parentBot.sendMessage(channel,"Stream is Currently Offline");
			}
		}else
		{
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart + "hitbox <channel>");
		}
	}


	private void twitch(String channel, String sender, String message) {
		String[] splitMessage;
		splitMessage = message.split(" ");
		if (splitMessage.length == 2){
			String userName = splitMessage[1];
			Twitch_Stream stream = Twitch_API.getStream(userName);
			String status = String.format("%s is live! | Game = %s | Title = %s | URL = %s", userName.toUpperCase(), stream.getGame(), stream.getStatus(), stream.getUrl());
			if (stream.isOnline()){
				parentBot.sendMessage(channel, status);
			}else
			{
				parentBot.sendMessage(channel,"Stream is Currently Offline");
			}
		} else {
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart + "twitch <channel>");
		}
	}

	private void shutdownBot(String sender, String message, boolean isOp) {
		String[] splitMessage;
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
					parentBot.sendNotice(sender, "Error while attempting to save before shutdown :[ \n"
							+ "If you wish to ignore this use " + ServBot.commandStart + "shutdown force.");
					System.out.printf("Error encounted while attempting to save while shuting down\n");
					e.printStackTrace();
				}
			}
			else{
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"shutdown [force]. "
						+ "If the word 'force' is supplied then bot will shutdown even if an error occurs.");
			}
		}else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	private void leaveServer(String sender, String message, boolean isOp) {
		String[] splitMessage;
		if (isOp){
			splitMessage = message.split(" ");
			if (splitMessage.length > 2){
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"leaveserver [server]. "
						+ "If no server is supplied then bot will leave this server");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(parentBot.getIRCServer().getServerAddress())){
				Main.removeBot(parentBot);
			}
			else{
				ServBot botToDisconnect = Main.getBotConnectedTo(splitMessage[1]);
				if (botToDisconnect == null){
					parentBot.sendNotice(sender, "I am not connected to that server");
				}
				else{
					Main.removeBot(botToDisconnect);
					parentBot.sendNotice(sender, "Successfully disconnected from " + splitMessage[1]);
				} 
			}
		}else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	private void leaveChannel(String channel, String sender, String message,
			boolean isOp) {
		String[] splitMessage;
		if (isOp){
			splitMessage = message.split(" ");
			if ((splitMessage.length == 2 && !splitMessage[1].startsWith("#")) || splitMessage.length > 2){
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"leavechannel [channel]. "
						+ "Channel must be prefixed by a #. If no channel is supplied then bot will leave this channel");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(channel)){
				parentBot.partChannel(channel, "Requested to leave channel");
				parentBot.getIRCServer().removeChannel(channel);
				if (parentBot.getIRCServer().getChannels().size() == 0){ //not connected to any channels, disconnect from the server
					Main.removeBot(parentBot);
				}
			}
			else{
				if (parentBot.getIRCServer().isConnectedTo(splitMessage[1])){
					parentBot.partChannel(splitMessage[1], "Remote channel leave request");
					parentBot.getIRCServer().removeChannel(splitMessage[1]);
					parentBot.sendNotice(sender, "Successfully left " + splitMessage[1]);
				}
				else{
					parentBot.sendNotice(sender, "I am not connected to this channel");
				}
			}
		}
		else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	private void removeCommand(String sender, String message, boolean isOp) {
		String[] splitMessage;
		if (isOp){
			splitMessage = message.split(" ",3);
			if (splitMessage.length == 2){
				String result = Main.removeCommand(splitMessage[1]);
				if (result==null){
					parentBot.sendNotice(sender, splitMessage[1] + " was never a saved command");
				}
				else{
					parentBot.sendNotice(sender, "Command successfully removed! :]");
					try {
						Main.save();
					} catch (IOException e) {
						parentBot.sendNotice(sender, "Failed to save command removal. Command will come back on bot restart :[");
						System.out.printf("\nFailed to save bot!\n");
						e.printStackTrace();
					}
				}
			}
			else{
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"removecommand <oldCommand>");
			}
		}
		else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	private void addCommand(String sender, String message, boolean isOp) {
		String[] splitMessage;
		splitMessage = message.split(" ",3);
		if (isOp && splitMessage.length >= 3){
			String result = Main.putCommand(splitMessage[1].toLowerCase(Locale.ROOT), splitMessage[2]);
			if (result !=null){
				parentBot.sendNotice(sender, "Command successfully overwritten :]. Previous response was '" +result+"'");
			}
			else{
				parentBot.sendNotice(sender, "Command successfully added :]");
			}
			try {
				Main.save();
			} catch (IOException e) {
				parentBot.sendNotice(sender, "Failed to save command. Command will be lost on bot restart :[");
				System.out.printf("\nFailed to save bot!\n");
				e.printStackTrace();
			}
		}
		else if (splitMessage.length < 3){
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"addcommand <newCommand> <Response>");
		}
		else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	private void getChannels(String channel, String message, String sender){ //TODO support for entering another server to check if its connected and what channels there?
		String[] splitMessage = message.split(" ");
		if (splitMessage.length == 2 || (splitMessage.length == 3 && splitMessage[2].equalsIgnoreCase(parentBot.getServer()))){ //get the channels of the current server
			String[] channelNames = parentBot.getChannels();
			parentBot.sendMessage(channel, "Channels in this server I am currently connected to are:");
			parentBot.sendMessage(channel, String.join(", ", channelNames) + ".");
		}
		else if (splitMessage.length == 3){
			String[] channelNames = Main.getConnectedChannelsOnServer(splitMessage[2]);
			if (channelNames != null){
				parentBot.sendMessage(channel, "Channels on " + splitMessage[2] + " I am currently connected to are:");
				parentBot.sendMessage(channel, String.join(", ", channelNames) + ".");
			} else{
				parentBot.sendMessage(channel, "I am not connected to " + splitMessage[2]);
			}
		} else{
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + ServBot.commandStart +"listchannels [server]");
		}


	}

	private class JoinRunnable implements Runnable{

		private String sender;
		private String message;
		private boolean isOp;

		public JoinRunnable(String sender, String message, boolean isOp) {
			this.sender = sender;
			this.message = message;
			this.isOp = isOp;
		}

		@Override
		public void run() {
			if (isOp){
				if (!Pattern.matches("^join(?: ([\\w\\-\\.:]+)(?: ([^\\s\\t\\n\\r]+))?)?(?: (#[^\\s\\t\\n\\r]+)(?: ([^\\s\\t\\n\\r]+))?)+", message.substring(ServBot.commandStart.length()))){
					parentBot.sendNotice(sender, "Syntax Error. Correct usage to get this bot to connect to:");
					parentBot.sendNotice(sender, "A channel on this server is " + ServBot.commandStart + "join <#channel> [password]");
					parentBot.sendNotice(sender, "A channel on another server is " + ServBot.commandStart + "join <server> [pass] <#channel> [password]");
					parentBot.sendNotice(sender, "All channels must start with the # symbol. Multiple channels can joined by listing them after the first channel. "
							+ "e.g. " + ServBot.commandStart + "join #foo #bar will join both channels #foo and #bar");
				}
				else{
					String[] splitMessage = message.split(" ");
					ServBot bot;
					if (splitMessage[1].startsWith("#")){
						bot = parentBot;
					} else {
						bot = Main.getBotConnectedTo(splitMessage[1]);	
						if (bot == null){
							String pass = splitMessage[2].startsWith("#") ? null : splitMessage[2];
							IRCServer newServ = new IRCServer(splitMessage[1]);
							bot = new ServBot(parentBot.getName(), parentBot.getOwner(), parentBot.getLogin(), pass, newServ);
							if (Main.addBot(bot)){
								parentBot.sendNotice(sender, "Successfully joined " + splitMessage[1]);
							} else {
								parentBot.sendNotice(sender, "Unable to join " + splitMessage[1] + ". Please check all details are correct");
								return;
							}
						}
					}
					for (int i = 1; i < splitMessage.length; i++){ //connect to channels
						if (splitMessage[i].startsWith("#")){
							String channel = splitMessage[i];
							String pass = null;
							boolean success = false;
							if (i<splitMessage.length-1 && !splitMessage[i+1].startsWith("#")){
								pass = splitMessage[i+1];
								success = bot.joinIRCChannel(channel, pass);
								i++; //read the password for this channel, skips ahead so don't attempt to connect to a channel named the password
							}
							else{
								success = bot.joinIRCChannel(channel);
							}
							
							if (success){
								parentBot.sendNotice(sender, "Successfully joined " + splitMessage[i]);
							}else{
								parentBot.sendNotice(sender, "unable to join " + splitMessage[i] + ". Please check details are correct and that there is no password required.");
							}
						}
						try {
							Main.save();
						} catch (IOException e) {
							// failed to save
						}
					}
				}
			}else{
				parentBot.sendNotice(sender, "You must be an OP to use this command");
			}
		}

	}

}
