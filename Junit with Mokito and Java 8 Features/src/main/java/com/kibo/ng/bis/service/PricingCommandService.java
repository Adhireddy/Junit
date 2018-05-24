package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Sku;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;

@Service
public class PricingCommandService extends MozuApiService<Sku>{

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config, CatalogModel catalogModel) {
		List<Sku> skuList = importCommand.getSkus().getSku();
		for (Sku skulist2 : skuList) {
			importEntityRecord(importCommand, skulist2, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Sku sku) {
		record.setSku(sku);
	}

	@Override
	public void insertEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInsertEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
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
