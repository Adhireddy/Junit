package com.kibo.ng.bis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.AttributePage;
import com.kibo.ng.bis.jaxb.AttributePageData;
import com.kibo.ng.bis.jaxb.AttributePageValues;
import com.kibo.ng.bis.jaxb.AttributePages;
import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Output;
import com.kibo.ng.bis.jaxb.Product;
import com.kibo.ng.bis.jaxb.Product.DescriptionPages;
import com.kibo.ng.bis.jaxb.Product.DescriptionPages.DescriptionPage;
import com.kibo.ng.bis.jaxb.Product.DescriptionPages.DescriptionPage.SiteDescriptions;
import com.kibo.ng.bis.jaxb.Product.DescriptionPages.DescriptionPage.SiteDescriptions.SiteDescription;
import com.kibo.ng.bis.jaxb.Product.ProductCategoryLinks;
import com.kibo.ng.bis.jaxb.Product.ProductOptionTypeLinks;
import com.kibo.ng.bis.jaxb.Product.ProductSkuLinks;
import com.kibo.ng.bis.jaxb.Product.RelProdProductLinks;
import com.kibo.ng.bis.jaxb.Product.SearchEngineSupportPages;
import com.kibo.ng.bis.jaxb.Product.SearchEngineSupportPages.SearchEngineSupportPage;
import com.kibo.ng.bis.jaxb.Product.SearchEngineSupportPages.SearchEngineSupportPage.SiteSearchEngineSupports;
import com.kibo.ng.bis.jaxb.Product.SearchEngineSupportPages.SearchEngineSupportPage.SiteSearchEngineSupports.SiteSearchEngineSupport;
import com.kibo.ng.bis.jaxb.Product.SiteProductLinks;
import com.kibo.ng.bis.jaxb.Product.SiteSpecificProperties;
import com.kibo.ng.bis.jaxb.Product.SiteSpecificProperties.SiteSpecificProperty;
import com.kibo.ng.bis.jaxb.Product.Skus;
import com.kibo.ng.bis.jaxb.Product.UpsellProductLinks;
import com.kibo.ng.bis.jaxb.ProductCategoryLink;
import com.kibo.ng.bis.jaxb.ProductOptionTypeLink;
import com.kibo.ng.bis.jaxb.ProductSkuLink;
import com.kibo.ng.bis.jaxb.Products;
import com.kibo.ng.bis.jaxb.RelProdProductLink;
import com.kibo.ng.bis.jaxb.SiteProductLink;
import com.kibo.ng.bis.jaxb.Sku;
import com.kibo.ng.bis.jaxb.Sku.Prices;
import com.kibo.ng.bis.jaxb.SkuPrice;
import com.kibo.ng.bis.jaxb.UpsellProductLink;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.core.Measurement;
import com.mozu.api.contracts.location.Location;
import com.mozu.api.contracts.productadmin.ActiveDateRange;
import com.mozu.api.contracts.productadmin.Attribute;
import com.mozu.api.contracts.productadmin.Category;
import com.mozu.api.contracts.productadmin.LocationInventory;
import com.mozu.api.contracts.productadmin.ProductCategory;
import com.mozu.api.contracts.productadmin.ProductExtra;
import com.mozu.api.contracts.productadmin.ProductInCatalogInfo;
import com.mozu.api.contracts.productadmin.ProductLocalizedContent;
import com.mozu.api.contracts.productadmin.ProductLocalizedSEOContent;
import com.mozu.api.contracts.productadmin.ProductOption;
import com.mozu.api.contracts.productadmin.ProductPrice;
import com.mozu.api.contracts.productadmin.ProductProperty;
import com.mozu.api.contracts.productadmin.ProductPropertyValue;
import com.mozu.api.contracts.productadmin.ProductPropertyValueLocalizedContent;
import com.mozu.api.contracts.productadmin.ProductType;
import com.mozu.api.resources.commerce.admin.LocationResource;
import com.mozu.api.resources.commerce.catalog.admin.CategoryResource;
import com.mozu.api.resources.commerce.catalog.admin.LocationInventoryResource;
import com.mozu.api.resources.commerce.catalog.admin.ProductResource;
import com.mozu.api.resources.commerce.catalog.admin.attributedefinition.AttributeResource;
import com.mozu.api.resources.commerce.catalog.admin.attributedefinition.ProductTypeResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductExtraResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductOptionResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductPropertyResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductVariationResource;

