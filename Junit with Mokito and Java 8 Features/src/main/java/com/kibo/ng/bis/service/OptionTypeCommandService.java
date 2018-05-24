package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.OptionType;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.productadmin.Attribute;
import com.mozu.api.contracts.productadmin.AttributeLocalizedContent;
import com.mozu.api.contracts.productadmin.AttributeSearchSettings;
import com.mozu.api.resources.commerce.catalog.admin.attributedefinition.AttributeResource;

@Service
public class OptionTypeCommandService extends MozuApiService<OptionType> {

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config,
			CatalogModel catalogModel) {
		List<OptionType> optionTypes = importCommand.getOptionTypes().getOptionType();
		for (OptionType optionType : optionTypes) {
			importEntityRecord(importCommand, optionType, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, OptionType optionType) {
		record.setOptionType(optionType);
	}

	@Override
	public void insertEntity(OptionType optionType, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		Attribute ngAttribute = new Attribute();

		updateProductAttribute(optionType, ngAttribute, marketLive, catalogModel, false);

	}

	public void updateProductAttribute(OptionType optionType, Attribute ngAttribute, Marketlive marketLive,
			CatalogModel catalogModel, boolean isUpdate) throws Exception {

		AttributeResource attributeResource = new AttributeResource(apiContext);

		optionType.getDisplayPages();// TODO: need to find mapping
		optionType.getOrdinal();// TODO: need to find mapping
		optionType.isActive();// TODO: need to find mapping
		optionType.isSwatchEnabled();// TODO: need to find mapping

		String nameSpace = "tenant";

		String attributeFQN = optionType.getCode().toLowerCase();

		ngAttribute.setMasterCatalogId(catalogModel.getMasterCatalogId());

		ngAttribute.setAdminName(optionType.getCode());
		ngAttribute.setAttributeCode(attributeFQN);
		ngAttribute.setAttributeFQN(nameSpace + "~" + attributeFQN);

		if (optionType.getDescriptionPages() != null) {
			AttributeLocalizedContent content = new AttributeLocalizedContent();
			setAttributeValues(content, optionType.getDescriptionPages());
			ngAttribute.setContent(content);
		}

		// Options are always String. Attributes that are checkbox become
		// "Bool". All others are String.
		// Bool, DateTime, Number, or String
		String dataType = "String";
		ngAttribute.setDataType(dataType);

		// Options are always List. Checkbox Attributes become "Yes/No". Text,
		// text-long & file attributes become TextBox; All others become List.
		// Yes/No, Date, DateTime, List, TextBox, or TextArea
		String inputType = "List";
		ngAttribute.setInputType(inputType);

		ngAttribute.setIsProperty(true);// Always "Yes"
		ngAttribute.setNamespace("Tenant");

		AttributeSearchSettings searchSettings = new AttributeSearchSettings();

		searchSettings.setAllowFilteringAndSortingInStorefront(true);
		searchSettings.setSearchableInAdmin(true);
		searchSettings.setSearchableInStorefront(true);

		ngAttribute.setSearchSettings(searchSettings);

		// predefined vocabulary by the admin during attribute set up or
		// user-defined
		String valueType = "PreDefined";
		ngAttribute.setValueType(valueType);

		if (isUpdate) {
			attributeResource.updateAttribute(ngAttribute, ngAttribute.getAttributeFQN());
		} else {
			attributeResource.addAttribute(ngAttribute);
		}
	}

	public void setAttributeValues(AttributeLocalizedContent content,
			com.kibo.ng.bis.jaxb.OptionType.DescriptionPages descriptionPages) {
		List<com.kibo.ng.bis.jaxb.OptionType.DescriptionPages.DescriptionPage> descriptionPageList = descriptionPages
				.getDescriptionPage();

		for (com.kibo.ng.bis.jaxb.OptionType.DescriptionPages.DescriptionPage descriptionPage : descriptionPageList) {

			content.setLocaleCode(descriptionPage.getLocale());
			content.setName(descriptionPage.getName());

		}
	}

	@Override
	public void updateEntity(OptionType optionType, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		AttributeResource attributeResource = new AttributeResource(apiContext);

		String attributeFQN = optionType.getCode().toLowerCase();

		Attribute ngAttribute = attributeResource.getAttribute(attributeFQN);

		if (ngAttribute != null) {
			updateProductAttribute(optionType, ngAttribute, marketLive, catalogModel, true);
		} else {
			throw new Exception("Product Attribute not found for attributeFQN : " + attributeFQN);
		}
	}

	@Override
	public void updateInsertEntity(OptionType optionType, Marketlive marketLive, CatalogModel catalogModel)
			throws Exception {

		AttributeResource attributeResource = new AttributeResource(apiContext);

		String attributeFQN = optionType.getCode().toLowerCase();

		Attribute ngAttribute = attributeResource.getAttribute(attributeFQN);

		if (ngAttribute != null) {
			updateProductAttribute(optionType, ngAttribute, marketLive, catalogModel, true);
		} else {
			insertEntity(optionType, marketLive, catalogModel);
		}
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
