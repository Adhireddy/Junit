package com.kibo.ng.bis.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.AttributePage;
import com.kibo.ng.bis.jaxb.AttributePageData;
import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.ProductSkuLink;
import com.kibo.ng.bis.jaxb.Sku;
import com.kibo.ng.bis.jaxb.Sku.SkuOptionLinks;
import com.kibo.ng.bis.jaxb.SkuOptionLink;
import com.kibo.ng.bis.jaxb.SkuPrice;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.productadmin.AttributeVocabularyValue;
import com.mozu.api.contracts.productadmin.Product;
import com.mozu.api.contracts.productadmin.ProductExtra;
import com.mozu.api.contracts.productadmin.ProductExtraValue;
import com.mozu.api.contracts.productadmin.ProductInCatalogInfo;
import com.mozu.api.contracts.productadmin.ProductOption;
import com.mozu.api.contracts.productadmin.ProductOptionValue;
import com.mozu.api.contracts.productadmin.ProductVariation;
import com.mozu.api.resources.commerce.catalog.admin.CategoryResource;
import com.mozu.api.resources.commerce.catalog.admin.ProductResource;
import com.mozu.api.resources.commerce.catalog.admin.attributedefinition.producttypes.ProductTypeExtraResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductExtraResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductOptionResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductVariationResource;

@Service
public class SkuCommandService extends MozuApiService<Sku> {

