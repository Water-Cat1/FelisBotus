package com.wc1.felisBotus.irc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IRCServer {

	private Set<IRCChannel> channels = new HashSet<IRCChannel>();
	private String serverAddress;


	public IRCServer(String serverAddress,
			Set<IRCChannel> channels) {
		super();
		this.serverAddress = serverAddress;
		this.channels = channels;
	}
	
	public IRCServer(String serverAddress) {
		super();
		this.serverAddress = serverAddress;
		this.channels = new HashSet<IRCChannel>();
	}


	public Set<IRCChannel> getChannels() {
		return Collections.unmodifiableSet(channels);
	}


	public String getServerAddress() {
		return serverAddress;
	}

	public boolean addChannel(IRCChannel newChannel){
		return channels.add(newChannel);
	}

	public boolean removeChannel(String oldChannel){
		return channels.remove(oldChannel);
	}
}
