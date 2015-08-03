package com.wc1.felisBotus;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;

import com.wc1.felisBotus.xml.SaveData;
import com.wc1.felisBotus.xml.XMLManager;

/**
 * Main class for running bots. Handles the list of bots as a single bot can only connect to one server. 
 * Also holds a map of commands to responses for use by all the bots.
 * Contains methods for handling the saving of all bots.
 * @author Reece
 *
 */
public class Main {

	private static List<FelisBotus> bots;
	private static Map<String, String> commands;
	private static boolean noSave = false;
	
	/**
	 * Location of the config file
	 */
	public static final String configFile = "./config.xml";
	
	

	/**
	 * Main Method for FelisBotus.
	 * Checks entered arguments, then checks for a config file. If one is found it reads it and initilizes needed bot instances.
	 * If not it gets required information from console to connect to an initial server and channel.
	 * @param args '-reset' to not use any saved configs and start new. '-nosave' to not save any changes of the bots.
	 */
	public static void main(String[] args) {
		Console console = System.console();
		boolean reset = false;
		for(int i = 0; i < args.length;i++){
			if (args[i].equals("-reset")) reset = true;
			if (args[i].equals("-noSave")) noSave = true;
		}
		File config = new File(configFile);
		if(reset || !config.exists()){
			if (!config.exists()) System.out.printf("No config file found in expected place\n");
			console.printf("Initilizing for first time use\n\n");
			//bot info
			String owner = console.readLine("What is your IRC Name? (Not the bot's)\n");
			String botName = console.readLine("What would you like this bot to be called?\n");
			String login = console.readLine("What is the login email address for the bot?\n");
			String response = console.readLine("Would you like to save a password for authentication? Y/N\n(Not Advised; this will be stored in plaintext)\n");
			String loginPass = null;
			if (response.startsWith("y") || response.startsWith("Y")){//could probably be smart about this but eh, only expect y/n or yes/no
				loginPass = console.readPassword("Please enter a password").toString();
			}
			bots = new ArrayList<FelisBotus>();
			bots.add(new FelisBotus(botName, owner, login, loginPass));//initilize new bot
			commands = new HashMap<String, String>();
		}
		else {
			//initilize bots with previous file
			SaveData loadedData;
			try {
				loadedData = XMLManager.loadConfigFile();

				bots = loadedData.getBots();
				commands = loadedData.getCommands();
			}
			catch (JDOMException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Enable debugging output.
		for (int i = 0; i < args.length; i++){
			if (args[i].equalsIgnoreCase("-debug")){
				for (FelisBotus bot:bots)bot.setVerbose(true);
				break;
			}
		}

		for (FelisBotus bot:bots){
			bot.connectConsole();
		}
		//TODO from here listen to console for specific commands
	}

	/**
	 * Method to save all information about the current running bot. As each instance can only connect to one server it is managed from here.
	 * @return Returns true if save successful and false otherwise.
	 * @throws IOException
	 */
	public static boolean save() throws IOException{
		if (noSave) return false;
		XMLManager.compileConfigFile(bots, commands);
		return true;
	}

	//TODO Method to reload all bots?

	/**
	 * Method to get the response for the entered command.
	 * @param command Command to find a response for
	 * @return Response to command or null if command does not exist in Map
	 */
	public static String getResponse(String command){
		return commands.get(command);
	}

	/**
	 * Stores a command and it's response into the map
	 * @param command Command to be used by others
	 * @param response Response that bot sends out to others
	 * @return old response if command already exists, null otherwise.
	 */
	public static String putCommand(String command, String response){
		return commands.put(command, response);
	}

	//TODO create a new bot on command
	//TODO remove a bot on command
}