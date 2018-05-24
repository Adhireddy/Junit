package com.kibo.ng.bis.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.kibo.ng.bis.model.SftpCredentialsModel;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;

@Service
public class SftpService {

	@Resource(name="sftpMap")
	HashMap<String, SftpCredentialsModel> sftpMap;

	protected final Logger logger = LoggerFactory.getLogger(getClass());


    //Method to gzip and send file over SFTP after service has been initialized
    public void sendFileOverSftp(List<File> fileList, String type) throws SftpException, IOException {

    	SftpCredentialsModel credentialsModel = getSftpModel(type);

        DefaultSftpSessionFactory sessionFactory = sftpSessionFactory(credentialsModel);
        SftpSession sftpSession = sessionFactory.getSession();
        ChannelSftp channelSftp = sftpSession.getClientInstance();
        channelSftp.cd(credentialsModel.getSftpExportPath());
        try {
        	for(File exportFile:fileList)
        	{
            File gzippedFile = gzipFile(exportFile);
            InputStream inputStream = new FileInputStream(exportFile);
           
            sftpSession.write(inputStream, exportFile.getName());
            IOUtils.closeQuietly(inputStream);
            exportFile.delete();
            gzippedFile.delete();
        	}
        }
        catch (IOException e) {
            logger.error("SFTP upload failure {} ", e);
            logger.error(e.getMessage());
            throw e;
        }
        finally {
            if (sftpSession != null && sftpSession.isOpen()) {
                sftpSession.close();
            }
        }
    }
    
    private SftpCredentialsModel getSftpModel(String sftpProcessor) {
    	SftpCredentialsModel credentialsModel = sftpMap.get(sftpProcessor);

    	if(credentialsModel == null) {
    		Set<String> set = sftpMap.keySet();
    		for (String string : set) {
    			SftpCredentialsModel model = sftpMap.get(string);
    			if(model.isDefault())
    				return model;
			}
    	}
    	
    	return credentialsModel;
    }
    //Method to gzip and send file over SFTP after service has been initialized
    public void getFileOverSftp(String folderName, String sftpProcessor) throws SftpException, IOException, JSchException {
    	SftpCredentialsModel credentialsModel = getSftpModel(sftpProcessor);

        DefaultSftpSessionFactory sessionFactory = sftpSessionFactory(credentialsModel);
        SftpSession sftpSession = sessionFactory.getSession();
        ChannelSftp channelSftp = sftpSession.getClientInstance();
        channelSftp.cd(credentialsModel.getSftpExportPath());
        Vector<LsEntry> vector = channelSftp.ls(credentialsModel.getSftpImportPath());
        try {
            Enumeration<LsEntry> en = vector.elements();
            while(en.hasMoreElements()) {
            	LsEntry obj = en.nextElement();
            	if(!obj.getAttrs().isDir()) {
            		channelSftp.get(credentialsModel.getSftpImportPath()+"/"+obj.getFilename(), new File(folderName+"/" + obj.getFilename()).getAbsolutePath());
            	}
            }
        }
        catch (Exception e) {
        	logger.error("SFTP download failure {} ", e);
        	logger.error(e.getMessage());
            throw e;
        }
        finally {
            if (sftpSession != null && sftpSession.isOpen()) {
                sftpSession.close();
            }
        }
    }

    //Gzips file and replaces original copy with zipped version in same directory
    public File gzipFile(File file) throws IOException {
        byte[] bufferArray = new byte[1024];
        String zippedFile = file.getAbsolutePath()+ ".gz";
        FileInputStream fileInputStream = new FileInputStream(file);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(zippedFile));

        int length;
        while((length = fileInputStream.read(bufferArray))>0) {
            gzipOutputStream.write(bufferArray,0,length);
        }

        fileInputStream.close();
        File createdFile = new File(zippedFile);
        gzipOutputStream.finish();
        gzipOutputStream.close();

        return createdFile;
    }



    public DefaultSftpSessionFactory sftpSessionFactory(SftpCredentialsModel credentials) {
        DefaultSftpSessionFactory sftpSessionFactory = new DefaultSftpSessionFactory();
        sftpSessionFactory.setHost(credentials.getIpAddress());
        sftpSessionFactory.setPort(credentials.getPort());
        sftpSessionFactory.setAllowUnknownKeys(true);
        sftpSessionFactory.setUser(credentials.getUserName());
        sftpSessionFactory.setPassword(credentials.getPassword());
        sftpSessionFactory.setServerAliveCountMax(5);
        sftpSessionFactory.setTimeout(2000);
        

        return sftpSessionFactory;
    }
}
