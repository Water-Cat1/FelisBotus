package com.q3.qubert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.JDOMException;

import com.q3.qubert.xml.SaveData;
import com.q3.qubert.xml.XMLManager;

/**
 * Main class for running bots. Handles the list of bots as a single bot can only connect to one server. 
 * Also holds a map of commands to responses for use by all the bots.
 * Contains methods for handling the saving of all bots.
 * @author Water_Cat1
 * @author JennyLeeP
 */
public class Main {

	private static List<ServBot> bots;
	private static Map<String, String> commands;
	private static List<String> streamersTwitch;
	private static boolean noSave = false;
	private static boolean devEnviro = false; //if system.console() returns null then set this true
	private static boolean debugMode = false;
	private static BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

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
		boolean reset = false;
		if (System.console() == null){
			devEnviro = true;
		}
		for(int i = 0; i < args.length;i++){
			if (args[i].equals("-reset")) reset = true;
			if (args[i].equals("-noSave")) noSave = true;
		}
		File config = new File(configFile);
		if(reset || !config.exists()){
			if (!config.exists()) System.out.printf("No config file found in expected place\n");
			initializeNewBot();
			commands = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		}
		else {
			//initilize bots with previous file
			SaveData loadedData;
			try {
				loadedData = XMLManager.loadConfigFile();

				bots = loadedData.getBots();
				if (bots.size() == 0 ){
					System.out.printf("No saved servers found\n");
					initializeNewBot();
				}
				commands = loadedData.getCommands();
				streamersTwitch = loadedData.getTwitchStreamers();
			}
			catch (JDOMException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Enable debugging output.
		for (int i = 0; i < args.length; i++){
			if (args[i].equalsIgnoreCase("-debug")){
				debugMode = true;
				for (ServBot bot:bots)bot.setVerbose(true);
				break;
			}
		}

		for (ServBot bot:bots){
			bot.connectConsole();
		}
		//listen to console for specific commands
		while(true){


		}
	}

	private synchronized static void initializeNewBot() {
		System.out.printf("Initilizing for first time use\n\n");
		//bot info
		String owner = readConsole("What is your IRC Name? (Not the bot's)\n");
		String botName = readConsole("What would you like this bot to be called?\n");
		String login = readConsole("What is the login email address for the bot?\n");
		String response = readConsole("Would you like to save a password for authentication? Y/N\n(Not Advised; this will be stored in plaintext)\n");
		String loginPass = null;
		if (response.startsWith("y") || response.startsWith("Y")){//could probably be smart about this but eh, only expect y/n or yes/no
			loginPass = new String(readConsolePass("Please enter a password"));
		}
		bots = new ArrayList<ServBot>();
		bots.add(new ServBot(botName, owner, login, loginPass));//initilize new bot
	}

	/**
	 * Method to save all information about the current running bot. As each instance can only connect to one server it is managed from here.
	 * @return Returns true if save successful and false otherwise.
	 * @throws IOException
	 */
	public synchronized static boolean save() throws IOException{
		if (noSave) return false;
		XMLManager.compileConfigFile(bots, commands, streamersTwitch);
		return true;
	}

	//TODO Method to reload all bots?

	/**
	 * Method to get the response for the entered command.
	 * @param command Command to find a response for
	 * @return Response to command or null if command does not exist in Map
	 */
	public synchronized static String getResponse(String command){
		return commands.get(command);
	}

	/**
	 * Stores a command and it's response into the map
	 * @param command Command to be used by others
	 * @param response Response that bot sends out to others
	 * @return old response if command already exists, null otherwise.
	 */
	public synchronized static String putCommand(String command, String response){
		return commands.put(command, response);
	}

	public synchronized static String removeCommand(String command){
		return commands.remove(command);
	}

	public synchronized static void removeBot(ServBot bot) {
		bot.shutDown();
		bots.remove(bot);
		try {
			save();
		} catch (IOException e) {
			System.out.printf("\nError saving config file\n");
			e.printStackTrace();
		}
	}
	
	public synchronized static boolean addBot(ServBot bot){
		bot.setVerbose(debugMode);
		boolean result =  bot.connectCommand();
		if (result){
			bots.add(bot);
			return result;
		} else{
			return result;
		}
	}

	public synchronized static ServBot getBotConnectedTo(String string) {
		for (ServBot currBot:bots){
			if (currBot.getServer().equalsIgnoreCase(string)){
				return currBot;
			}
		}
		return null;

	}

	public synchronized static String[] getConnectedChannelsOnServer(String server){
		ServBot bot = getBotConnectedTo(server);
		if (bot != null){
			return bot.getChannels();
		} else{
			return null;
		}
	}
	
	public synchronized static void shutItDown(boolean force) throws IOException{
		try {
			Main.save();
		} catch (IOException e) {
			if (!force)throw e;
		}
		for (ServBot bot:bots){
			bot.shutDown();
		}
		System.exit(0);

	}

	public static String readConsole(String query){
		if (devEnviro){
			System.out.printf("%s\n", query);
			try {
				String result = inReader.readLine();
				return result;
				} catch (IOException e) {
					System.out.printf("Error attempting to read console\n");
				}
			return "";
		} else{
			return System.console().readLine(query);
		}
	}

	public static String readConsolePass(String query){
		if (devEnviro){
			System.out.printf("%s\n", query);
			try {
				String result = inReader.readLine();
				return result;
				} catch (IOException e) {
					System.out.printf("Error attempting to read console\n");
				}
			return "";
		} else{
			return new String(System.console().readPassword(query));
		}
	}
}