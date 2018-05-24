package com.kibo.ng.bis.model;

import org.springframework.stereotype.Service;

public enum FileOperationType {

	UPDATE("update"),
	UPDATEORINSERT("UpdateOrInsert"),
	INSERT("Insert");
	
	public String command;
	private FileOperationType(String command) {
		this.command = command;
	}
	
	public static FileOperationType getType(String command) {
		FileOperationType [] values =  FileOperationType.values();
		for (FileOperationType commandType : values) {
			if(command.equals(commandType.command))
				return commandType;
		}
		return null;
	}
	
	@Service
    private static class FileOperationInjector{
		
		
	}
}
