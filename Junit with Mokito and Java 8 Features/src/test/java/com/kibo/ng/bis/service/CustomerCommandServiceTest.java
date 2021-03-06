package com.kibo.ng.bis.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.CommandResult.Summary;
import com.kibo.ng.bis.jaxb.Customer;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Marketlive.Import;
import com.kibo.ng.bis.jaxb.Marketlive.Results;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;
import com.kibo.ng.bis.scheduler.IntegrationScheduler;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-beans.xml" })
public class CustomerCommandServiceTest {

	@Autowired
	CustomerCommandService customerCommandService;
	
	@Autowired
	IntegrationScheduler integrationScheduler;
	
	@Resource(name="stgv162")
	ConfigModel config;

	@Resource(name="kibocommercesstore")
	CatalogModel catalogModel;
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCustomerimportCommand() {

		File orderStatus = new File("src/test/java/09_customer.xml");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Marketlive.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Marketlive marketLive = (Marketlive) jaxbUnmarshaller.unmarshal(orderStatus);
			integrationScheduler.prePopulateResults(marketLive);

			Import imports = marketLive.getImport();
			if (imports != null) {
				List<ImportCommand> commands = imports.getCommand();
				for (ImportCommand importCommand : commands) {
					Boolean actuvalResulut = customerCommandService.importCommand(importCommand, marketLive, config,
							catalogModel);
					Boolean expectedResult = true;
					Assert.assertEquals(expectedResult, actuvalResulut);
				}
			}
		} catch (JAXBException e) {

			e.printStackTrace();
		}

	}

}
