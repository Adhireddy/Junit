package com.kibo.ng.bis.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Option;
import com.kibo.ng.bis.jaxb.Option.DescriptionPages;
import com.kibo.ng.bis.jaxb.Option.DescriptionPages.DescriptionPage;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.contracts.productadmin.Attribute;
import com.mozu.api.contracts.productadmin.AttributeVocabularyValue;
import com.mozu.api.contracts.productadmin.AttributeVocabularyValueLocalizedContent;
import com.mozu.api.resources.commerce.catalog.admin.attributedefinition.AttributeResource;

@Service
public class OptionsCommandService extends MozuApiService<Option> {

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config,
			CatalogModel catalogModel) {
		List<Option> options = importCommand.getOptions().getOption();
		for (Option option : options) {
			importEntityRecord(importCommand, option, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Option option) {
		record.setOption(option);
	}

	@Override
	public void insertEntity(Option option, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		updateEntity(option, marketLive, catalogModel);

	}

	public void updateProductAttribute(Option option, Attribute ngAttribute, Marketlive marketLive,
			CatalogModel catalogModel) throws Exception {

		AttributeResource attributeResource = new AttributeResource(apiContext);

		option.getOrdinal();// TODO: need to find mapping
		option.getImage();// TODO: need to find mapping

		AttributeVocabularyValue attributeVocabularyValue = new AttributeVocabularyValue();

		if (option.getDescriptionPages() != null) {

			AttributeVocabularyValueLocalizedContent content = new AttributeVocabularyValueLocalizedContent();
			setAttributeValues(content, option.getDescriptionPages());
			attributeVocabularyValue.setContent(content);
		}

		attributeVocabularyValue.setValue(option.getCode());

		List<AttributeVocabularyValue> vocabularyValues = ngAttribute.getVocabularyValues();
		vocabularyValues.add(attributeVocabularyValue);
		ngAttribute.setVocabularyValues(vocabularyValues);

		attributeResource.updateAttribute(ngAttribute, ngAttribute.getAttributeFQN());

	}

	public void setAttributeValues(AttributeVocabularyValueLocalizedContent content, DescriptionPages option) {
		List<DescriptionPage> descriptionPageList = option.getDescriptionPage();

		for (DescriptionPage descriptionPage : descriptionPageList) {

			content.setLocaleCode(descriptionPage.getLocale());
			content.setStringValue(descriptionPage.getName());
			// This will make Label [en_US]Green[en_US], else label will be like Green

		}
	}

	@Override
	public void updateEntity(Option option, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		AttributeResource attributeResource = new AttributeResource(apiContext);

		attributeResource.getAttributes(0, 100, null, "attributeCode eq Brand", null);

		String attributeFQN = option.getOptionTypeCode().toLowerCase();

		Attribute ngAttribute = attributeResource.getAttribute(attributeFQN);

		if (ngAttribute != null) {
			updateProductAttribute(option, ngAttribute, marketLive, catalogModel);
		} else {
			throw new Exception("Product Attribute not found for attributeFQN : " + option.getOptionTypeCode());
		}
	}

	@Override
	public void updateInsertEntity(Option option, Marketlive marketLive, CatalogModel catalogModel) throws Exception {

		updateEntity(option, marketLive, catalogModel);
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
