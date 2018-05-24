package com.kibo.ng.bis.scheduler;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aspose.cells.FileFormatInfo;
import com.aspose.cells.FileFormatUtil;
import com.kibo.ng.bis.jaxb.CommandProcessor;
import com.kibo.ng.bis.jaxb.CommandProcessors;
import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.CommandResult.Summary;
import com.kibo.ng.bis.jaxb.ConfigInfo;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.T10FileCopy;
import com.kibo.ng.bis.jaxb.T11FileCopyEncryption;
import com.kibo.ng.bis.jaxb.Marketlive.Export;
import com.kibo.ng.bis.jaxb.Marketlive.Import;
import com.kibo.ng.bis.jaxb.Marketlive.Results;
import com.kibo.ng.bis.jaxb.T8File;
import com.kibo.ng.bis.jaxb.T9OutputFileCopies;
import com.kibo.ng.bis.jaxb.TBatchIntegrationRequest;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandRequestType;
import com.kibo.ng.bis.model.ConfigModel;
import com.kibo.ng.bis.model.SitesModel;
import com.kibo.ng.bis.service.EmailService;
import com.kibo.ng.bis.service.KiboPGPUtil;
import com.kibo.ng.bis.service.SftpService;

/**
 * Batch Integration Scheduler.
 */
@Service("integrationscheduler")
public class IntegrationScheduler {

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The control file regex. */
	@Value("${control.file.regex}")
	private String CONTROL_FILE_REGEX;

	/** The control file regex1. */
	@Value("${control.file.regex1}")
	private String CONTROL_FILE_REGEX1;

	/** The control folder path. */
	@Value("${control.folder.path}")
	private String CONTROL_FOLDER_PATH;

	/** The command folder path. */
	@Value("${command.folder.path}")
	private String COMMAND_FOLDER_PATH;

	/** The archive folder path. */
	@Value("${archive.folder.path}")
	private String ARCHIVE_FOLDER_PATH;

	/** The public ring path. */
	@Value("${gnupg.pubRing.path}")
	private String PUBLIC_RING_PATH;

	/** The transformation folder path. */
	@Value("${output.transformations.folder.path}")
	private String TRANSFORMATION_FOLDER_PATH;

	/** The results folder path. */
	@Value("${results.folder.path}")
	private String RESULTS_FOLDER_PATH;

	/** The working folder path. */
	@Value("${working.folder.path}")
	private String WORKING_FOLDER_PATH;

	/** The export folder path. */
	@Value("${export.folder.path}")
	private String EXPORT_FOLDER_PATH;

	/** The output archive folder path. */
	@Value("${output.archive.folder.path}")
	private String OUTPUT_ARCHIVE_FOLDER_PATH;


	/** The config map. */
	@Resource(name = "configMap")
	HashMap<String, ConfigModel> configMap;

	/** The catagory map. */
	@Resource(name = "catalogMap")
	HashMap<String, CatalogModel> catalogMap;

	/** The sites list. */
	@Resource(name = "sitesList")
	List<SitesModel> sitesList;

	/** The email service. */
	@Autowired
	private EmailService emailService;

	/** The date format. */
	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssMSS");

	/** The sftp service. */ 
	@Autowired
	SftpService sftpService;

	/**
	 * This method is entry point of Cron Job.
	 */
	public void process() {
		
		for (SitesModel sitesModel : sitesList) {
			logger.info("Batch Integration Process started");
			String emailMsg = null;
			boolean fatal = false;
			try {
				List<String> controlFileList = copyFilesFromSiteToServiceFolder(sitesModel);
				
				if (!controlFileList.isEmpty()) {
					for (String controlFile : controlFileList) {
						processControlFile(controlFile, sitesModel);
					}
				} else {
					fatal = true;
					emailMsg = "No Control Files found";
				}

			} catch (Exception e) {
				logger.error("Error while processing control file",e);

				fatal = true;
				emailMsg = e.getMessage();
			}
			if (fatal)
				emailService.sendFatalEmail(emailMsg);
		}
	}

