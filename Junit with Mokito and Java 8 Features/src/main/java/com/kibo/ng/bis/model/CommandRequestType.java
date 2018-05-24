package com.kibo.ng.bis.model;

import java.util.EnumSet;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.service.CategoryCommandService;
import com.kibo.ng.bis.service.CustomerCommandService;
import com.kibo.ng.bis.service.FamilyCommandService;
import com.kibo.ng.bis.service.InventoryCommandService;
import com.kibo.ng.bis.service.MozuApiService;
import com.kibo.ng.bis.service.OptionTypeCommandService;
import com.kibo.ng.bis.service.OptionsCommandService;
import com.kibo.ng.bis.service.OrderCommandService;
import com.kibo.ng.bis.service.OrderStatusCommandService;
import com.kibo.ng.bis.service.PriceListCommandService;
import com.kibo.ng.bis.service.PricingCommandService;
import com.kibo.ng.bis.service.ProductCommandService;
import com.kibo.ng.bis.service.SkuCommandService;

public enum CommandRequestType {

	INVENTORY("Inventory"),
	PRICELIST("Price List"),
	SKU("Skus"),
	CATEGORY("Category"),
	OPTIONTYPE("Option Type"),
	OPTIONS("Option"),
	PRODUCT("Product"),
	FAMILY("Family"),
	PRICING("Pricing"),
	CUSTOMER("Customer"),
	ORDER("OrderImport"),
	ORDERSTATUSIMPORT("Order Status Import");
	
	private MozuApiService mozuApiService;
	
	public MozuApiService getMozuApiService() {
		return mozuApiService;
	}

	public String value;
	
	private CommandRequestType(String value) {
		this.value = value;
	}
	
	public static CommandRequestType getType(ImportCommand importCommand) {
    	if (importCommand.getCategories() != null) {
    		return CommandRequestType.CATEGORY;
		} else if (importCommand.getOptionTypes() != null) {
			return CommandRequestType.OPTIONTYPE;
		} else if (importCommand.getOptions() != null) {
			return CommandRequestType.OPTIONS;
		} else if (importCommand.getSkus() != null) {
			return CommandRequestType.SKU;
		} else if (importCommand.getProducts() != null) {
			return CommandRequestType.PRODUCT;
		} else if (importCommand.getFamilies() != null) {
			return CommandRequestType.FAMILY;
		} else if (importCommand.getInventories() != null) {
			return CommandRequestType.INVENTORY;
		} else if (importCommand.getPriceLists() != null) {
			return CommandRequestType.PRICELIST;
		} else if (importCommand.getCustomers()!= null) {
			return CommandRequestType.CUSTOMER;
		} else if (importCommand.getOrders() != null) {
			return CommandRequestType.ORDER;
		} else if (importCommand.getOrders() != null) {  // Possible bug because Order will be same
			return CommandRequestType.ORDERSTATUSIMPORT;
		}
		return null;
	}
	
	public static CommandRequestType getType(ExportCommand exportCommand) {
		String entity = null;
		
		if("findByCriteria".equals(exportCommand.getType())) {
			entity = exportCommand.getFindByCriteriaParameters().getEntityType();
			
		} else if("findByCode".equals(exportCommand.getType())) {
			entity = exportCommand.getFindByCodeParameters().getEntityType();
		}
		
		if("product".equals(entity)) {
    		return CommandRequestType.PRODUCT;
		} else if("inventory".equals(entity)) {
    		return CommandRequestType.INVENTORY;
		} else if("order".equals(entity)) {
			return CommandRequestType.ORDER;
		}
		return null;
	}
	
	@Service
    private static class MozuApiInjector{
		
	
		@Autowired
		CategoryCommandService categoryCommandService;
		
		@Autowired
		OptionTypeCommandService optionTypeCommandService;
		
		@Autowired
		OptionsCommandService optionsCommandService;
		
		@Autowired
		ProductCommandService productCommandService;
		
		@Autowired
		FamilyCommandService familyCommandService;
		
		@Autowired
		InventoryCommandService inventoryCommandService;
		
		@Autowired
		PriceListCommandService priceListCommandService;
		
		@Autowired
		PricingCommandService pricingCommandService; 
		
		@Autowired
		CustomerCommandService customerCommandService;
		
		@Autowired
		OrderCommandService orderCommandService;
		
		@Autowired
		OrderStatusCommandService orderStatusCommandService;
		
		@Autowired
		SkuCommandService skuCommandService;
		
		
		@PostConstruct
        public void postConstruct() {
		
            for (CommandRequestType type : EnumSet.allOf(CommandRequestType.class)){
	            switch (type) {
		            case CATEGORY:
			            type.mozuApiService = categoryCommandService;
		        		break;
		            case SKU:
			            type.mozuApiService = skuCommandService;
		        		break;
		            case OPTIONTYPE:
			            type.mozuApiService = optionTypeCommandService;
		        		break;
		            case OPTIONS:
			            type.mozuApiService = optionsCommandService;
		        		break;
		            case PRODUCT:
			            type.mozuApiService = productCommandService;
		        		break;
		            case FAMILY:
			            type.mozuApiService = familyCommandService;
		        		break;
		            case INVENTORY:
			            type.mozuApiService = inventoryCommandService;
		        		break;
		            case PRICELIST:
			            type.mozuApiService = priceListCommandService;
		        		break;
		            case PRICING:
			            type.mozuApiService = pricingCommandService;
		        		break;
	            	case CUSTOMER:
	            		type.mozuApiService = customerCommandService;
	            		break;
	            	case ORDER:
	            		type.mozuApiService = orderCommandService;
	            		break;
	            	case ORDERSTATUSIMPORT:
	            		type.mozuApiService = orderStatusCommandService;
	            		break;
	            }
            }
		}
	}

	//public abstract void decryptValues(Marketlive marketLive);
}
