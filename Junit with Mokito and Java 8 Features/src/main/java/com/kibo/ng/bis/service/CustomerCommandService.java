package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.Customer;
import com.kibo.ng.bis.jaxb.Entity;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventories;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Order;
import com.kibo.ng.bis.jaxb.PriceList;
import com.kibo.ng.bis.jaxb.PriceListItem;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandRequestType;
import com.kibo.ng.bis.model.CommandType;
import com.kibo.ng.bis.model.ConfigModel;

@Service
public class CustomerCommandService extends MozuApiService<Customer>{

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config, CatalogModel catalogModel) {
		List<Customer> customers = importCommand.getCustomers().getCustomer();
		for (Customer customer : customers) {
			importEntityRecord(importCommand, customer, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Customer customer) {
		record.setCustomer(customer);
	}

	@Override
	public void insertEntity(Customer customer, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEntity(Customer customer, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInsertEntity(Customer customer, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
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
