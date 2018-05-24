package com.kibo.ng.bis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.datatype.XMLGregorianCalendar;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Output;
import com.kibo.ng.bis.jaxb.Products;
import com.kibo.ng.bis.jaxb.Sku;
import com.kibo.ng.bis.jaxb.Sku.Inventories;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.location.Location;
import com.mozu.api.contracts.productadmin.LocationInventory;
import com.mozu.api.contracts.productadmin.LocationInventoryCollection;
import com.mozu.api.contracts.productadmin.Product;
import com.mozu.api.resources.commerce.admin.LocationResource;
import com.mozu.api.resources.commerce.catalog.admin.ProductResource;
import com.mozu.api.resources.commerce.catalog.admin.products.LocationInventoryResource;

// TODO: Auto-generated Javadoc
/**
 * The Class InventoryCommandService.
 */
@Service
public class InventoryCommandService extends MozuApiService<Inventory> {

	/** The Constant ALLOWBACKORDER. */
	public static final String ALLOWBACKORDER = "AllowBackOrder";

	/** The Constant SHOW_OUT_OF_STOCK_MESSAGE. */
	public static final String SHOW_OUT_OF_STOCK_MESSAGE = "Show out of stock message";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#importCommand(com.kibo.ng.bis.jaxb
	 * .ImportCommand, com.kibo.ng.bis.jaxb.Marketlive,
	 * com.kibo.ng.bis.model.ConfigModel)
	 */
	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel configModel, CatalogModel catalogModel) {
		List<Inventory> inventories = importCommand.getInventories().getInventory();
		for (Inventory inventory : inventories) {
			importEntityRecord(importCommand, inventory, marketLive, configModel, catalogModel);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#setInputRecord(com.kibo.ng.bis.
	 * jaxb.InputRecord, java.lang.Object)
	 */
	@Override
	public void setInputRecord(InputRecord record, Inventory inventory) {
		record.setInventory(inventory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#insertEntity(java.lang.Object,
	 * com.kibo.ng.bis.jaxb.Marketlive)
	 */
	@Override
	public void insertEntity(Inventory inventory, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		updateEntity(inventory, marketLive, catalogModel);
	}

	/**
	 * 
	 * This method updates maps ML inventroy data to NG LocationInventory and
	 * updates that by calling Mozu Service
	 * 
	 */
	@Override
	public void updateEntity(Inventory inventory, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		ProductResource productResource = new ProductResource(apiContext);
		String productCode = inventory.getCode();
		String variantProductCode = null;
		String skuCode = inventory.getSkuCode();
		String inventorySetCode = inventory.getInventorySetCode();
		int stock = inventory.getStock();
		XMLGregorianCalendar dateRestock = inventory.getDateRestock();

		DateTime dateRestockTemp = null;
		if (dateRestock != null)
			dateRestockTemp = new DateTime(dateRestock.toGregorianCalendar().getTime());
		else
			dateRestockTemp = new DateTime();

		Product ngProduct = productResource.getProduct(productCode);
		if (ngProduct == null) {
			List<com.mozu.api.contracts.productadmin.Product> ngProducts = productResource.getProducts(0, 1,
					"productCode", "isVariation eq true and productCode eq " + productCode, null, null, null, null)
					.getItems();
			if (ngProducts.size() > 0) {
				ngProduct = ngProducts.get(0);
				productCode = ngProduct.getBaseProductCode();
				variantProductCode = ngProduct.getProductCode();
			} else {
				logger.error("Product Not found for code : {}", productCode);
				throw new Exception("Product Not found for code :" + productCode);
			}
		}

		if (dateRestockTemp.isAfterNow()) {
			// inventory.getDateRestock() is future date
			ngProduct.getInventoryInfo().setOutOfStockBehavior(ALLOWBACKORDER);
			ngProduct.getInventoryInfo().setManageStock(true);
		} else {
			// inventory.getDateRestock() is past date
			ngProduct.getInventoryInfo().setOutOfStockBehavior(SHOW_OUT_OF_STOCK_MESSAGE);
			ngProduct.getInventoryInfo().setManageStock(true);
		}

		productResource.updateProduct(ngProduct, productCode);
		logger.info("Product updated for code {}", productCode);

		LocationInventoryResource locationInventoryResource = new LocationInventoryResource(apiContext);

		String code = variantProductCode != null ? variantProductCode : productCode;
		LocationInventory locationInventory = locationInventoryResource.getLocationInventory(code, inventorySetCode);
		if (locationInventory == null && skuCode != null) {
			locationInventory = locationInventoryResource.getLocationInventory(skuCode, inventorySetCode);
			productCode = skuCode;
		}

		if (locationInventory == null) {
			logger.error("Location Inventory Not found for code : {} and skuCode {}", productCode, skuCode);
			throw new Exception("Location Inventory Not found for code :" + productCode + " and skuCode :" + skuCode);
		}

		locationInventoryResource.deleteLocationInventory(code, inventorySetCode);

		locationInventory.setStockOnHand(stock);

		List<LocationInventory> list = new ArrayList<>();
		list.add(locationInventory);
		locationInventoryResource.addLocationInventory(list, code);
		logger.info("Location Inventory {} is added for code {}", inventorySetCode, code);
	}

	/**
	 * As inventory doesn't have insert , so calling updateEntity.
	 *
	 * @param inventory
	 *            the inventory
	 * @param marketLive
	 *            the market live
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void updateInsertEntity(Inventory inventory, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		updateEntity(inventory, marketLive, catalogModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#exportEntity(com.kibo.ng.bis.jaxb.
	 * ExportCommand, com.kibo.ng.bis.jaxb.Marketlive)
	 */
	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub

		Random random = new Random();
		return random.nextBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#exportEntityByCriteria(com.kibo.ng
	 * .bis.jaxb.Marketlive, java.lang.String, java.lang.String)
	 */
	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {
		// TODO Auto-generated method stub
		//filterStr="productCode eq ACC1";

		String productCode="ACC1";
		
		LocationInventoryResource locationInventoryResource =new LocationInventoryResource(apiContext);
		
		LocationInventoryCollection locationInventoryCollection	=locationInventoryResource.getLocationInventories(productCode, 0, 100, orderByStr, filterStr, null);
		
		List<LocationInventory> locationInventories=locationInventoryCollection.getItems();
		
		for (LocationInventory locationInventory : locationInventories) {
			
			Sku sku = new Sku();
			
			Inventory mlInventory=new Inventory();
			mlInventory.setCode(locationInventory.getProductCode());
			mlInventory.setStock(locationInventory.getStockAvailable());
			mlInventory.setMinStock(locationInventory.getStockOnHand());
			mlInventory.setInventorySetCode(locationInventory.getLocationCode());
			
			// TODO :set  dateRestock to ml
			com.kibo.ng.bis.jaxb.Sku.Inventories inventories = new com.kibo.ng.bis.jaxb.Sku.Inventories();
			inventories.getInventory().add(mlInventory);
			sku.setInventories(inventories);
		
			
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kibo.ng.bis.service.MozuApiService#getDefaultOrderByString()
	 */
	@Override
	public String getDefaultOrderByString() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kibo.ng.bis.service.MozuApiService#exportEntityByCode(com.kibo.ng.bis
	 * .jaxb.Marketlive, com.kibo.ng.bis.jaxb.FindByCodeParameters)
	 */
	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {
		// TODO Auto-generated method stub

       LocationInventoryResource locationInventoryResource =new LocationInventoryResource(apiContext);
		
		LocationInventoryCollection locationInventoryCollection	=locationInventoryResource.getLocationInventories(code.getCode());
		
		List<LocationInventory> locationInventories=locationInventoryCollection.getItems();
		
		com.kibo.ng.bis.jaxb.Sku.Inventories inventories = new com.kibo.ng.bis.jaxb.Sku.Inventories();
		
		Sku sku = new Sku();
		
		for (LocationInventory locationInventory : locationInventories) {
			
			Inventory mlInventory=new Inventory();
			mlInventory.setCode(locationInventory.getProductCode());
			mlInventory.setStock(locationInventory.getStockAvailable());
			mlInventory.setMinStock(locationInventory.getStockOnHand());
			mlInventory.setInventorySetCode(locationInventory.getLocationCode());
			
			// TODO :set  dateRestock to ml
			
			inventories.getInventory().add(mlInventory);
			sku.setInventories(inventories);
		
			
		}
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kibo.ng.bis.service.MozuApiService#decryptFields(java.util.List,
	 * com.kibo.ng.bis.model.ConfigModel)
	 */
	@Override
	public void decryptFields(List<CommandResult> results, ConfigModel config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInitialData(ConfigModel config) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