	/**
	 * This method processes the control file.
	 *
	 * @param controlFile
	 *            the control file list
	 * @param sitesModel
	 *            the sites model
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public void processControlFile(String controlFile, SitesModel sitesModel) throws JAXBException {

		File controlfile = new File(CONTROL_FOLDER_PATH + "/" + controlFile);
		TBatchIntegrationRequest control = null;
		
		if (controlfile.exists()) {
			// parsing the control file using jaxb
			control = parseControlFile(controlfile);

			List<T8File> commandFiles = getCommandFiles(control);

			logger.debug("Total number of command Files: {}" + commandFiles.size());

			boolean previousCommandWasSuccess = true;

			for (T8File t8File : commandFiles) {
				
				// processing of individual command file.
				previousCommandWasSuccess = processCommandFile(t8File, control, sitesModel, previousCommandWasSuccess);
			}

			// removing the control file after copying it to archive directory.
			controlfile.delete();

		}
	}

	/**
	 * This method processes command file.
	 *
	 * @param t8File
	 *            the t 8 file
	 * @param control
	 *            the control
	 * @param sitesModel
	 *            the sites model
	 * @param previousCommandWasSuccess
	 *            the previous command was success
	 * @return true, if successful
	 */
	public boolean processCommandFile(T8File t8File, TBatchIntegrationRequest control, SitesModel sitesModel,
			boolean previousCommandWasSuccess) {

		String requestId = control.getRequestId() + "_" + dateFormat.format(new Date());

		String environment = control.getMerchant().getSite().getEnvironment();

		String siteId = control.getMerchant().getSite().getCode();

		File commandFile = new File(COMMAND_FOLDER_PATH + "/" + t8File.getName());
		
		logger.info("Batch Integration RequestID  : {}" , requestId);

		logger.info("Processing command File      : {}" , commandFile.getName());
		
		logger.info("Merchant - Site Code         : {}" , siteId);
		
		logger.info("Merchant - Site - Environment: {}" , environment);

		copyToArchive(commandFile, control);

		if (!previousCommandWasSuccess) {
			return false;
		}

		createBakFile(requestId, t8File.getName());

		createRequestConfirmFile(requestId, control);

		Marketlive marketLive = null;

		try {
			marketLive = parseCommandFile(t8File, commandFile, control, sitesModel);

		} catch (Exception e) {
			String msg = "JAXB parsing exception while trying to parse command file - " + commandFile.getName() + " -- "
					+ e.getMessage();

			logger.error("JAXB parsing exception while trying to parse command file {} ", e);

			emailService.sendNotification(msg, control);
			return false;
		}

		prePopulateResults(marketLive);

		previousCommandWasSuccess = processImports(marketLive, configMap.get(environment), catalogMap.get(siteId));

		if (!previousCommandWasSuccess) {
			return false;
		}

		previousCommandWasSuccess = processExports(t8File, marketLive, requestId, t8File.getName(),
				configMap.get(environment), catalogMap.get(siteId), commandFile, control);

		if (!previousCommandWasSuccess) {
			return false;
		}

		moveToWorkingFolder(requestId, t8File.getName(), commandFile);

		writeSuccessFailure(marketLive, requestId, t8File.getName(), control);

		return true;
	}

	/**
	 * Copies files from site folder to service folder
	 *
	 * @param sitesModel
	 *            the sites model
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */

