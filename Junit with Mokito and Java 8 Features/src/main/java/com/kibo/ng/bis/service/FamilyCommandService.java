package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.Entity;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.Families;
import com.kibo.ng.bis.jaxb.Family;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventories;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandRequestType;
import com.kibo.ng.bis.model.CommandType;
import com.kibo.ng.bis.model.ConfigModel;

@Service
public class FamilyCommandService extends MozuApiService<Family> {
	
	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config, CatalogModel catalogModel) {
		List<Family> families = importCommand.getFamilies().getFamily();
		for (Family family : families) {
			importEntityRecord(importCommand, family, marketLive, config, catalogModel);
		}
		
		return true;
	}


	@Override
	public void setInputRecord(InputRecord record, Family family) {
		record.setFamily(family);
	}

	@Override
	public void insertEntity(Family family, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateEntity(Family family, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateInsertEntity(Family family, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getDefaultOrderByString() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void decryptFields(List<CommandResult> results, ConfigModel config) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setInitialData(ConfigModel config) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
