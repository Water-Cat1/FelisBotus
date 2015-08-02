package com.wc1.felisBotus.irc;

import java.util.HashSet;
import java.util.Set;

public class IRCChannel {

	private String name;
	private Set<String> ops;


	public IRCChannel(String name, Set<String> ops) {
		super();
		this.ops = ops;
		this.name = name;
	}
	
	public IRCChannel(String name) {
		super();
		this.ops = new HashSet<String>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Set<String> getOpList() {
		return ops;
	}

	public boolean addOp(String name){
		return ops.add(name);
	}

	public boolean removeOp(String name){
		return ops.remove(name);
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