@Service
public class ProductCommandService extends MozuApiService<Product> {

	@Autowired
	InventoryCommandService inventoryCommandService;

	@Autowired
	SkuCommandService skuCommandService;

	public static HashMap<ArrayList<String>, ProductType> productTypeMap = new HashMap<>();
	public static HashMap<String, Attribute> attributeMap = new HashMap<>();

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel configModel, CatalogModel catalogModel) {
		List<Product> products = importCommand.getProducts().getProduct();
		for (Product product : products) {
			importEntityRecord(importCommand, product, marketLive, configModel, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Product product) {
		record.setProduct(product);
	}

	@Override
	public void insertEntity(Product product, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		com.mozu.api.contracts.productadmin.Product ngProduct = new com.mozu.api.contracts.productadmin.Product();

		updateProduct(product, ngProduct, marketLive, catalogModel, false);
		/*ngProduct.setProductCode(product.getCode());

		List<ProductProperty> ngProperties = new ArrayList<>();

		readAttribute(product.getAttributePages(), ngProperties, false);

		ngProduct.setProperties(ngProperties);
*/
	}

	public void updateProduct(Product product, com.mozu.api.contracts.productadmin.Product ngProduct,
			Marketlive marketLive, CatalogModel catalogModel, boolean isUpdate) throws Exception {
		ProductResource productResource = new ProductResource(apiContext);

		ngProduct.setProductCode(product.getCode());

		ProductInCatalogInfo catalog = getCatalog(ngProduct, catalogModel);
		setActiveDateRange(catalog, product.getDateActivate(), product.getDateDeactivate());
		DescriptionPages pages = product.getDescriptionPages();
		if(pages != null) {
			List<DescriptionPage> descriptionPageList = product.getDescriptionPages().getDescriptionPage();
			updateProductDescription(ngProduct, catalog, descriptionPageList, catalogModel);
		}
		
		ProductCategoryLinks productCategoryLinks = product.getProductCategoryLinks();
		if(productCategoryLinks != null) {
			List<ProductCategoryLink> links = productCategoryLinks.getProductCategoryLink();
			updateProductCategorylinks(links, catalog);
		}
		
		List<String> keyWords = new ArrayList<>();
		ProductOptionTypeLinks productOptionLinks = product.getProductOptionTypeLinks();
		if(productOptionLinks != null) {
			List<ProductOptionTypeLink> optionTypeLinks = productOptionLinks.getProductOptionTypeLink();
			for (ProductOptionTypeLink productOptionTypeLink : optionTypeLinks) {
				String option = productOptionTypeLink.getOptionTypeCode();
				keyWords.add(option);
				/*List<ProductOption> ngOptions = ngProduct.getOptions();
				if(ngOptions == null) {
					ngOptions = new ArrayList<>();
					ngProduct.setOptions(ngOptions);
				}*/
			}
			if(optionTypeLinks.size() > 0 ) {
				ngProduct.setHasConfigurableOptions(true);
				ngProduct.setProductUsage("Configurable");
			} else {
				ngProduct.setProductUsage("Standard");
			}
		}
		
		Collections.sort(keyWords);

		ProductType productType = productTypeMap.get(keyWords);
		if(productType != null) {
			ngProduct.setProductTypeId(productType.getId());
		}
	
		ProductPrice price  = new ProductPrice();
		price.setPrice(0.0);
		ngProduct.setPrice(price);
		
		List<ProductProperty> ngProperties = ngProduct.getProperties();
		if(ngProperties == null) {
			ngProperties = new ArrayList<ProductProperty>();
			ngProduct.setProperties(ngProperties);
		}
		
		readAttribute(product.getAttributePages(), ngProperties, false);
	
		// TODO : added

		Measurement packageWeight = new Measurement();

		packageWeight.setUnit("lbs");
		packageWeight.setValue(1.0);

		ngProduct.setPackageWeight(packageWeight);

		//
		
		ProductOptionTypeLinks optionLinks = product.getProductOptionTypeLinks();
		if(optionLinks != null) {
			List<ProductOption> options = ngProduct.getOptions();
			
			if(options == null) {
				options = new ArrayList<>();
				ngProduct.setOptions(options);
			}

			List<ProductOptionTypeLink> optionList = optionLinks.getProductOptionTypeLink();
			
			for (ProductOptionTypeLink mlProductOptionTypeLink : optionList) {
				ProductOption ngOption = null;
				for (ProductOption ngProductOption : options) {
					if(("tenant~"+mlProductOptionTypeLink.getOptionTypeCode()).equals(ngProductOption.getAttributeFQN())) {
						ngOption = ngProductOption;
						break;
					}
				}
				if(ngOption == null) {
					ngOption = new ProductOption();
					ngOption.setAttributeFQN("tenant~"+mlProductOptionTypeLink.getOptionTypeCode());
					options.add(ngOption);
				}
			}
		}

		if(isUpdate)
			productResource.updateProduct(ngProduct, ngProduct.getProductCode());
		else 
			productResource.addProduct(ngProduct);
		
		Skus skus = product.getSkus();
		if(skus != null) {
			List<Sku> skuList = skus.getSku();
			if(skuList != null) {
				for (Sku sku : skuList) {
					skuCommandService.updateSku(sku, ngProduct, marketLive, catalogModel);
				}
			}
		}
		
		productResource.deleteProduct(ngProduct.getProductCode());

		
	}

	private void updateProductCategorylinks(List<ProductCategoryLink> links, ProductInCatalogInfo catalog) {
		if(links != null) {
			List<ProductCategory> tempList = new ArrayList<>();
			for (ProductCategoryLink productCategoryLink : links) {
				List<ProductCategory> list = catalog.getProductCategories();
				if(list == null) {
					list = new ArrayList<>();
					catalog.setProductCategories(list);
				}
				boolean found = false;
				for (ProductCategory productCategory : list) {
					if(productCategory.getCategoryId() == productCategoryLink.getCategoryId()) {
						found = true;
						break;
					}
				}
				if(!found) {
					ProductCategory cat = new ProductCategory();
					cat.setCategoryId(productCategoryLink.getCategoryId());
					tempList.add(cat);
				}
			}
			catalog.getProductCategories().addAll(tempList);
		}
	}
	
	private void updateProductDescription(com.mozu.api.contracts.productadmin.Product ngProduct, ProductInCatalogInfo catalog, List<DescriptionPage> descriptionPageList, CatalogModel catalogModel) {
		if(descriptionPageList != null) {
			descriptionPageList.get(0).getSiteDescriptions().getSiteDescription();
			for (DescriptionPage descriptionPage : descriptionPageList) {
				SiteDescriptions siteDescriptions = descriptionPage.getSiteDescriptions();
				if(siteDescriptions != null) {
					List<SiteDescription> list = siteDescriptions.getSiteDescription();
					if(list != null) {
						for (SiteDescription siteDescription : list) {
							if(catalogModel.getSiteName().equals(siteDescription.getSiteCode())) {
								ProductLocalizedContent content = catalog.getContent();
								if(content == null) {
									content = new ProductLocalizedContent();
									catalog.setContent(content);
								}
								content.setLocaleCode(descriptionPage.getLocale());
								content.setProductFullDescription(siteDescription.getLong());
								content.setProductName(siteDescription.getName().replaceAll("\\s",""));//TODO product won't take whitespace 
								content.setProductShortDescription(siteDescription.getShort());
								ngProduct.setContent(content); // #TODO i am not sure how to handle this. content should go to Catalog or to product, because if Product content is empty then it won't allow to add product in ng
								ProductLocalizedSEOContent seoContent = catalog.getSeoContent();
								if(seoContent == null) {
									seoContent = new ProductLocalizedSEOContent();
									catalog.setSeoContent(seoContent);
								}
								seoContent.setMetaTagKeywords(siteDescription.getKeywords());
							}
						}
					}
				}
			}
		}
	}
	
	private void setActiveDateRange(ProductInCatalogInfo catalog, XMLGregorianCalendar dateActivate, XMLGregorianCalendar DateDeactivate) {
		ActiveDateRange range = catalog.getActiveDateRange();
		if(range == null) {
			range = new ActiveDateRange();
			catalog.setActiveDateRange(range);
		}
		if(dateActivate != null) {
			DateTime startDate = new DateTime(dateActivate.toGregorianCalendar().getTime());
			range.setStartDate(startDate);
		}
		
		if(DateDeactivate != null) {
			DateTime endDate = new DateTime(DateDeactivate.toGregorianCalendar().getTime());
			range.setEndDate(endDate);
		}
	}
	
	@Override
	public void updateInsertEntity(Product product, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		ProductResource productResource = new ProductResource(apiContext);
		com.mozu.api.contracts.productadmin.Product ngProduct = productResource.getProduct(product.getCode());
		if (ngProduct == null)
			insertEntity(product, marketLive, catalogModel);
		else
			updateProduct(product, ngProduct, marketLive, catalogModel, true);
	}

	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private ProductInCatalogInfo getCatalog(com.mozu.api.contracts.productadmin.Product ngProduct, CatalogModel catalogModel) {
		List<ProductInCatalogInfo> catalogList = ngProduct.getProductInCatalogs();
		if(catalogList == null) {
			catalogList = new ArrayList<>();
			ngProduct.setProductInCatalogs(catalogList);
			return addCatalog(catalogList, catalogModel);
		} else {
			for (ProductInCatalogInfo productInCatalogInfo : catalogList) {
				if(productInCatalogInfo.getCatalogId() == catalogModel.getCatalogId())
					return productInCatalogInfo;
			}
			return addCatalog(catalogList, catalogModel);
		}
	}
	
	private ProductInCatalogInfo addCatalog(List<ProductInCatalogInfo> catalogList, CatalogModel catalogModel) {
		ProductInCatalogInfo cat1 = new ProductInCatalogInfo();
		catalogList.add(cat1);
		cat1.setCatalogId(catalogModel.getCatalogId());
		return cat1;
	}
	
	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {
		CommandResult result = marketLive.getResults().getResult().get(0);
		result.setOutput(new Output());

		Products productsExport = new Products();
		result.getOutput().setProducts(productsExport);
		List<Product> productsList = productsExport.getProduct();

		ProductResource productResource = new ProductResource(apiContext);
		List<com.mozu.api.contracts.productadmin.Product> ngProductsList = null;
		int startIndex = 0;
		do {
			ngProductsList = productResource
					.getProducts(startIndex, PAGE_SIZE, orderByStr, filterStr, null, null, null, null).getItems();
			for (com.mozu.api.contracts.productadmin.Product ngProduct : ngProductsList) {
				Product mlProduct = new Product();

				mlProduct.setCode(ngProduct.getProductCode());

				// TODO Add logic to get ngProduct.getShippingClassId();
				mlProduct.setChargeShipping(true);

				// sku
				Sku sku = new Sku();

				sku.setSkuTypeId(ngProduct.getProductTypeId());

				// Sku Prices
				SkuPrice skuPrice = new SkuPrice();

				skuPrice.setCurrency(ngProduct.getPrice().getIsoCurrencyCode());
				skuPrice.setOverweightPrice(new BigDecimal(0.0));
				skuPrice.setRegularPrice(new BigDecimal(ngProduct.getPrice().getPrice()));
				skuPrice.setSalePrice(new BigDecimal(ngProduct.getPrice().getSalePrice()));

				Prices prices = new Prices();
				prices.getPrice().add(skuPrice);
				sku.setPrices(prices);

				// Sku Inventories
				String productCode = ngProduct.getBaseProductCode() != null ? ngProduct.getBaseProductCode()
						: ngProduct.getProductCode();

				// TODO: fetching of locationInventory logic need to be
				// determine

				LocationResource locationResource = new LocationResource(apiContext);
				LocationInventoryResource locationInventoryResource = new LocationInventoryResource(apiContext);
				List<Location> locationList = locationResource.getLocations().getItems();

				for (Location location : locationList) {
					List<LocationInventory> locationInventoryList = locationInventoryResource
							.getLocationInventories(location.getCode(), 0, 1000, null,
									"productcode eq " + productCode, null, null)
							.getItems();

					if (locationInventoryList.size() > 0) {
						logger.info("LocationInventroy -- " + locationInventoryList.size());
					}
				}

				List<LocationInventory> locationInventoryList = locationInventoryResource
						.getLocationInventories(null, 0, 1000, null, "productcode eq " + productCode, null, null)
						.getItems();

				if (locationInventoryList == null) {
					throw new Exception("Location Inventroy not found for code " + productCode);
				}

				if (locationInventoryList != null) {
					for (LocationInventory locationInventory : locationInventoryList) {
						Inventory inventory = new Inventory();

						inventory.setCode(locationInventory.getProductCode());
						inventory.setInventorySetCode(locationInventory.getLocationCode());
						inventory.setStock(locationInventory.getStockAvailable());

						com.kibo.ng.bis.jaxb.Sku.Inventories inventories = new com.kibo.ng.bis.jaxb.Sku.Inventories();
						inventories.getInventory().add(inventory);
						sku.setInventories(inventories);
					}
				}

				Skus skus = new Skus();
				skus.getSku().add(sku);
				mlProduct.setSkus(skus);

				// ProductOptionTypeLinks
				if (ngProduct.getHasConfigurableOptions()) {

					ProductOptionTypeLink productOptionTypeLink = new ProductOptionTypeLink();

					productOptionTypeLink.setProductCode(ngProduct.getProductCode());
					productOptionTypeLink.setIsSwatch(false);
					productOptionTypeLink.setOrdinal(0);
					productOptionTypeLink.setOptionTypeCode(null);

					ProductOptionTypeLinks productOptionTypeLinks = new ProductOptionTypeLinks();
					productOptionTypeLinks.getProductOptionTypeLink().add(productOptionTypeLink);

					mlProduct.setProductOptionTypeLinks(productOptionTypeLinks);
				}

				// UpsellProductLinks

				UpsellProductLinks upsellProductLinks = new UpsellProductLinks();
				UpsellProductLink upsellProductLink = new UpsellProductLink();

				upsellProductLink.setProductCode(ngProduct.getProductCode());
				upsellProductLink.setUpsellProductCode(ngProduct.getProductCode());
				upsellProductLink.setPrice(new BigDecimal(ngProduct.getPrice().getPrice()));
				/*upsellProductLink
						.setDateCreated(dateTimeToXMLGregorianCalendar(ngProduct.getAuditInfo().getCreateDate()));*/

				upsellProductLinks.getUpsellProductLink().add(upsellProductLink);
				mlProduct.setUpsellProductLinks(upsellProductLinks);

				// TODO: Non-identified ML fields mapping with NG fields

				// Catalog fetching logic
				List<com.mozu.api.contracts.productadmin.Product> productList = productResource.getProducts(0, 50,
						"", "productInCatalogs.isActive eq true and productCode eq " + ngProduct.getProductCode(),
						"", null, false, "").getItems();

				if (productList.size() > 0) {
					mlProduct.setActive(true);
				} else {
					mlProduct.setActive(false);
				}

				// TODO: ProductCategoryLinks

				CategoryResource categoryResource = new CategoryResource(apiContext);
				ProductCategoryLinks productCategoryLinks = new ProductCategoryLinks();

				List<ProductInCatalogInfo> productInCatalogInfoList = ngProduct.getProductInCatalogs();
				for (ProductInCatalogInfo productInCatalogInfo : productInCatalogInfoList) {
					List<ProductCategory> productCategoryList = productInCatalogInfo.getProductCategories();

					productInCatalogInfo.getCatalogId();

					for (ProductCategory productCategory : productCategoryList) {
						Category category = categoryResource.getCategory(productCategory.getCategoryId());

						category.getCatalogId();

						ProductCategoryLink productCategoryLink = new ProductCategoryLink();

						productCategoryLink.setProductCode(ngProduct.getProductCode());
						productCategoryLink.setCategoryCode(category.getCategoryCode());
						productCategoryLink.setCategoryId(category.getId());
						productCategoryLink.setDefaultCategory(true);
						productCategoryLink.setOrdinal(0);
						productCategoryLink.setProductId(0);

						productCategoryLinks.getProductCategoryLink().add(productCategoryLink);

					}
				}

				mlProduct.setProductCategoryLinks(productCategoryLinks);

				// TODO: AttributePages
				AttributePages attributePages = new AttributePages();
				AttributePage attributePage = new AttributePage();
				AttributePageData attributePageData = new AttributePageData();

				attributePageData.setFirstName(null);
				attributePageData.setLastName(null);
				attributePageData.setCity(null);

				attributePage.setAttributes(attributePageData);
				attributePages.getAttributePage().add(attributePage);

				mlProduct.setAttributePages(attributePages);

				// TODO: SiteSpecificProperties
				SiteSpecificProperties siteSpecificProperties = new SiteSpecificProperties();
				SiteSpecificProperty siteSpecificPropertys = new SiteSpecificProperty();

				siteSpecificProperties.getSiteSpecificProperty().add(siteSpecificPropertys);
				mlProduct.setSiteSpecificProperties(siteSpecificProperties);

				// TODO: SiteProductLink
				SiteProductLinks siteProductLinks = new SiteProductLinks();

				SiteProductLink siteProductLink = new SiteProductLink();
				siteProductLink.setProductCode(ngProduct.getBaseProductCode());
				siteProductLink.setSiteCode(null);

				siteProductLinks.getSiteProductLink().add(siteProductLink);
				mlProduct.setSiteProductLinks(siteProductLinks);

				// TODO : SearchEngineSupportPages
				SearchEngineSupportPages searchEngineSupportPages = new SearchEngineSupportPages();
				SearchEngineSupportPage searchEngineSupportPage = new SearchEngineSupportPage();
				SiteSearchEngineSupports siteSearchEngineSupports = new SiteSearchEngineSupports();

				SiteSearchEngineSupport siteSearchEngineSupport = new SiteSearchEngineSupport();

				siteSearchEngineSupports.getSiteSearchEngineSupport().add(siteSearchEngineSupport);
				searchEngineSupportPage.setSiteSearchEngineSupports(siteSearchEngineSupports);

				searchEngineSupportPages.getSearchEngineSupportPage().add(searchEngineSupportPage);
				mlProduct.setSearchEngineSupportPages(searchEngineSupportPages);

				// RelProdProductLinks
				RelProdProductLinks relProdProductLinks = new RelProdProductLinks();
				RelProdProductLink relProdProductLink = new RelProdProductLink();

				relProdProductLink.setProductCode(ngProduct.getProductCode());
				/*relProdProductLink
						.setDateCreated(dateTimeToXMLGregorianCalendar(ngProduct.getAuditInfo().getCreateDate()));*/

				relProdProductLinks.getRelProdProductLink().add(relProdProductLink);
				mlProduct.setRelProdProductLinks(relProdProductLinks);

				// ProductSkuLinks
				ProductSkuLinks productSkuLinks = new ProductSkuLinks();
				ProductSkuLink productSkuLink = new ProductSkuLink();

				productSkuLink.setProductCode(ngProduct.getProductCode());
				productSkuLink.setSkuCode(ngProduct.getProductCode());
				/*productSkuLink
						.setDateCreated(dateTimeToXMLGregorianCalendar(ngProduct.getAuditInfo().getCreateDate()));*/
				productSkuLink.setDefaultProduct(true);

				productSkuLinks.getProductSkuLink().add(productSkuLink);
				mlProduct.setProductSkuLinks(productSkuLinks);

				// TODO: Pending mapping
				mlProduct.setProductSiteOptionOverrides(null);
				mlProduct.setProductPersonalizationLinks(null);
				mlProduct.setProductOptionTypeLinks(null);
				mlProduct.setProductInfoPages(null);

				mlProduct.setOptionDataPages(null);
				mlProduct.setImageTargetPages(null);
				mlProduct.setImagePages(null);
				mlProduct.setFamilyProductLinks(null);
				mlProduct.setDescriptionPages(null);

				mlProduct.setDateDeactivate(null);
				mlProduct.setDateActivate(null);
				mlProduct.setCrossSellProductLinks(null);

				// TODO : Determine the mapping from NG
				mlProduct.setZoomCount(0);
				mlProduct.setTaxable(false);
				mlProduct.setSuppressSwatches(false);
				mlProduct.setRating(0.0);
				mlProduct.setPk(null);
				mlProduct.setNumberOfReviews(0);
				mlProduct.setDisplayTypeId(0);
				mlProduct.setDiscountable(false);
				mlProduct.setDeleted(false);
				mlProduct.setChargeShipping(false);

				/*
				 * TODO ngProduct.getMasterCatalogId();
				 * ngProduct.getProductTypeId();
				 * ngProduct.getContent().getProductName();
				 * ngProduct.getProductUsage();
				 * ngProduct.getApplicableDiscounts()
				 */

				// TODO populate the products here

			}
			startIndex = startIndex + PAGE_SIZE;
		} while (ngProductsList.size() > 0);

	}

	@Override
	public String getDefaultOrderByString() {
		return "productCode";
	}

	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {

		CommandResult result = marketLive.getResults().getResult().get(0);
		result.setOutput(new Output());

		Products productsExport = new Products();
		result.getOutput().setProducts(productsExport);

		List<Product> productsList = productsExport.getProduct();

		ProductResource productResource = new ProductResource(apiContext);
		com.mozu.api.contracts.productadmin.Product ngProduct = productResource.getProduct(code.getCode());

		Product mlProduct = new Product();

		if (ngProduct != null) {
			productsList.add(mlProduct);
			mlProduct.setCode(ngProduct.getProductCode());
			List<ProductInCatalogInfo> cataLogList = ngProduct.getProductInCatalogs();
			if (cataLogList != null) {
				for (ProductInCatalogInfo productInCatalogInfo : cataLogList) {
					mlProduct.setActive(productInCatalogInfo.getIsActive());
				}
			}

			// TODO populate the product fields here
		}
	}

	@Override
	public void decryptFields(List<CommandResult> results, ConfigModel config) {
		// TODO Auto-generated method stub

	}

	/*
	 * producttype and attribute fetching for product insert or update.
	 * 
	 */
	@Override
	public void setInitialData(ConfigModel config) throws Exception{
		
		skuCommandService.apiAuthentication(config);
		inventoryCommandService.apiAuthentication(config);
		// PRODUCT TYPE
		ProductTypeResource productTypeResource = new ProductTypeResource(apiContext);

		List<ProductType> productTypeList = productTypeResource.getProductTypes(0, 1000, null, null, null).getItems();
		for (ProductType productType : productTypeList) {
			productTypeMap.put(sortStringList(productType.getName()), productType);
		}

		// ATTRIBUTE

		AttributeResource attributeResource = new AttributeResource(apiContext);
		List<Attribute> attributeList = attributeResource.getAttributes(0, 1000, null, null, null).getItems();
		for (Attribute attribute : attributeList) {
			attributeMap.put(attribute.getAttributeCode().toLowerCase(), attribute);
		}

	}

	static ArrayList<String> sortStringList(String str) {
		ArrayList<String> keyList = new ArrayList<>();
		StringTokenizer tokens = new StringTokenizer(str, " - ");
		
		while(tokens.hasMoreTokens()) {
			keyList.add(tokens.nextToken());
		}
		Collections.sort(keyList);
		return keyList;
	}
	/*
	 * method to set ML attributePages content to NG ProductProperty
	 */
	private void readAttribute(AttributePages attributePages, List<ProductProperty> ngProperties,
			boolean isUpdate) {
		if (attributePages != null) {
			List<AttributePage> attributeList = attributePages.getAttributePage();
			List<ProductProperty> ngTempProperties = new ArrayList<>();

			for (AttributePage attributePage : attributeList) {
				boolean found = false;
				Attribute attribute = attributeMap.get(attributePage.getAttributes().getName().toLowerCase());
				String inputType = "TextBox";
				String dataType = "String";
				String nameSpace = "tenant";
				if (attribute != null) {
					dataType = attribute.getDataType();
					nameSpace = attribute.getNamespace();
					inputType = attribute.getInputType();

				}

				for (ProductProperty productProperty : ngProperties) {
					String fqn = productProperty.getAttributeFQN().replaceAll(nameSpace.toLowerCase() + "~", "");
					if (attributePage.getAttributes().getName().equalsIgnoreCase(fqn)) {
						found = true;

						List<ProductPropertyValue> valuesNg = productProperty.getValues();
						ProductPropertyValue value = null;
						if (valuesNg != null)
							value = valuesNg.get(0);
						if (attributePage.getAttributes().getValues() == null) {
							addValue(dataType, value, attributePage.getAttributes().getValue(), inputType);
						} else {
							AttributePageValues values = attributePage.getAttributes().getValues();
							// TODO: Yet to handle it
						}
						productProperty.setValues(valuesNg);
						break;
					}

				}
				if (!found) {
					ProductProperty property = new ProductProperty();
					ngTempProperties.add(property);
					property.setAttributeFQN(nameSpace.toLowerCase() + "~" + attributePage.getAttributes().getName());
					List<ProductPropertyValue> valuesNg = new ArrayList<>();
					if (attributePage.getAttributes().getValues() == null) {
						ProductPropertyValue value = new ProductPropertyValue();
						addValue(dataType, value, attributePage.getAttributes().getValue(), inputType);
						valuesNg.add(value);
					} else {
						AttributePageValues values = attributePage.getAttributes().getValues();
						if(values != null){
							List<String> list = values.getValue();
							for (String string : list) {
								ProductPropertyValue value = new ProductPropertyValue();
								addValue(dataType, value, string, inputType);
								valuesNg.add(value);
							}
						}
						// TODO: Yet to handle it
					}
					property.setValues(valuesNg);

				}
			}
			if (ngTempProperties.size() > 0)
				ngProperties.addAll(ngTempProperties);
		}
	}

	private void addValue(String dataType, ProductPropertyValue value,  String obj, String inputType) {
		if ("String".equals(dataType)) {
			String str = (String) obj;
			value.setValue(str);
		} else if ("Bool".equals(dataType)) {
			Boolean bool = Boolean.valueOf(obj);
			value.setValue(bool);
		} else {
			value.setValue(obj);
		}
		
		if("TextBox".equals(inputType)) {
			ProductPropertyValueLocalizedContent content = value.getContent();
			if(content == null) {
				content = new ProductPropertyValueLocalizedContent();
				value.setContent(content);
			}
			content.setLocaleCode("en_US");
			content.setStringValue(obj);
		}
	}
	@Override
	public void updateEntity(Product product, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		ProductResource productResource = new ProductResource(apiContext);
		com.mozu.api.contracts.productadmin.Product ngProduct = productResource.getProduct(product.getCode());
		if(ngProduct == null) {
			throw new Exception("Product Not found : " + product.getCode());
		} else {
			updateProduct(product, ngProduct, marketLive, catalogModel, true);
			//productResource.updateProduct(ngProduct, ngProduct.getProductCode());//need to decide which method has to be used
		}
	}
}