	@Autowired
	InventoryCommandService inventoryCommandService;

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel configModel, CatalogModel catalogModel) {
		List<Sku> skus = importCommand.getSkus().getSku();
		for (Sku sku : skus) {
			importEntityRecord(importCommand, sku, marketLive, configModel, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Sku sku) {
		record.setSku(sku);
	}

	@Override
	public void insertEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		ProductResource productResource = new ProductResource(apiContext);
		
		
		updateEntity(sku, marketLive, catalogModel);

	}
	
	public void updateSku(Sku sku, Product ngBaseProduct, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		
		ProductVariationResource productVariationResource = new ProductVariationResource(apiContext);
		ProductOptionResource productOptionResource = new ProductOptionResource(apiContext);

		String productCode = sku.getCode();

		SkuOptionLinks mlSkuOptionLink = sku.getSkuOptionLinks();
		if(mlSkuOptionLink != null) {
			List<SkuOptionLink> mlSkuOptionList = mlSkuOptionLink.getSkuOptionLink();
			List<ProductOption> options = ngBaseProduct.getOptions();
			
			for (SkuOptionLink skuOptionLink : mlSkuOptionList) {
				for (ProductOption productOption : options) {
					if(productOption.getAttributeFQN().equals("tenant~"+skuOptionLink.getOptionTypeCode())) {
						List<ProductOptionValue> values = productOption.getValues();
						if(values == null) {
							values = new ArrayList<>();
							productOption.setValues(values);
						}
						boolean found = false;
						for (ProductOptionValue productOptionValue : values) {
							if(skuOptionLink.getOptionCode().equals(productOptionValue.getValue()))
								found = true;
						}
						
						if(!found) {
							ProductOptionValue value = new ProductOptionValue();
							value.setValue(skuOptionLink.getOptionCode());
							values.add(value);
							AttributeVocabularyValue attr1 = new AttributeVocabularyValue();
							attr1.setProductName(productCode);
							attr1.setValue(skuOptionLink.getOptionCode());
							value.setAttributeVocabularyValueDetail(attr1);
							
							productOptionResource.updateOption(productOption, ngBaseProduct.getProductCode(), productOption.getAttributeFQN());
							productVariationResource.getProductVariations(ngBaseProduct.getProductCode());
						}
					}
				}
			}
		}
		ProductResource productResource = new ProductResource(apiContext);

		ProductOption option = new ProductOption();
		List<ProductOptionValue> values = new ArrayList<>();
		option.setValues(values);

	//	option.setAttributeFQN("color");

		productOptionResource.addOption(option, ngBaseProduct.getProductCode());

		
	}
	

	@Override
	public void updateEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		ProductResource productResource = new ProductResource(apiContext);
		ProductVariationResource productVariationResource = new ProductVariationResource(apiContext);

		String productCode = sku.getCode();

		com.mozu.api.contracts.productadmin.Product ngProduct = productResource.getProduct(productCode);
		if (ngProduct == null) {
			List<com.mozu.api.contracts.productadmin.Product> ngProductList = productResource.getProducts(0, 1,
					"productCode", "isVariation eq true and productCode eq " + productCode, null, null, null, null)
					.getItems();
			if (ngProductList.size() > 0) {
				com.mozu.api.contracts.productadmin.Product variantNgProduct = ngProductList.get(0);

				ProductVariation productVariation = productVariationResource
						.getProductVariation(variantNgProduct.getBaseProductCode(), variantNgProduct.getVariationKey());

				if (productVariation != null) {
					// variant product

					List<SkuPrice> prices = sku.getPrices().getPrice();
					for (SkuPrice skuPrice : prices) {
						updatePrice(skuPrice, productVariation);
					}

					productVariation.setFixedWeight(sku.getWeight());
					if (sku.getAttributePages() != null) {
						List<AttributePage> attributePageList = sku.getAttributePages().getAttributePage();
						for (AttributePage attributePage : attributePageList) {
							AttributePageData data = attributePage.getAttributes();

							if ("UPC Code".equals(data.getName())) {
								productVariation.setUpc(data.getValue());
							}
						}
					}

					productVariationResource.updateProductVariation(productVariation,
							variantNgProduct.getBaseProductCode(), productVariation.getVariationkey());
				} else {

					// whether need to add variant if it is not exist?
					throw new Exception("No product or Product Variant found ");
				}
			} else {

				// Add product
				ngProduct = new Product();

				ngProduct.setProductCode(productCode);
				ngProduct.getAuditInfo().setCreateDate(XMLGregorianCalendarTodateTime(sku.getDateCreated()));
				ngProduct.getAuditInfo().setUpdateDate(XMLGregorianCalendarTodateTime(sku.getDateModified()));

				sku.getSkuTypeId();
				sku.getSkuTypeIdAsString();
				sku.getStatusCode();
				sku.isShippingIndicator();
				sku.isDeleted();
				sku.getDateActivate();
				sku.getDateDeleted();
				sku.isGiftWrap();

				if (sku.isOverWeight()) {
					// EXTRAWEIGHT: If the sku has an extra weight attribute
					// then send that value, otherwise blank.
					// EXTRACOST: If the sku has an overweight shipping charge,
					// send that value otherwise blank

					// if sku.isOverWeight()==true then sku price will have
					// overweightPrice
				}

				ngProduct.setIsTaxable(sku.isTaxable());

				ProductInCatalogInfo productInCatalogInfo = new ProductInCatalogInfo();
				productInCatalogInfo.setIsActive(sku.isActive());
				ngProduct.getProductInCatalogs().add(productInCatalogInfo);

				if (sku.getWeight() != 0.0) {
					// need to verify this
					// FIXEDWEIGHT: If the sku has weight attribute then send
					// that value, otherwise blank.
					ngProduct.getPackageWeight().setValue(sku.getWeight());
					ngProduct.getPackageWeight().setUnit("");
				}

				if (sku.isShippingChargeable()) {
					// match not found
				}

				// ProductSkuLink
				List<ProductSkuLink> productSkuLinkList = sku.getProductSkuLinks().getProductSkuLink();
				if (productSkuLinkList != null) {
					for (ProductSkuLink productSkuLink : productSkuLinkList) {
						productSkuLink.getSkuCode();
						productSkuLink.getDateCreated();

						if (!productSkuLink.isDefaultProduct()) {
							ngProduct.setBaseProductCode(productSkuLink.getProductCode());
						}
						ngProduct.setIsVariation(!productSkuLink.isDefaultProduct());

					}
				}

				ngProduct.setProductTypeId(sku.getSkuTypeId());
				ngProduct.setProductUsage(sku.getSkuTypeIdAsString());

				// AttributePages
				if (sku.getAttributePages() != null) {
					List<AttributePage> attributePageList = sku.getAttributePages().getAttributePage();
					for (AttributePage attributePage : attributePageList) {
						AttributePageData data = attributePage.getAttributes();

						if ("UPC Code".equals(data.getName())) {
							ngProduct.setUpc(data.getValue());
						}
						if ("Color Family".equals(data.getName())) {

						}
						if ("Size Family".equals(data.getName())) {

						}
						if ("BF Is Ormd".equals(data.getName())) {

						}
						if ("Email When Back in Stock".equals(data.getName())) {

						}

					}
				}

				// Price
				List<SkuPrice> prices = sku.getPrices().getPrice();
				for (SkuPrice skuPrice : prices) {
					updatePrice(skuPrice, ngProduct);
				}

				// skuOptionLinks
				if (sku.getSkuOptionLinks().getSkuOptionLink() != null) {
					List<SkuOptionLink> skuOptionLinkList = sku.getSkuOptionLinks().getSkuOptionLink();
					for (SkuOptionLink skuOptionLink : skuOptionLinkList) {
						// skuOptionLink.getOptionCode();
						skuOptionLink.getOptionTypeCode();
						// skuOptionLink.getSkuCode();

						AttributeVocabularyValue attributeVocabularyValueDetail = new AttributeVocabularyValue();

						attributeVocabularyValueDetail.setProductName(skuOptionLink.getSkuCode());
						attributeVocabularyValueDetail.setValue(skuOptionLink.getOptionCode());

						ProductOptionValue productOptionValue = new ProductOptionValue();
						productOptionValue.setAttributeVocabularyValueDetail(attributeVocabularyValueDetail);
						List<ProductOptionValue> values = new ArrayList<>();
						values.add(productOptionValue);
						ProductOption productOption = new ProductOption();
						productOption.setValues(values);
						List<ProductOption> options = new ArrayList<>();
						options.add(productOption);
						ngProduct.setOptions(options);

					}

				}

				productResource.addProduct(ngProduct);
			}
		} else {
			// update product

			if (sku.getAttributePages() != null) {
				List<AttributePage> attributePageList = sku.getAttributePages().getAttributePage();
				for (AttributePage attributePage : attributePageList) {
					AttributePageData data = attributePage.getAttributes();

					if ("UPC Code".equals(data.getName())) {
						ngProduct.setUpc(data.getValue());
					}
				}
			}

			List<SkuPrice> prices = sku.getPrices().getPrice();
			for (SkuPrice skuPrice : prices) {
				updatePrice(skuPrice, ngProduct);
			}

			productResource.updateProduct(ngProduct, productCode);

		}
		if (sku.getInventories() != null) {
			List<Inventory> inventories = sku.getInventories().getInventory();
			for (Inventory inventory : inventories) {
				inventoryCommandService.updateEntity(inventory, marketLive, catalogModel);
			}
		}

	}

	@Override
	public void updateInsertEntity(Sku sku, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		updateEntity(sku, marketLive, catalogModel);
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
		return "productCode";
	}

	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {

	}

	@Override
	public void decryptFields(List<CommandResult> results, ConfigModel config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInitialData(ConfigModel config) throws Exception {
		
	}

}
