package com.WC1.CatBot;

import java.util.List;
import java.util.Map;

public class SaveData {

	private List<FelisBotus> bots;
	private Map<String, String> Commands;
	
	public SaveData(List<FelisBotus> bots, Map<String, String> commands) {
		super();
		this.bots = bots;
		Commands = commands;
	}

	public List<FelisBotus> getBots() {
		return bots;
	}

	public Map<String, String> getCommands() {
		return Commands;
	}
	
	
}
