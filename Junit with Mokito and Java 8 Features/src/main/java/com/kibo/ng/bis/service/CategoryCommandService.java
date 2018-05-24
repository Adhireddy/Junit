package com.kibo.ng.bis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.AttributePage;
import com.kibo.ng.bis.jaxb.AttributePages;
import com.kibo.ng.bis.jaxb.Category;
import com.kibo.ng.bis.jaxb.CategoryLink;
import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Output;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.productadmin.CategoryLocalizedContent;
import com.mozu.api.contracts.productadmin.CategoryLocalizedImage;
import com.mozu.api.resources.commerce.catalog.admin.CategoryResource;

@Service
public class CategoryCommandService extends MozuApiService<Category> {

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config,
			CatalogModel catalogModel) {
		List<Category> categories = importCommand.getCategories().getCategory();
		for (Category category : categories) {
			importEntityRecord(importCommand, category, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Category category) {
		record.setCategory(category);
	}

	@Override
	public void insertEntity(Category category, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		logger.debug("Insert Category " + category.getCode());

		com.mozu.api.contracts.productadmin.Category ngCategory = new com.mozu.api.contracts.productadmin.Category();

		updateCategory(ngCategory, category, catalogModel, false);

	}

	public void updateCategory(com.mozu.api.contracts.productadmin.Category ngCategory, Category category,
			CatalogModel catalogModel, boolean isUpdate) throws Exception {

		CategoryResource categoryResource = new CategoryResource(apiContext);

		// Parent Category
		setParentCategory(category, ngCategory, catalogModel);

		ngCategory.setCategoryCode(category.getCode().replaceAll("\\s", "_"));

		ngCategory.setCatalogId(catalogModel.getCatalogId());
		ngCategory.setCategoryType("Static");// category.getCategoryTypeAsString()

		if (category.getSearchEngineSupportPages() != null) {
			setContent(category.getSearchEngineSupportPages().getSearchEngineSupportPage(), category, ngCategory);
		}

		ngCategory.setIsActive(category.isActive());
		ngCategory.setIsDisplayed(true);

		if (isUpdate) {
			categoryResource.updateCategory(ngCategory, ngCategory.getId());
		} else {
			categoryResource.addCategory(ngCategory);
		}

		// Child category
		setChildCategory(category, ngCategory, catalogModel);

	}

	public void setParentCategory(Category category, com.mozu.api.contracts.productadmin.Category ngCategory,
			CatalogModel catalogModel) throws Exception {

		boolean found = false;

		if (category.getParentCategoryLinks() != null) {

			CategoryResource categoryResource = new CategoryResource(apiContext);

			List<CategoryLink> parentCategoryLink = category.getParentCategoryLinks().getParentCategoryLink();

			for (CategoryLink categoryLink : parentCategoryLink) {

				String parentCategoryCode = categoryLink.getParentCode().replaceAll("\\s", "_");
				Integer parentCategoryId = null;
				String parentCategoryName = null;
				boolean parentIsActive = false;

				List<com.mozu.api.contracts.productadmin.Category> items = categoryResource
						.getCategories(0, 100, "", "categoryCode eq " + parentCategoryCode, null).getItems();

				if (items.size() > 0) {
					found = true;

					parentCategoryId = items.get(0).getId();
					parentCategoryName = items.get(0).getContent().getName();
					parentIsActive = items.get(0).getIsActive();

				}

				if (!found) {
					// add parent Category
					com.mozu.api.contracts.productadmin.Category ngParentCategory = new com.mozu.api.contracts.productadmin.Category();

					ngParentCategory.setCategoryCode(parentCategoryCode);

					ngParentCategory.setCatalogId(catalogModel.getCatalogId());
					ngParentCategory.setCategoryType("Static");// category.getCategoryTypeAsString()

					ngParentCategory.setIsActive(true);
					ngParentCategory.setIsDisplayed(true);

					CategoryLocalizedContent content = new CategoryLocalizedContent();

					content.setName(parentCategoryCode);

					ngParentCategory.setContent(content);

					categoryResource.addCategory(ngParentCategory);

					items = categoryResource.getCategories(0, 100, "", "categoryCode eq " + parentCategoryCode, null)
							.getItems();

					if (items.size() > 0) {

						parentCategoryId = items.get(0).getId();
						parentCategoryName = items.get(0).getContent().getName();
						parentIsActive = items.get(0).getIsActive();

					}
				}

				// set parent category
				ngCategory.setParentCategoryCode(parentCategoryCode);
				ngCategory.setParentCategoryId(parentCategoryId);
				ngCategory.setParentCategoryName(parentCategoryName);
				ngCategory.setParentIsActive(parentIsActive);

			}
		}

	}

	public void setChildCategory(Category category, com.mozu.api.contracts.productadmin.Category ngCategory,
			CatalogModel catalogModel) throws Exception {
		if (category.getChildCategoryLinks() != null) {
			boolean found = false;

			CategoryResource categoryResource = new CategoryResource(apiContext);

			List<CategoryLink> childCategoryLink = category.getChildCategoryLinks().getChildCategoryLink();

			for (CategoryLink categoryLink : childCategoryLink) {

				String childCategoryCode = categoryLink.getChildCode().replaceAll("\\s", "_");

				String parentCategoryCode = ngCategory.getCategoryCode();
				Integer parentCategoryId = ngCategory.getId();
				String parentCategoryName = ngCategory.getContent().getName();
				boolean parentIsActive = ngCategory.getIsActive();

				List<com.mozu.api.contracts.productadmin.Category> items = categoryResource
						.getCategories(0, 100, "", "categoryCode eq " + childCategoryCode, null).getItems();

				if (items.size() > 0) {
					found = true;
				}

				com.mozu.api.contracts.productadmin.Category ngChildCategory = null;

				if (!found) {
					ngChildCategory = new com.mozu.api.contracts.productadmin.Category();

					ngChildCategory.setCategoryCode(childCategoryCode);

					ngChildCategory.setCatalogId(catalogModel.getCatalogId());
					ngChildCategory.setCategoryType("Static");
					// [Static,Dynamic]
					// category.getCategoryTypeAsString()

					ngChildCategory.setIsActive(true);
					ngChildCategory.setIsDisplayed(true);

					ngChildCategory.setParentCategoryCode(parentCategoryCode);
					ngChildCategory.setParentCategoryId(parentCategoryId);
					ngChildCategory.setParentCategoryName(parentCategoryName);
					ngChildCategory.setParentIsActive(parentIsActive);

					CategoryLocalizedContent content = new CategoryLocalizedContent();

					content.setName(childCategoryCode);

					ngChildCategory.setContent(content);

					categoryResource.addCategory(ngChildCategory);

				} else {
					ngChildCategory = items.get(0);

					ngChildCategory.setParentCategoryCode(parentCategoryCode);
					ngChildCategory.setParentCategoryId(parentCategoryId);
					ngChildCategory.setParentCategoryName(parentCategoryName);
					ngChildCategory.setParentIsActive(parentIsActive);

					categoryResource.updateCategory(ngChildCategory, ngChildCategory.getId());
				}

			}

		}
	}

	public void setContent(List<Category.SearchEngineSupportPages.SearchEngineSupportPage> searchEngineSupportPages,
			Category category, com.mozu.api.contracts.productadmin.Category ngCategory) {

		for (Category.SearchEngineSupportPages.SearchEngineSupportPage searchEngineSupportPage : searchEngineSupportPages) {

			CategoryLocalizedContent content = new CategoryLocalizedContent();

			content.setPageTitle(searchEngineSupportPage.getTitle());
			content.setName(searchEngineSupportPage.getUrlValue());
			content.setMetaTagTitle(searchEngineSupportPage.getTitle());
			content.setMetaTagKeywords(searchEngineSupportPage.getMetaKeywords());
			content.setMetaTagDescription(searchEngineSupportPage.getMetaDescription());
			content.setLocaleCode(searchEngineSupportPage.getLocale());
			content.setDescription(searchEngineSupportPage.getMetaDescription());

			if (category.getImagePages() != null) {
				List<CategoryLocalizedImage> categoryImages = new ArrayList<>();

				List<Category.ImagePages.ImagePage> imagePages = category.getImagePages().getImagePage();

				for (Category.ImagePages.ImagePage imagePage : imagePages) {

					CategoryLocalizedImage categoryLocalizedImage = new CategoryLocalizedImage();

					imagePage.getNavigateOn();
					imagePage.getNavigateOff();
					imagePage.getNavAltText();
					imagePage.getLocale();
					imagePage.getLeftNavOn();
					imagePage.getLeftNavOff();
					imagePage.getGatewayHeader();
					imagePage.getDirectoryHeader();

					categoryLocalizedImage.setAltText(imagePage.getNavAltText());
					content.setCategoryImages(categoryImages);
				}

			}

			ngCategory.setContent(content);
		}
	}

	@Override
	public void updateEntity(Category category, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		CategoryResource categoryResource = new CategoryResource(apiContext);

		String categoryCode = category.getCode().replaceAll("\\s", "_");

		List<com.mozu.api.contracts.productadmin.Category> ngCategory = categoryResource
				.getCategories(0, 100, "", "categoryCode eq " + categoryCode, null).getItems();

		if (ngCategory.size() > 0) {
			updateCategory(ngCategory.get(0), category, catalogModel, true);
		} else {
			throw new Exception("Category Not found for categoryCode : " + categoryCode);
		}

	}

	@Override
	public void updateInsertEntity(Category category, Marketlive marketLive, CatalogModel catalogModel)
			throws Exception {
		CategoryResource categoryResource = new CategoryResource(apiContext);

		List<com.mozu.api.contracts.productadmin.Category> ngCategory = categoryResource
				.getCategories(0, 100, "", "categoryCode eq " + category.getCode().replaceAll("\\s", "_"), null)
				.getItems();

		if (ngCategory.size() > 0) {
			updateCategory(ngCategory.get(0), category, catalogModel, true);
		} else {
			insertEntity(category, marketLive, catalogModel);
		}

	}

	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub
		Random random = new Random();
		return random.nextBoolean();
	}

	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {

		CommandResult result = marketLive.getResults().getResult().get(0);
		result.setOutput(new Output());

		Category categoryExport = new Category();
		result.getOutput().setCategory(categoryExport);

		CategoryResource categoryResource = new CategoryResource(apiContext);
		List<com.mozu.api.contracts.productadmin.Category> ngcategoryList = null;
		int startIndex = 0;
		do {
			ngcategoryList = categoryResource.getCategories(startIndex, PAGE_SIZE, orderByStr, filterStr, null)
					.getItems();

			for (com.mozu.api.contracts.productadmin.Category ngCategory : ngcategoryList) {
				Category mlCategory = new Category();

				mlCategory.setPk(ngCategory.getId() + "");
				mlCategory.setCode(ngCategory.getCategoryCode());
				mlCategory.setCategoryTypeAsString(ngCategory.getCategoryType());

				mlCategory.setActive(ngCategory.getIsActive());

				// TODO : determine the mapping data for attributepage data
				AttributePages attributePages = new AttributePages();
				AttributePage attributePage = new AttributePage();

				attributePages.getAttributePage().add(attributePage);

				mlCategory.setAttributePages(attributePages);

				mlCategory.setBorderFreeMerchantId(null);
				// TODO : implement logic to map categorytype
				// mlCategory.setCategoryType(ngCategory.getCategoryType());
				mlCategory.setCategoryTypeAsString(ngCategory.getCategoryType());

			}

			startIndex = startIndex + PAGE_SIZE;
		} while (ngcategoryList.size() > 0);

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
