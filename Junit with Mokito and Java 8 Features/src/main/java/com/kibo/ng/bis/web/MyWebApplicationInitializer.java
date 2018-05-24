package com.kibo.ng.bis.web;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;


public class MyWebApplicationInitializer implements WebApplicationInitializer {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Override
    public void onStartup(ServletContext container) {
        logger.info("Create Spring Job here ..");
        
    }
}