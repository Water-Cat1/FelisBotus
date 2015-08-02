package com.wc1.felisBotus.irc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to contain information about an IRC server
 * @author Reece
 *
 */
public class IRCServer {

	private Set<IRCChannel> channels = new HashSet<IRCChannel>();
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
		this.channels = channels;
	}
	
	/**
	 * Create an instance using just a server address
	 * @param serverAddress
	 */
	public IRCServer(String serverAddress) {
		super();
		this.serverAddress = serverAddress;
		this.channels = new HashSet<IRCChannel>();
	}

	/**
	 * Get the set of saved channels for this server
	 * @return Set of saved channels
	 */
	public Set<IRCChannel> getChannels() {
		return Collections.unmodifiableSet(channels);
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
	 * @return True if successfully added, false otherwise
	 */
	public boolean addChannel(IRCChannel newChannel){
		return channels.add(newChannel);
	}

	/**
	 * Remove a chanel from the saved set
	 * @param oldChannel Channel to remove
	 * @return True if successfully removed
	 */
	public boolean removeChannel(String oldChannel){
		return channels.remove(oldChannel);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serverAddress == null) ? 0 : serverAddress.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IRCServer))
			return false;
		IRCServer other = (IRCServer) obj;
		if (serverAddress == null) {
			if (other.serverAddress != null)
				return false;
		} else if (!serverAddress.equals(other.serverAddress))
			return false;
		return true;
	}
	
	
}
