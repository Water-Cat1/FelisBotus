package com.wc1.felisBotus;

import java.io.IOException;
import java.util.Locale;

import com.wc1.felisBotus.streamAPIs.twitch.Twitch_API;
import com.wc1.felisBotus.streamAPIs.twitch.Twitch_Stream;

public class BotCommandHelper {

	FelisBotus parentBot;
	
	public BotCommandHelper(FelisBotus felisBotus) {
		parentBot = felisBotus;
	}

	void twitch(String channel, String message) {
		String[] splitMessage;
		splitMessage = message.split(" ");
		String userName = splitMessage[1];
		Twitch_Stream stream = Twitch_API.getStream(userName);
		String status = String.format("%s is live! | Game = %s | Title = %s | URL = %s", userName.toUpperCase(), stream.getGame(), stream.getStatus(), stream.getUrl());
		if (stream.isOnline()){
		parentBot.sendMessage(channel, status);
		}else
		{
		parentBot.sendMessage(channel,"Stream is Currently Offline");
		}
	}

	void shutdownBot(String sender, String message, boolean isOp) {
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

	void leaveServer(String sender, String message, boolean isOp) {
		String[] splitMessage;
		if (isOp){
			splitMessage = message.split(" ");
			if (splitMessage.length > 2){
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"leaveserver [server]. "
						+ "If no server is supplied then bot will leave this server");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(parentBot.server.getServerAddress())){
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

	void leaveChannel(String channel, String sender, String message,
			boolean isOp) {
		String[] splitMessage;
		if (isOp){
			splitMessage = message.split(" ");
			if ((!splitMessage[1].startsWith("#")) || splitMessage.length > 2){
				parentBot.sendNotice(sender, "Syntax Error. Correct usage is " + FelisBotus.commandStart +"leavechannel [channel]. "
						+ "Channel must be prefxed by a #. If no channel is supplied then bot will leave this channel");
			}
			else if (splitMessage.length == 1 || splitMessage[1].equals(channel)){
				parentBot.partChannel(channel, "I don't hate you");
				parentBot.server.removeChannel(channel);
				if (parentBot.server.getChannels().size() == 0){ //not connected to any channels, disconnect from the server
					parentBot.shuttingdown = true;
					parentBot.disconnect();
				}
			}
			else{
				if (parentBot.server.getChannels().contains(splitMessage[1])){
					parentBot.partChannel(splitMessage[1], "I must go, my people need me");
					parentBot.server.removeChannel(splitMessage[1]);
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

	void removeCommand(String sender, String message, boolean isOp) {
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

	void addCommand(String sender, String message, boolean isOp) {
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

}
