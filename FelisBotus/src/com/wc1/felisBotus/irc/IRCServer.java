package com.wc1.felisBotus.irc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to contain information about an IRC server
 * @author Reece
 *
 */
public class IRCServer {

	private Map<String, IRCChannel> channels = new HashMap<String, IRCChannel>();
	private String serverAddress;


	/**
	 * Create an instance using a server address and set of saved channels
	 * @param serverAddress Address used to connect to the server
	 * @param channels
	 */
	public IRCServer(String serverAddress,
			Set<IRCChannel> channels) {
		super();
		this.serverAddress = serverAddress;
		for (IRCChannel currChannel:channels){
			this.channels.put(currChannel.getName(), currChannel);
		}
	}

	/**
	 * Create an instance using just a server address
	 * @param serverAddress
	 */
	public IRCServer(String serverAddress) {
		super();
		this.serverAddress = serverAddress;
		this.channels = new HashMap<String,IRCChannel>();
	}

	/**
	 * Get the set of saved channels for this server
	 * @return Set of saved channels
	 */
	public Collection<IRCChannel> getChannels() {
		return Collections.unmodifiableCollection(channels.values());
	}

	public IRCChannel getChannel(String channelName){
		return channels.get(channelName);
	}

	/**
	 * Get the server address
	 * @return Server address
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * Add a channel to the set of saved channels
	 * @param newChannel Channel to save
	 * @return Old channel if one is already saved, null otherwise.
	 */
	public IRCChannel addChannel(IRCChannel newChannel){
		return channels.put(newChannel.getName(), newChannel);
	}

	/**
	 * Remove a channel from the saved set
	 * @param oldChannel Channel to remove
	 * @return IRCChannel that was removed
	 */
	public IRCChannel removeChannel(String oldChannel){
		return channels.remove(oldChannel);
	}

}
