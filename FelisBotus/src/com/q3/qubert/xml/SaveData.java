package com.q3.qubert.xml;

import java.util.List;
import java.util.Map;

import com.q3.qubert.ServBot;

/**
 * Class for storing data returned from reading the XML config file
 * @author Water_Cat1
 *
 */
public class SaveData {

	private List<ServBot> bots;
	private Map<String, String> commands;
	private List<String> streamersTwitch;

	/**
	 * Create an instance of save data supplying the list of pots and map of commands
	 * @param bots List of bots created
	 * @param commands Map of commands
	 */
	public SaveData(List<ServBot> bots, Map<String, String> commands, List<String> streamersTwitch) {
		super();
		this.bots = bots;
		this.commands = commands;
		this.streamersTwitch = streamersTwitch;
	}

	/**
	 * Get the list of bots
	 * @return List of bots
	 */
	public List<ServBot> getBots() {
		return bots;
	}

	/**
	 * Get the map of commands
	 * @return Map of commands
	 */
	public Map<String, String> getCommands() {
		return commands;
	}
	
	public List<String> getTwitchStreamers(){
		return streamersTwitch;
	}


}
