package com.wc1.felisBotus.xml;

import java.util.List;
import java.util.Map;

import com.wc1.felisBotus.FelisBotus;

/**
 * Class for storing data returned from reading the XML config file
 * @author Reece
 *
 */
public class SaveData {

	private List<FelisBotus> bots;
	private Map<String, String> Commands;

	/**
	 * Create an instance of save data supplying the list of pots and map of commands
	 * @param bots List of bots created
	 * @param commands Map of commands
	 */
	public SaveData(List<FelisBotus> bots, Map<String, String> commands) {
		super();
		this.bots = bots;
		Commands = commands;
	}

	/**
	 * Get the list of bots
	 * @return List of bots
	 */
	public List<FelisBotus> getBots() {
		return bots;
	}

	/**
	 * Get the map of commands
	 * @return Map of commands
	 */
	public Map<String, String> getCommands() {
		return Commands;
	}


}
