package com.wc1.felisBotus;

import java.io.IOException;
import java.util.Locale;

import com.wc1.felisBotus.irc.IRCChannel;
import com.wc1.felisBotus.streamAPIs.twitch.Twitch_API;
import com.wc1.felisBotus.streamAPIs.twitch.Twitch_Stream;

public class BotCommandHelper {

	private FelisBotus parentBot;

	public BotCommandHelper(FelisBotus felisBotus) {
		parentBot = felisBotus;
	}
	
	public void runBotCommand(FelisBotus felisBotus, String channel, String sender, String message, String lowercaseCommand) {
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
		case("joinchannel"):
			Thread thread = new Thread(new JoinChannel(sender, message, isOp));
			thread.start();
			//joinChannel(sender, message, isOp);
			break;
		case("joinserver"):
			//TODO
			break;
		case("shutdown"):
			shutdownBot(sender, message, isOp);
			break;
		case("twitch"):
			twitch(channel, message);
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
	

	private void twitch(String channel, String message) {
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
							+ "If you wish to ignore this use " + FelisBotus.commandStart + "shutdown force.");
					System.out.printf("Error encounted while attempting to save while shuting down\n");
					e.printStackTrace();
				}
			}
			else{
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"shutdown [force]. "
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
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"leaveserver [server]. "
						+ "If no server is supplied then bot will leave this server");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(parentBot.getIRCServer().getServerAddress())){
				Main.removeBot(parentBot);
			}
			else{
				FelisBotus botToDisconnect = Main.getBotConnectedTo(splitMessage[1]);
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
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"leavechannel [channel]. "
						+ "Channel must be prefixed by a #. If no channel is supplied then bot will leave this channel");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(channel)){
				parentBot.partChannel(channel, "Requested to leave channel");
				parentBot.getIRCServer().removeChannel(channel);
				if (parentBot.getIRCServer().getChannels().size() == 0){ //not connected to any channels, disconnect from the server
					parentBot.setShuttingdown();
					parentBot.disconnect();
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
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"removecommand <oldCommand>");
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
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"addcommand <newCommand> <Response>");
		}
		else{
			parentBot.sendNotice(sender, "You must be an OP to use this command");
		}
	}

	//private void joinChannel(String sender, String message, boolean isOp) {
	//}
	
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
			parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"listchannels [server]");
		}
		
		
	}

	private class JoinChannel implements Runnable{

		private String sender;
		private String message;
		private boolean isOp;
		
		public JoinChannel(String sender, String message, boolean isOp) {
			this.sender = sender;
			this.message = message;
			this.isOp = isOp;
		}
		
		@Override
		public void run() {
			if (isOp){
				String[] splitMessage = message.split(" ");
				if ((splitMessage.length == 2 && !splitMessage[1].startsWith("#")) || splitMessage.length > 3 || splitMessage.length < 2){
					parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"joinchannel <channel> [pass]. "
							+ "Channel must be prefixed by a #.");
				}
				else if (parentBot.getIRCServer().isConnectedTo(splitMessage[1])){
					parentBot.sendNotice(sender, "This bot is already connected to that channel");
				}
				else if (splitMessage.length == 2){
					if(parentBot.joinIRCChannel(splitMessage[1])){
						parentBot.sendNotice(sender, "Successfully joined " + splitMessage[1]);
					} else{
						parentBot.sendNotice(sender, "unable to join " + splitMessage[1] + ". Please check details are correct and that there is no password required.");
					}
				}
				else if (splitMessage.length == 3){ //password supplied
					if (parentBot.joinIRCChannel(splitMessage[1], splitMessage[2])){
						parentBot.sendNotice(sender, "Successfully joined " + splitMessage[1]);
					} else{
						parentBot.sendNotice(sender, "unable to join " + splitMessage[1] + ". Please check details are correct.");
					}
				}
			}else{
				parentBot.sendNotice(sender, "You must be an OP to use this command");
			}
		}
		
	}

}
