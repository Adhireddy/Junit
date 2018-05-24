package com.kibo.ng.bis.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.Entity;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventories;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.PriceList;
import com.kibo.ng.bis.jaxb.PriceListItem;
import com.kibo.ng.bis.jaxb.PriceLists;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandRequestType;
import com.kibo.ng.bis.model.CommandType;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.core.AuditInfo;
import com.mozu.api.contracts.productadmin.LocationInventory;
import com.mozu.api.contracts.productadmin.PriceListCollection;
import com.mozu.api.contracts.productadmin.Product;
import com.mozu.api.contracts.tenant.Site;
import com.mozu.api.resources.commerce.catalog.admin.PriceListResource;
import com.mozu.api.resources.commerce.catalog.admin.ProductResource;

@Service
public class PriceListCommandService extends MozuApiService<PriceList> {

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel configModel,
			CatalogModel catalogModel) {
		List<PriceList> priceList = importCommand.getPriceLists().getPriceList();
		for (PriceList priceList2 : priceList) {
			importEntityRecord(importCommand, priceList2, marketLive, configModel, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, PriceList priceList) {
		record.setPriceList(priceList);
	}

	@Override
	public void insertEntity(PriceList priceList, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		PriceListResource priceListResource = new PriceListResource(apiContext);

		com.mozu.api.contracts.productadmin.PriceList ngPriceListItem = new com.mozu.api.contracts.productadmin.PriceList();
		ngPriceListItem.setPriceListCode(priceList.getCode());
		ngPriceListItem.setDescription(priceList.getDescription());
		ngPriceListItem.setName(priceList.getName());

		AuditInfo info = new AuditInfo();

		info.setUpdateDate(new DateTime(priceList.getDateModified().toGregorianCalendar().getTime()));
		info.setCreateDate(new DateTime());
		info.setUpdateBy("BatchIntegration");
		info.setCreateBy("BatchIntegration");
		ngPriceListItem.setAuditInfo(info);

		List<PriceListItem> priceListItemList = priceList.getPriceListItems().getPriceListItem();

		for (PriceListItem priceListItem : priceListItemList) {

			String productCode = priceListItem.getProductCode() == null ? priceListItem.getSkuCode()
					: priceListItem.getProductCode();

			ProductResource productResource = new ProductResource(apiContext);

			Product ngProduct = productResource.getProduct(productCode);
			if (ngProduct == null) {
				List<Product> ngProducts = productResource.getProducts(0, 1, "productCode",
						"isVariation eq true and productCode eq " + productCode, null, null, null, null).getItems();
				if (ngProducts.size() > 0) {

					ngProduct = ngProducts.get(0);

				} else {
					throw new Exception("Product not found for code " + productCode);
				}
			}

		}

		priceListResource.addPriceList(ngPriceListItem);
	}

	@Override
	public void updateEntity(PriceList priceList, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		PriceListResource priceListResource = new PriceListResource(apiContext);

		com.mozu.api.contracts.productadmin.PriceList ngPriceListItem = priceListResource
				.getPriceList(priceList.getCode());
		if (ngPriceListItem == null) {
			throw new Exception("PriceList Not found for code :" + priceList.getCode());
		}

		ngPriceListItem.setPriceListCode(priceList.getCode());
		ngPriceListItem.setDescription(priceList.getDescription());

		AuditInfo info = new AuditInfo();

		info.setUpdateDate(new DateTime(priceList.getDateModified().toGregorianCalendar().getTime()));
		info.setCreateDate(new DateTime());
		info.setUpdateBy("BatchIntegration");
		info.setCreateBy("BatchIntegration");
		ngPriceListItem.setAuditInfo(info);

		List<PriceListItem> priceListItemList = priceList.getPriceListItems().getPriceListItem();

		for (PriceListItem priceListItem : priceListItemList) {
			// TODO : need to findout mapping
		}

		priceListResource.updatePriceList(ngPriceListItem, ngPriceListItem.getParentPriceListCode());

	}

	@Override
	public void updateInsertEntity(PriceList priceList, Marketlive marketLive, CatalogModel catalogModel)
			throws Exception {
		PriceListResource priceListResource = new PriceListResource(apiContext);
		com.mozu.api.contracts.productadmin.PriceList ngPriceListItem = priceListResource
				.getPriceList(priceList.getCode());
		if (ngPriceListItem == null)
			insertEntity(priceList, marketLive, catalogModel);
		else
			updateEntity(priceList, marketLive, catalogModel);
	}

	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {
		
		PriceListResource priceListResource = new PriceListResource(apiContext);
		PriceListCollection priceListCollection = priceListResource.getPriceLists(0, 100, null, filterStr, null);

		List<com.mozu.api.contracts.productadmin.PriceList> priceLists = priceListCollection.getItems();

		for (com.mozu.api.contracts.productadmin.PriceList priceList : priceLists) {

			PriceList mlPriceList = new PriceList();

			mlPriceList.setCode(priceList.getPriceListCode());
			mlPriceList.setName(priceList.getName());
			mlPriceList.setDescription(priceList.getDescription());

			mlPriceList.getPriceListItems();// TODO need to identify the mapping

			// TODO: get pricelistitems

			// TODO: mlPriceList.setSites(priceList.getValidSites());

		}
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
