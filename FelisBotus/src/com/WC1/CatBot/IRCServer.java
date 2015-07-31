package com.WC1.CatBot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IRCServer {

	private Set<String> channels = new HashSet<String>();
	private String serverAddress;
	private String serverName;
	
	
	public IRCServer(String serverName, String serverAddress,
			Set<String> channels) {
		super();
		this.serverName = serverName;
		this.serverAddress = serverAddress;
		this.channels = channels;
	}


	public Set<String> getChannels() {
		return Collections.unmodifiableSet(channels);
	}


	public String getServerAddress() {
		return serverAddress;
	}


	public String getServerName() {
		return serverName;
	}
	
	public boolean addChannel(String newChannel){
		return channels.add(newChannel);
	}
	
	public boolean removeChannel(String oldChannel){
		return channels.remove(oldChannel);
	}
}
