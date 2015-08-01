package com.wc1.felisBotus.irc;

import java.util.Set;

public class IRCChannel {
	
	private String name;
	private Set<String> opList;
	
	
	public IRCChannel(String name, Set<String> opList) {
		super();
		this.opList = opList;
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public Set<String> getOpList() {
		return opList;
	}
	
	public boolean addOp(String name){
		return opList.add(name);
	}
	
	public boolean removeOp(String name){
		return opList.remove(name);
	}
}
