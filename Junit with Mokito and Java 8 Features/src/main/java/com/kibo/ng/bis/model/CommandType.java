package com.kibo.ng.bis.model;

public enum CommandType {

	UPDATE("update"),
	CREATEORUPDATE("createOrUpdate"),
	INSERT("Insert"),
	DELETE("delete");
	
	public String command;
	private CommandType(String command) {
		this.command = command;
	}
	
	public static CommandType getType(String command) {
		CommandType [] values =  CommandType.values();
		for (CommandType commandType : values) {
			if(command.equals(commandType.command))
				return commandType;
		}
		return null;
	}
	
}
