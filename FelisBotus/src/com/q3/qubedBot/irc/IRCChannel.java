package com.q3.qubedBot.irc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to represent information about a single IRC channel. to be stored in the IRC Server object.
 * Contains channel name (including the '#' symbol) and the set of ops
 * @author Water_Cat1
 *
 */
public class IRCChannel {

	private String name;
	private String pass;
	private Set<String> ops;
	private boolean botIsOP = false;

	
	/**
	 * Create an instance of IRCChannel supply the name, password and set of ops
	 * @param name
	 * @param pass
	 * @param ops
	 */
	public IRCChannel(String name, String pass, Set<String> ops){
		super();
		this.ops = ops;
		this.name = name;
		this.pass = pass;
	}
	/**
	 * Create an instance of IRCChannel with just the channel name.
	 * @param name
	 */
	public IRCChannel(String name) {
		super();
		this.ops = new HashSet<String>();
		this.name = name;
	}
	
	public IRCChannel(String name, String pass){
		super();
		this.ops = new HashSet<String>();
		this.name = name;
		this.pass = pass;
	}

	/**
	 * Get the channel name
	 * @return Name of the channel
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the saved channel password
	 * @return
	 */
	public String getPass(){
		return pass;
	}

	/**
	 * Get an unmodiffiable set of ops. to modify the list please use the appropriate methods for addOp and removeOp
	 * @return unmodiffiable set of ops
	 */
	public Set<String> getOpList() {
		return Collections.unmodifiableSet(ops);
	}

	/**
	 * Add the username to the list of ops
	 * @param name Username for the Op
	 * @return True if successfully added, false otherwise
	 */
	public boolean addOp(String name){
		return ops.add(name);
	}

	/**
	 * remove op from the list of ops
	 * @param name Name to remove
	 * @return True if name successfully removed, false otherwise
	 */
	public boolean removeOp(String name){
		return ops.remove(name);
	}
	
	public boolean checkOp(String name){
		return ops.contains(name);
	}

	public void setBotIsOp(boolean opStatus){
		botIsOP = opStatus;
	}
	
	public boolean getBotIsOp(){
		return botIsOP;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IRCChannel))
			return false;
		IRCChannel other = (IRCChannel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