	public List<String> copyFilesFromSiteToServiceFolder(SitesModel sitesModel) throws Exception {

		List<String> ctrlFileList = new ArrayList<String>();

		File siteControlFolder = new File(sitesModel.getSiteControlFolderPath());
		
		File siteCommandFolder = new File(sitesModel.getSiteCommandFolderPath());

		File servicecontrolFolder = new File(CONTROL_FOLDER_PATH);

		File servicecommandFolder = new File(COMMAND_FOLDER_PATH);

		File[] siteControlFiles = siteControlFolder.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX));
		
		
		
		if (siteControlFiles != null && siteControlFiles.length != 0) {

			for (File siteControlFile : siteControlFiles) {
				FileUtils.copyFileToDirectory(siteControlFile, servicecontrolFolder);

				siteControlFile.delete();
			}
		}
		
		File[] siteCtrlFiles = siteControlFolder.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX1));
		
		if (siteCtrlFiles != null && siteCtrlFiles.length != 0) {

			for (File siteCtrlFile : siteCtrlFiles) {
				FileUtils.copyFileToDirectory(siteCtrlFile, servicecontrolFolder);

				siteCtrlFile.delete();
			}
		}
		
		File[] siteCommandFiles = siteCommandFolder.listFiles();
		
		if (siteCommandFiles != null && siteCommandFiles.length != 0) {
			for (File siteCommandFile : siteCommandFiles) {
				FileUtils.copyFileToDirectory(siteCommandFile, servicecommandFolder);
				siteCommandFile.delete();
			}
		}
		
		File[] serviceControlFiles = servicecontrolFolder.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX));
		
		if (serviceControlFiles.length == 0) {
			serviceControlFiles = servicecontrolFolder.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX1));
		}

		if (serviceControlFiles.length == 0) {
			throw new Exception("control file is not present");
		}

		
		if (serviceControlFiles != null) {
		
			for (int i = 0;i < serviceControlFiles.length; i++) {
	
				ctrlFileList.add(serviceControlFiles[i].getName());
			}
		
		}

		logger.debug("list of control files {}", ctrlFileList);
		return ctrlFileList;
	}

	/**
	 * This method will copy files to archive folder.
	 *
	 * @param commandFile
	 *            command file path this method will create a copy of Command
	 *            File to Archive Folder
	 * @param control
	 *            the control
	 */
	private void copyToArchive(File commandFile, TBatchIntegrationRequest control) {
		logger.debug("Name of the commandFile {}", commandFile);
		try {
			FileUtils.copyFileToDirectory(commandFile, new File(ARCHIVE_FOLDER_PATH));

		} catch (Exception e) {

			logger.error("Error occured while trying to copy command file to archive directory after renaming it, {}",
					e);

			emailService.sendNotification(
					"Error occured while trying to copy command file to archive directory after renaming it " + ","
							+ e.getMessage(),
					control);
		}
	}

	/**
	 * This method creates the bak file.
	 *
	 * @param requestId
	 *            the request id
	 * @param commandFileName
	 *            the command file name
	 */
	private void createBakFile(String requestId, String commandFileName) {
		String fileBackUpName = requestId + "_" + commandFileName + "." + System.currentTimeMillis() + ".bak";

		File neWInventoryArchiveFile = new File(ARCHIVE_FOLDER_PATH + "/" + fileBackUpName);

		logger.info("command file backup name, {}", fileBackUpName);

		File inventoryArchivefile = new File(ARCHIVE_FOLDER_PATH + "/" + commandFileName);

		inventoryArchivefile.renameTo(neWInventoryArchiveFile);
	}

	/**
	 * This method creates the request confirm file.
	 *
	 * @param requestId
	 *            the request id
	 * @param control
	 *            the control
	 */
	private void createRequestConfirmFile(String requestId, TBatchIntegrationRequest control) {

		String msg = "Batch Integration request has been registered with ID " + requestId + " MerchantCode: "
				+ control.getMerchant().getCode() + " SiteCode: " + control.getMerchant().getSite().getCode()
				+ " environment: " + control.getMerchant().getSite().getEnvironment();

		File resultFilePath = new File(RESULTS_FOLDER_PATH + "/" + requestId + "_" + "request-confirmed.txt");

		// Creating confirmation file indicating the integration job for control
		// file
		fileWriter(msg, resultFilePath);
	}

	/**
	 * This method processes import files and calls MozuApiService for import
	 * task.
	 *
	 * @param marketLive
	 *            the market live
	 * @param config
	 *            the config
	 * @return true, if successful
	 */
	public boolean processImports(Marketlive marketLive, ConfigModel config, CatalogModel catalogModel) {

		Import imports = marketLive.getImport();

		if (imports != null) {
			List<ImportCommand> commands = imports.getCommand();

			for (ImportCommand importCommand : commands) {
				CommandProcessors processor = importCommand.getCommandProcessors();

				if (processor != null) {
					List<CommandProcessor> list = processor.getCommandProcessor();

					for (CommandProcessor commandProcessor : list) {

						if (commandProcessor.getCode().contains("Export")) { 
							try {
								File exportedFile = new File(EXPORT_FOLDER_PATH);

								File[] exportFiles = exportedFile.listFiles();

								List<File> fileList = new ArrayList<>();
								for (File file : exportFiles) {
									if (file.isFile()) {
										fileList.add(file);
									}
								}

								sftpService.sendFileOverSftp(fileList, commandProcessor.getBeanName());

								logger.info("Sending files over sftp");

							} catch (Exception e) {
								String msg = "Error uploading export  Files to sftp " + e.getMessage();

								logger.error("Error uploading export  Files to sftp {} ", e);

								emailService.sendFatalEmail(msg);

								return false;
							}
						} else {
							try {
								sftpService.getFileOverSftp(COMMAND_FOLDER_PATH, commandProcessor.getBeanName());

							} catch (Exception e) {
								String msg = "Error Downloading Command Files from sftp" + e.getMessage();

								logger.error("Error Downloading Command Files from sftp {} ", e);

								emailService.sendFatalEmail(msg);

								return false;
							}
						}
					}
				}
				else {
					try {

						CommandRequestType commandRequestType = CommandRequestType.getType(importCommand);

						commandRequestType.getMozuApiService().importCommand(importCommand, marketLive, config,
								catalogModel);

					} catch (Exception e) {
						String msg = "Error Importing to NG API" + e.getMessage();

						logger.error("Error Importing to NG API {} ", e);

						emailService.sendFatalEmail(msg);

						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * This method processes export files and calls MozuApiService for export
	 * task.
	 *
	 * @param t8File
	 *            the t 8 file
	 * @param marketLive
	 *            the market live
	 * @param requestId
	 *            the request id
	 * @param commandFileName
	 *            the command file name
	 * @param config
	 *            the config
	 * @param commandFile
	 *            the command file
	 * @param control
	 *            the control
	 * @return true, if successful
	 */
	public boolean processExports(T8File t8File, Marketlive marketLive, String requestId, String commandFileName,
			ConfigModel config, CatalogModel catalogModel, File commandFile, TBatchIntegrationRequest control) {

		Export exports = marketLive.getExport();
		if (exports != null) {
			List<ExportCommand> commands = exports.getCommand();

			for (ExportCommand exportCommand : commands) {

				try {

					ByteArrayOutputStream finalOut = null;

					String newcommandFileName = commandFileName.replace(".xml", "");

					File exportedFile = new File(EXPORT_FOLDER_PATH + "/" + requestId + "_" + newcommandFileName + "."
							+ System.currentTimeMillis() + ".xml");

					if (t8File.isEncryptionRequested()) {
						exportedFile = new File(
								OUTPUT_ARCHIVE_FOLDER_PATH + "/" + requestId + "_" + commandFile.getName() + ".orig");
					}

					CommandRequestType commandRequestType = CommandRequestType.getType(exportCommand);

					if (exportCommand.getFindByCriteriaParameters() != null) {
						commandRequestType.getMozuApiService().exportEntityByCriteria(exportCommand, marketLive,
								config);

					} else if (exportCommand.getFindByCodeParameters() != null) {
						commandRequestType.getMozuApiService().exportEntityByCode(exportCommand, marketLive, config);

					}
					
					encryptOutputFileCopy(marketLive, control, config, exportedFile, t8File, commandRequestType,
							requestId, commandFileName, newcommandFileName, finalOut);

				} catch (Exception e) {
					String msg = "Error Exporting from NG API. " + e.getMessage();

					logger.error("Error Exporting from NG API {} ", e);

					emailService.sendFatalEmail(msg);

					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method encrypt output file copy.
	 *
	 * @param marketLive
	 *            the market live
	 * @param control
	 *            the control
	 * @param config
	 *            the config
	 * @param exportedFile
	 *            the exported file
	 * @param t8File
	 *            the t 8 file
	 * @param commandRequestType
	 *            the command request type
	 * @param requestId
	 *            the request id
	 * @param commandFileName
	 *            the command file name
	 * @param newcommandFileName
	 *            the newcommand file name
	 * @param finalOut
	 *            the final out
	 * @throws Exception
	 *             the exception
	 */
	private void encryptOutputFileCopy(Marketlive marketLive, TBatchIntegrationRequest control, ConfigModel config,
			File exportedFile, T8File t8File, CommandRequestType commandRequestType, String requestId,
			String commandFileName, String newcommandFileName, ByteArrayOutputStream finalOut) throws Exception {

		marketLive.setExport(null);

		marketLive.setImport(null);
		JAXBContext jaxbContext = JAXBContext.newInstance(Marketlive.class);

		Marshaller marshaller = jaxbContext.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		marshaller.marshal(marketLive, exportedFile);

		String xsltName = t8File.getOutputXSLTFileName();

		if (t8File.isEncryptionRequested()) {

			if (control.getMerchant().getSite().getFileEncryption() != null) {

				String pubRingFile = control.getMerchant().getSite().getFileEncryption().getPublicKeyFile();

				commandRequestType.getMozuApiService().decryptFields(marketLive.getResults().getResult(), config);

				ByteArrayOutputStream stm = new ByteArrayOutputStream();

				jaxbContext = JAXBContext.newInstance(Marketlive.class);
				marshaller = jaxbContext.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				marshaller.marshal(marketLive, stm);

				File file = null;
				if (file == null) {
					file = new File(EXPORT_FOLDER_PATH + "/" + requestId + "_" + commandFileName + "."
							+ System.currentTimeMillis() + ".xml" + ".pgp");
				}

				FileOutputStream out = new FileOutputStream(file);
				FileInputStream keyIn = new FileInputStream(PUBLIC_RING_PATH + "/" + pubRingFile);

				KiboPGPUtil.encryptBytes(out, "emptyfile", stm.toByteArray(),
						com.kibo.ng.bis.service.KiboPGPUtil.readPublicKey(keyIn),
						control.getMerchant().getSite().getFileEncryption().isAsciiFormatEncoding(), false);

				keyIn.close();
				out.close();
			}
		}

		T9OutputFileCopies fileCopies = t8File.getOutputFileCopies();

		if (fileCopies != null) {
			List<T10FileCopy> list = fileCopies.getFileCopy();

			if (list != null && !list.isEmpty()) {

				for (T10FileCopy t10FileCopy : list) {
					finalOut = new ByteArrayOutputStream();
					JAXBElement<String> xsltFile = t10FileCopy.getXsltFileName();

					T11FileCopyEncryption encryptionDetail = t10FileCopy.getFileCopyEncryption();
					if (encryptionDetail != null) {

						String pubRingFile = encryptionDetail.getPublicKeyFile();

						String pgp = encryptionDetail.getOutputFileExtension();

						if (!t8File.isEncryptionRequested())

							commandRequestType.getMozuApiService().decryptFields(marketLive.getResults().getResult(),
									config);

						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

						jaxbContext = JAXBContext.newInstance(Marketlive.class);
						marshaller = jaxbContext.createMarshaller();

						marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
						marshaller.marshal(marketLive, byteArrayOutputStream);

						File file = null;

						if (t10FileCopy.getDirectoryName() != null) {
							file = new File(EXPORT_FOLDER_PATH + "/" + t10FileCopy.getDirectoryName().getValue());

							if (!file.exists()) {
								file.mkdirs();
							}
						}

						if (t10FileCopy.getFileName() != null) {
							file = new File(file.getAbsolutePath() + "/" + t10FileCopy.getFileName() + "." + pgp);
						}

						if (file == null) {
							file = new File(EXPORT_FOLDER_PATH + "/" + requestId + "_" + newcommandFileName + "."
									+ System.currentTimeMillis() + ".xml");
						}

						FileOutputStream out = new FileOutputStream(file);
						FileInputStream keyIn = new FileInputStream(PUBLIC_RING_PATH + "/" + pubRingFile);

						if (xsltFile != null) {

							Source xsl = new StreamSource(new File(TRANSFORMATION_FOLDER_PATH + "/" + xsltName));

							ByteArrayInputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

							Source inputFile = new StreamSource(in);
							Result xmlOutput = new StreamResult(finalOut);

							Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);

							transformer.transform(inputFile, xmlOutput);

						}

						KiboPGPUtil.encryptBytes(out, "emptyfile", finalOut.toByteArray(),
								com.kibo.ng.bis.service.KiboPGPUtil.readPublicKey(keyIn),
								encryptionDetail.isAsciiFormatEncoding(), false);

						keyIn.close();
						out.close();
					}

					else if (xsltFile != null) {
						xsltName = xsltFile.getValue();

						Source inputFile = new StreamSource(exportedFile);
						Source xsl = new StreamSource(new File(TRANSFORMATION_FOLDER_PATH + "/" + xsltName));

						File xsltFileOutputFolder = new File(
								EXPORT_FOLDER_PATH + "/" + t10FileCopy.getDirectoryName().getValue());

						if (!xsltFileOutputFolder.exists()) {
							xsltFileOutputFolder.mkdirs();
						}

						File xsltOutputFile = new File(
								xsltFileOutputFolder.getPath() + "/" + t10FileCopy.getFileName());

						Result xmlOutput = new StreamResult(xsltOutputFile);

						Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
						transformer.transform(inputFile, xmlOutput);

						exportedFile = xsltOutputFile;

					}
				}
			}
		}

	}

	/**
	 * This method moves file to working folder.
	 *
	 * @param requestId
	 *            the request id
	 * @param commandFileName
	 *            the command file name
	 * @param commandFile
	 *            the command file
	 */
	private void moveToWorkingFolder(String requestId, String commandFileName, File commandFile) {

		String commandFileBackUpNameXml = requestId + "_" + commandFileName + "." + System.currentTimeMillis() + ".xml";

		File workingFileName = new File(COMMAND_FOLDER_PATH + "/" + commandFileBackUpNameXml);

		commandFile.renameTo(new File(WORKING_FOLDER_PATH + "/" + workingFileName.getName()));
	}

	/**
	 * This method writes success or failure to specified file and sends email
	 * notification.
	 *
	 * @param marketLive
	 *            the market live
	 * @param requestID
	 *            the request ID
	 * @param commandFileName
	 *            the command file name
	 * @param control
	 *            the control
	 * @return true, if successful
	 */
	public boolean writeSuccessFailure(Marketlive marketLive, String requestID, String commandFileName,
			TBatchIntegrationRequest control) {

		marketLive.setImport(null);
		marketLive.setExport(null);

		try {
			Summary summary = marketLive.getResults().getResult().get(0).getSummary();
			JAXBContext jaxbContext = JAXBContext.newInstance(Marketlive.class);

			Marshaller marsheller = jaxbContext.createMarshaller();
			marsheller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			if (summary.getFailed() == null || summary.getFailed() == 0) {

				deleteWorkingFolderFile();

				String succesMsg = "proceesing of " + commandFileName + " is complted";

				File successResultFilepath = new File(
						RESULTS_FOLDER_PATH + "/" + requestID + "_" + commandFileName + "_succes.xml");
				marsheller.marshal(marketLive, successResultFilepath);
				
				logger.info(succesMsg);

				emailService.sendNotification(succesMsg, control);

			} else {
				deleteWorkingFolderFile();

				String failureMsg = "proceesing of " + commandFileName + " is complted with failures";

				File failureResultsFilePath = new File(
						RESULTS_FOLDER_PATH + "/" + requestID + "_" + commandFileName + "_failure.xml");

				marsheller.marshal(marketLive, failureResultsFilePath);
				
				logger.info(failureMsg);

				emailService.sendNotification(failureMsg, control);

				return false;
			}
		} catch (Exception e) {
			String msg = "Error in marshalling while writing succes or failure xml" + e.getMessage();

			logger.error("Error in marshalling while writing succes or failure xml {} ", e);

			emailService.sendFatalEmail(msg);
		}
		return true;
	}

	/**
	 * This method parses the command file.
	 *
	 * @param t8File
	 *            the t 8 file
	 * @param commandFile
	 *            the command file
	 * @param control
	 *            the control
	 * @param siteModel
	 *            the site model
	 * @return the marketlive
	 * @throws Exception
	 *             the exception
	 */
	public Marketlive parseCommandFile(T8File t8File, File commandFile, TBatchIntegrationRequest control,
			SitesModel siteModel) throws Exception {

		String xsltName = t8File.getInputXSLTFileName();

		File outputFile = new File(COMMAND_FOLDER_PATH + "/" + t8File.getOutputXSLTFileName());

		Source input = new StreamSource(new File(COMMAND_FOLDER_PATH + "/" + t8File.getName()));

		Result output = new StreamResult(outputFile);

		ByteArrayOutputStream finalOut = null;

		if (xsltName != null && !xsltName.isEmpty()) {

			input = new StreamSource(new File(COMMAND_FOLDER_PATH + "/" + t8File.getName()));

			Source xsl = new StreamSource(new File(TRANSFORMATION_FOLDER_PATH + "/" + xsltName));

			outputFile = new File(COMMAND_FOLDER_PATH + "/" + t8File.getInputXSLTFileName());

			output = new StreamResult(outputFile);

			if (control.getMerchant().getSite().getFileEncryption() != null && t8File.isEncryptionRequested()) {

				FileInputStream in = new FileInputStream(new File(COMMAND_FOLDER_PATH + "/" + t8File.getName()));

				FileInputStream keyIn = new FileInputStream(siteModel.getSecring());

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				KiboPGPUtil.decryptFile(in, out, keyIn, siteModel.getPassphrase().toCharArray());

				ByteArrayInputStream inStm = new ByteArrayInputStream(out.toByteArray());

				input = new StreamSource(inStm);
				finalOut = new ByteArrayOutputStream();

				output = new StreamResult(finalOut);
			}
			try {

				Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
				transformer.transform(input, output);

				commandFile = outputFile;
			} catch (TransformerException e) {
				String msg = "Error Transforming the csv file to xml " + e.getMessage();

				logger.error("Error Transforming the csv file to xml {} ", e);

				emailService.sendNotification(msg, control);
			}
		} else if (t8File.isEncryptionRequested()) {

			FileInputStream in = new FileInputStream(new File(COMMAND_FOLDER_PATH + "/" + t8File.getName()));
			FileFormatInfo info = FileFormatUtil.detectFileFormat(in);

			if (info.isEncrypted()) {
				FileInputStream keyIn = new FileInputStream(siteModel.getSecring());

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				KiboPGPUtil.decryptFile(in, out, keyIn, siteModel.getPassphrase().toCharArray());
				ByteArrayInputStream inStm = new ByteArrayInputStream(out.toByteArray());

				input = new StreamSource(inStm);

				finalOut = new ByteArrayOutputStream();
				output = new StreamResult(finalOut);

			}
		}

		JAXBContext jaxbContext = JAXBContext.newInstance(Marketlive.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		Marketlive marketLive = null;
		if (finalOut != null) {
			marketLive = (Marketlive) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(finalOut.toByteArray()));
		} else {
			marketLive = (Marketlive) jaxbUnmarshaller.unmarshal(commandFile);
		}

		return marketLive;
	}

	/**
	 * This method is used for File write.
	 *
	 * @param msg
	 *            the msg
	 * @param resultFilePath
	 *            the result file path
	 */
	public void fileWriter(String msg, File resultFilePath) {

		BufferedWriter resultPathBufferedReader = null;
		FileWriter resultPathFileWriter = null;

		try {
			resultPathFileWriter = new FileWriter(resultFilePath);
			resultPathBufferedReader = new BufferedWriter(resultPathFileWriter);

			resultPathBufferedReader.write(msg);
		} catch (IOException e) {
			String errorMsg = "Error  in writing confirmation meassge to results folder" + e.getMessage();

			logger.error("Error  in writing confirmation meassge to results folder {} ", e);

			emailService.sendFatalEmail(errorMsg);
		} finally {
			try {
				if (resultPathBufferedReader != null) {
					resultPathBufferedReader.close();
				}
				if (resultPathFileWriter != null) {
					resultPathFileWriter.close();
				}
			} catch (IOException e) {
				String errorMsg = "Error while trying to close filewriter and bufferedwriter connections"
						+ e.getMessage();

				logger.error("Error while trying to close filewriter and bufferedwriter connections {} ", e);

				emailService.sendFatalEmail(errorMsg);
			}
		}
	}

	/**
	 * This method is Pre populates results.
	 *
	 * @param marketLive
	 *            the market live
	 * 
	 *            Temp method
	 */
	public void prePopulateResults(Marketlive marketLive) {
		Results results = new Results();
		marketLive.setResults(results);

		ConfigInfo info = new ConfigInfo();
		marketLive.getResults().setInfo(info);

		info.setVmId("Hello");
		info.setHost("localhost");
		info.setTimeStamp("123211");

		Summary summary = new Summary();
		CommandResult commandResult = new CommandResult();
		commandResult.setSummary(summary);

		results.getResult().add(commandResult);
	}

	/**
	 * This method parses the control file.
	 *
	 * @param the controlfile
	 *            Control file
	 * @return the t batch integration request
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public TBatchIntegrationRequest parseControlFile(File controlfile) throws JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(TBatchIntegrationRequest.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		JAXBElement<TBatchIntegrationRequest> element = (JAXBElement<TBatchIntegrationRequest>) jaxbUnmarshaller
				.unmarshal(controlfile);

		TBatchIntegrationRequest request = element.getValue();

		return request;
	}

	/**
	 * This method gets the command files.
	 *
	 * @param request
	 *            the request
	 * @return the command files
	 */
	public List<T8File> getCommandFiles(TBatchIntegrationRequest request) {

		List<T8File> commandFiles = request.getMerchant().getSite().getIntegrationWebService().getCommandFiles()
				.getFile();

		Collections.sort(commandFiles, new Comparator<T8File>() {
			@Override
			public int compare(T8File lhs, T8File rhs) {
				return lhs.getSeqNum() < rhs.getSeqNum() ? -1 : (lhs.getSeqNum() > rhs.getSeqNum()) ? 1 : 0;
			}
		});

		return commandFiles;
	}

	/**
	 * This method deletes working folder file.
	 */
	public void deleteWorkingFolderFile() {
		File workingFolderPath = new File(WORKING_FOLDER_PATH);

		if (workingFolderPath.exists()) {
			workingFolderPath.delete();
		}
	}

	/**
	 * not used method
	 * 
	 * private void copyOrigFileToArchive(File commandFile, String requestId) {
	 * // copying command files to archive and renaming it //
	 * checkAndCreateDirectory(ARCHIVEFOLDERPATH); logger.info("name of the
	 * commandFile" + commandFile);
	 * 
	 * try {
	 * 
	 * File archivefile = new File(OUTPUT_ARCHIVE_FOLDER_PATH); File
	 * archiveFileName = new File(OUTPUT_ARCHIVE_FOLDER_PATH + "/" +
	 * commandFile.getName()); File archiveFileNameOrg = new File(
	 * OUTPUT_ARCHIVE_FOLDER_PATH + "/" + requestId + "_" +
	 * commandFile.getName() + ".orig");
	 * FileUtils.copyFileToDirectory(commandFile, archivefile);
	 * archiveFileName.renameTo(archiveFileNameOrg);
	 * 
	 * } catch (IOException e) { String msg = "error occured while trying to
	 * copy command file to archivedirectory after renaming it" +
	 * e.getMessage(); logger.error(msg); emailService.sendFatalEmail(msg + "-"
	 * + commandFile.getName()); } }
	 *
	 * @param sitesList
	 *            the new sites list
	 */

	public void setSitesList(List<SitesModel> sitesList) {
		this.sitesList = sitesList;
	}

}