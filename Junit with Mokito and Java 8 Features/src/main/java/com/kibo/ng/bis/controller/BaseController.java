package com.kibo.ng.bis.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kibo.ng.bis.scheduler.IntegrationScheduler;

import com.kibo.ng.bis.service.SftpService;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseController.
 */
@Controller
@PropertySources({ @PropertySource("classpath:mail.properties"), 
		@PropertySource("classpath:config.properties") })

public class BaseController {

	/** The integrationbatchscheduler. */
	@Autowired
	IntegrationScheduler integrationbatchscheduler;

	/** The Constant VIEW_INDEX. */
	private static final String VIEW_INDEX = "index";

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The sftp service. */
	@Autowired
	SftpService sftpService;

	/**
	 * Welcome.
	 *
	 * @param model
	 *            the model
	 * @return the string
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome(ModelMap model) {
		String dateParam = new Date().toString();

		logger.info("Application has Strated | Date : {}",  dateParam);

		return VIEW_INDEX;
	}

	/**
	 * Batch integration scheduler.
	 *
	 * @return the string
	 */

	@RequestMapping(value = "/bis", method = RequestMethod.GET)
	public String batchIntegrationScheduler() {

		logger.info("Running batch itegration scheduler");

		integrationbatchscheduler.process();
		return VIEW_INDEX;
	}

}