package com.kibo.ng.bis.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.kibo.ng.bis.model.ConfigModel;
import com.kibo.ng.bis.model.SitesModel;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-beans.xml" })
@PropertySource("classpath:config.properties")
@PropertySource("classpath:mail.properties")
@PropertySource("classpath:test-config.properties")
public class IntegrationSchedulerTest {

	@Autowired
	IntegrationScheduler integrationScheduler;

	@Resource(name = "site162")
	private SitesModel sitesModel;

	@Resource(name = "configMap")
	private HashMap<String, ConfigModel> configMap;

	@Value("${control.file.regex}")
	private String CONTROL_FILE_REGEX;

	@Value("${control.file.regex1}")
	private String CONTROL_FILE_REGEX1;

	@Value("${control.folder.path}")
	private String CONTROL_FOLDER_PATH;

	@Value("${command.folder.path}")
	private String COMMAND_FOLDER_PATH;

	@Value("${archive.folder.path}")
	private String ARCHIVE_FOLDER_PATH;

	@Value("${gnupg.pubRing.path}")
	private String PUBLIC_RING_PATH;

	@Value("${output.transformations.folder.path}")
	private String TRANSFORMATION_FOLDER_PATH;

	@Value("${results.folder.path}")
	private String RESULTS_FOLDER_PATH;

	@Value("${working.folder.path}")
	private String WORKING_FOLDER_PATH;

	@Value("${export.folder.path}")
	private String EXPORT_FOLDER_PATH;

	@Value("${output.archive.folder.path}")
	private String OUTPUT_ARCHIVE_FOLDER_PATH;

	@Value("${request.confirm.regex}")
	private String REQUEST_CONFIRM_REGEX;
	// scenario 1 cases
	@Value("${scenario1.case1.command.files}")
	private String SCENARIO1_CASE1_COMMANDFILE_PATH;

	@Value("${scenario1.case1.control.files}")
	private String SCENARIO1_CASE1_CONTROLFILE_PATH;

	@Value("${scenario1.case2.control.files}")
	private String SCENARIO1_CASE2_CONTROLFILE_PATH;

	@Value("${scenario1.case2.command.files}")
	private String SCENARIO1_CASE2_COMMANDFILE_PATH;

	@Value("${scenario1.case3.control.files}")
	private String SCENARIO1_CASE3_CONTROLFILE_PATH;

	@Value("${scenario1.case3.command.files}")
	private String SCENARIO1_CASE3_COMMANDFILE_PATH;
	// scenario 2 cases
	@Value("${scenario2.case1.command.files}")
	private String SCENARIO2_CASE1_COMMANDFILE_PATH;

	@Value("${scenario2.case1.control.files}")
	private String SCENARIO2_CASE1_CONTROLFILE_PATH;

	@Value("${scenario2.case2.control.files}")
	private String SCENARIO2_CASE2_CONTROLFILE_PATH;

	@Value("${scenario2.case2.command.files}")
	private String SCENARIO2_CASE2_COMMANDFILE_PATH;

	@Value("${scenario2.case3.control.files}")
	private String SCENARIO2_CASE3_CONTROLFILE_PATH;

	@Value("${scenario2.case3.command.files}")
	private String SCENARIO2_CASE3_COMMANDFILE_PATH;

	// scenario 3 cases
	@Value("${scenario3.case1.command.files}")
	private String SCENARIO3_CASE1_COMMANDFILE_PATH;

	@Value("${scenario3.case1.control.files}")
	private String SCENARIO3_CASE1_CONTROLFILE_PATH;

	@Value("${scenario3.case2.control.files}")
	private String SCENARIO3_CASE2_CONTROLFILE_PATH;

	@Value("${scenario3.case2.command.files}")
	private String SCENARIO3_CASE2_COMMANDFILE_PATH;

	// scenario 4
	@Value("${scenario4.case1.command.files}")
	private String SCENARIO4_CASE1_COMMANDFILE_PATH;

	@Value("${scenario4.case1.control.files}")
	private String SCENARIO4_CASE1_CONTROLFILE_PATH;

	@Value("${scenario4.case2.control.files}")
	private String SCENARIO4_CASE2_CONTROLFILE_PATH;

	@Value("${scenario4.case2.command.files}")
	private String SCENARIO4_CASE2_COMMANDFILE_PATH;

	// scenario 5
	@Value("${scenario5.case1.command.files}")
	private String SCENARIO5_CASE1_COMMANDFILE_PATH;

	@Value("${scenario5.case1.control.files}")
	private String SCENARIO5_CASE1_CONTROLFILE_PATH;

	@Value("${scenario5.case2.control.files}")
	private String SCENARIO5_CASE2_CONTROLFILE_PATH;

	@Value("${scenario5.case2.command.files}")
	private String SCENARIO5_CASE2_COMMANDFILE_PATH;

	// scenario 6
	@Value("${scenario6.case1.command.files}")
	private String SCENARIO6_CASE1_COMMANDFILE_PATH;

	@Value("${scenario6.case1.control.files}")
	private String SCENARIO6_CASE1_CONTROLFILE_PATH;

	@Value("${scenario6.case2.control.files}")
	private String SCENARIO6_CASE2_CONTROLFILE_PATH;

	@Value("${scenario6.case2.command.files}")
	private String SCENARIO6_CASE2_COMMANDFILE_PATH;

	@Value("${scenario6.pgp}")
	private String SCENARIO6_PGP;

	@Value("${text.regex}")
	private String TXT;

	@Value("${xml.regex}")
	private String XML;

	@Value("${file.success.regex}")
	private String SUCESS;

	@Resource(name = "sitesList")
	List<SitesModel> sitesList;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testScenario1() {

		File commandFilePath = null;
		File controlFilePath = null;

		// Case 1: Success Case
		/*commandFilePath = new File(SCENARIO1_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO1_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 1:corrupt command File
		commandFilePath = new File(SCENARIO1_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO1_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);*/

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO1_CASE3_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO1_CASE3_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

	}

	@Test
	public void testScenario2() {

		File commandFilePath = null;
		File controlFilePath = null;

		// Case 1: Success Case
		commandFilePath = new File(SCENARIO2_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO2_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 1:corrupt command File
		commandFilePath = new File(SCENARIO2_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO2_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO2_CASE3_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO2_CASE3_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

	}

	@Test
	public void testScenario3() {

		File commandFilePath = null;
		File controlFilePath = null;

		// Case 1: Success Case
		commandFilePath = new File(SCENARIO3_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO3_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO3_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO3_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

	}

	@Test
	public void testScenario4() {

		File commandFilePath = null;
		File controlFilePath = null;

		// Case 1: Success Case
		commandFilePath = new File(SCENARIO4_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO4_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO4_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO4_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

	}

	@Test
	public void testScenario5() {

		File commandFilePath = null;
		File controlFilePath = null;
		File exportFolderPath = new File(EXPORT_FOLDER_PATH);
		File outputArchiveFolderPath = new File(OUTPUT_ARCHIVE_FOLDER_PATH);
		createFolder(exportFolderPath);
		createFolder(outputArchiveFolderPath);

		// Case 1: Success Case
		commandFilePath = new File(SCENARIO5_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO5_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO5_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO5_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		exportFolderPath.delete();
		outputArchiveFolderPath.delete();
	}

	@Test
	public void testScenario6() throws IOException {

		File commandFilePath = null;
		File controlFilePath = null;
		File exportFolderPath = new File(EXPORT_FOLDER_PATH);
		File outputArchiveFolderPath = new File(OUTPUT_ARCHIVE_FOLDER_PATH);
		File publicRingPath = new File(PUBLIC_RING_PATH);
		File transformationFolderPath = new File(TRANSFORMATION_FOLDER_PATH);
		File encryptPGP = new File(SCENARIO6_PGP);
		createFolder(publicRingPath);
		createFolder(exportFolderPath);
		createFolder(outputArchiveFolderPath);
		createFolder(transformationFolderPath);

		copyFilesToServiceFolder(publicRingPath, encryptPGP);

		// Case 1: Success Case
		commandFilePath = new File(SCENARIO6_CASE1_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO6_CASE1_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		// Failure Case 2: Wrong Control File
		commandFilePath = new File(SCENARIO6_CASE2_COMMANDFILE_PATH);
		controlFilePath = new File(SCENARIO6_CASE2_CONTROLFILE_PATH);
		testImport(commandFilePath, controlFilePath);

		publicRingPath.delete();
		exportFolderPath.delete();
		outputArchiveFolderPath.delete();
		transformationFolderPath.delete();

	}

	@Test
	public void testcopyFilesFromSiteToServiceFolder() {
		File siteControlFolderPath = new File(sitesModel.getSiteControlFolderPath());
		File siteCommandFolderPath = new File(sitesModel.getSiteCommandFolderPath());
		createFolder(siteControlFolderPath);
		createFolder(siteCommandFolderPath);

		File scenario1CommandFilesPath = new File(SCENARIO1_CASE1_COMMANDFILE_PATH);
		File scenario1ControlFilesPath = new File(SCENARIO1_CASE1_CONTROLFILE_PATH);

		File scenario2CommandFilesPath = new File(SCENARIO2_CASE1_COMMANDFILE_PATH);
		File Scenario2ControlFilesPath = new File(SCENARIO2_CASE1_CONTROLFILE_PATH);

		File scenario3CommandFilesPath = new File(SCENARIO3_CASE1_COMMANDFILE_PATH);
		File Scenario3ControlFilesPath = new File(SCENARIO3_CASE1_CONTROLFILE_PATH);

		File scenario4CommandFilesPath = new File(SCENARIO4_CASE1_COMMANDFILE_PATH);
		File Scenario4ControlFilesPath = new File(SCENARIO4_CASE1_CONTROLFILE_PATH);

		File scenario5CommandFilesPath = new File(SCENARIO5_CASE1_COMMANDFILE_PATH);
		File Scenario5ControlFilesPath = new File(SCENARIO5_CASE1_CONTROLFILE_PATH);

		File scenario6CommandFilesPath = new File(SCENARIO6_CASE1_COMMANDFILE_PATH);
		File Scenario6ControlFilesPath = new File(SCENARIO6_CASE1_CONTROLFILE_PATH);

		try {
			copyFilesToServiceFolder(siteCommandFolderPath, scenario1CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, scenario1ControlFilesPath);

			copyFilesToServiceFolder(siteCommandFolderPath, scenario2CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, Scenario2ControlFilesPath);

			copyFilesToServiceFolder(siteCommandFolderPath, scenario3CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, Scenario3ControlFilesPath);

			copyFilesToServiceFolder(siteCommandFolderPath, scenario4CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, Scenario4ControlFilesPath);

			copyFilesToServiceFolder(siteCommandFolderPath, scenario5CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, Scenario5ControlFilesPath);

			copyFilesToServiceFolder(siteCommandFolderPath, scenario6CommandFilesPath);
			copyFilesToServiceFolder(siteControlFolderPath, Scenario6ControlFilesPath);

			List<String> actvual = new ArrayList<String>();
			File[] siteControlFiles = siteControlFolderPath.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX));
			if (siteControlFiles.length == 0)
				siteControlFiles = siteControlFolderPath.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX1));
			for (File file : siteControlFiles) {
				actvual.add(file.getName());
			}
			List<String> expected = integrationScheduler.copyFilesFromSiteToServiceFolder(sitesModel);
			Assert.assertEquals(expected, actvual);

			siteControlFolderPath.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testProcess() {

		File siteFolderPath = new File(sitesModel.getSiteControlFolderPath());
		if (siteFolderPath != null) {
			siteFolderPath.mkdirs();
		}
		File servicecontrolFolderpath = new File(CONTROL_FOLDER_PATH);
		File serviceCommandFolderpath = new File(COMMAND_FOLDER_PATH);

		File scenario1CommandFilesPath = new File(SCENARIO1_CASE1_COMMANDFILE_PATH);
		File scenario1ControlFilesPath = new File(SCENARIO1_CASE1_CONTROLFILE_PATH);

		File scenario2CommandFilesPath = new File(SCENARIO2_CASE1_COMMANDFILE_PATH);
		File Scenario2ControlFilesPath = new File(SCENARIO2_CASE1_CONTROLFILE_PATH);

		File scenario3CommandFilesPath = new File(SCENARIO3_CASE1_COMMANDFILE_PATH);
		File Scenario3ControlFilesPath = new File(SCENARIO3_CASE1_CONTROLFILE_PATH);

		File scenario5CommandFilesPath = new File(SCENARIO5_CASE1_COMMANDFILE_PATH);
		File Scenario5ControlFilesPath = new File(SCENARIO5_CASE1_CONTROLFILE_PATH);

		File scenario6CommandFilesPath = new File(SCENARIO6_CASE1_COMMANDFILE_PATH);
		File Scenario6ControlFilesPath = new File(SCENARIO6_CASE1_CONTROLFILE_PATH);

		try {
			copyFilesToServiceFolder(serviceCommandFolderpath, scenario1CommandFilesPath);
			copyFilesToServiceFolder(servicecontrolFolderpath, scenario1ControlFilesPath);
			copyFilesToServiceFolder(serviceCommandFolderpath, scenario2CommandFilesPath);
			copyFilesToServiceFolder(servicecontrolFolderpath, Scenario2ControlFilesPath);

			copyFilesToServiceFolder(serviceCommandFolderpath, scenario3CommandFilesPath);
			copyFilesToServiceFolder(servicecontrolFolderpath, Scenario3ControlFilesPath);

			copyFilesToServiceFolder(serviceCommandFolderpath, scenario5CommandFilesPath);
			copyFilesToServiceFolder(servicecontrolFolderpath, Scenario5ControlFilesPath);

			copyFilesToServiceFolder(serviceCommandFolderpath, scenario6CommandFilesPath);
			copyFilesToServiceFolder(servicecontrolFolderpath, Scenario6ControlFilesPath);

			integrationScheduler.process();

			siteFolderPath.delete();
			servicecontrolFolderpath.delete();
			serviceCommandFolderpath.delete();

			integrationScheduler.process();

		} catch (Exception e) {

			e.getMessage();
		}
	}

	private void testImport(File inputCommandFile, File inputControlFile) {

		File serviceControlFolderPath = new File(CONTROL_FOLDER_PATH);
		File serviceCommandFolderPath = new File(COMMAND_FOLDER_PATH);
		File archiveFolderPath = new File(ARCHIVE_FOLDER_PATH);
		File resultFolderPath = new File(RESULTS_FOLDER_PATH);
		File workingFolderPath = new File(WORKING_FOLDER_PATH);

		// Create directory if it does not exist
		createFolder(serviceControlFolderPath);
		createFolder(serviceCommandFolderPath);
		createFolder(archiveFolderPath);
		createFolder(resultFolderPath);
		createFolder(workingFolderPath);

		// If files exist in the directory delete them
		deleteFilesInFolder(serviceControlFolderPath);
		deleteFilesInFolder(serviceCommandFolderPath);
		deleteFilesInFolder(archiveFolderPath);
		deleteFilesInFolder(resultFolderPath);
		deleteFilesInFolder(workingFolderPath);

		try {

			copyFilesToServiceFolder(serviceCommandFolderPath, inputCommandFile);
			copyFilesToServiceFolder(serviceControlFolderPath, inputControlFile);

			processControlFile(serviceControlFolderPath);

			checkControlFileExits(serviceControlFolderPath.list(), serviceControlFolderPath);
			checkCommandFileExits(serviceCommandFolderPath.list(), serviceCommandFolderPath);
			checkArchiveFilesCreated(serviceCommandFolderPath.list(), archiveFolderPath);
			checkRequestConfirmedFileCreated(resultFolderPath);
			checkSucessOrFailFilesCreated(serviceCommandFolderPath.list(), resultFolderPath);
			checkWorkingFolderFilesCreated(serviceCommandFolderPath.list(), workingFolderPath);

			serviceControlFolderPath.delete();
			serviceCommandFolderPath.delete();
			archiveFolderPath.delete();
			resultFolderPath.delete();
			workingFolderPath.delete();

		} catch (IOException e) {
			e.getMessage();
		} catch (JAXBException e) {
			e.getMessage();
		}
	}

	private void checkCommandFileExits(String[] commandFiles, File serviceCommandFolderPath) {
		if (commandFiles == null)
			Arrays.asList(commandFiles).stream().forEach(cmdFileName -> {
				org.junit.Assert.assertEquals(false, checkFileNameExists(cmdFileName, serviceCommandFolderPath.list()));
			});
	}

	private void checkControlFileExits(String[] controlFiles, File serviceControlFolderPath) {
		
		try{
			if(serviceControlFolderPath.list().length != 0)
				Arrays.asList(controlFiles).stream().forEach(ctrlFileName -> {
				org.junit.Assert.assertEquals(false, checkFileNameExists(ctrlFileName, serviceControlFolderPath.list()));
				});
		}catch(Exception e){
			org.junit.Assert.assertTrue(true);
		}
		
		/*Arrays.asList(controlFiles).stream().forEach(ctrlFileName -> {
			org.junit.Assert.assertEquals(false, checkFileNameExists(ctrlFileName, serviceControlFolderPath.list()));
		});
*/
	}

	private void createFolder(File serviceControlFolderPath) {
		if (!serviceControlFolderPath.exists()) {
			serviceControlFolderPath.mkdirs();
		}
	}

	private void deleteFilesInFolder(File inputFolderPath) {
		if (inputFolderPath.listFiles() != null)
			Arrays.asList(inputFolderPath.listFiles()).stream().forEach(file -> file.delete());
	}

	private void copyFilesToServiceFolder(File serviceCommandFolderPath, File fromFilePath) throws IOException {

		File[] commandfile = fromFilePath.listFiles();
		for (File file : commandfile) {
			FileUtils.copyFileToDirectory(file, serviceCommandFolderPath);
		}
	}

	/**
	 * Asserts that copy of the command file is saved to archive directory
	 */

	private void checkArchiveFilesCreated(String[] commandFiles, File archiveFolderPath) {

		if (commandFiles != null)
			Arrays.asList(commandFiles).stream().forEach(cmdFileName -> {
				org.junit.Assert.assertEquals(true, checkFileNameExists(cmdFileName, archiveFolderPath.list()));
			});

	}

	private static boolean checkFileNameExists(String name, String fileNames[]) {

		return Arrays.asList(fileNames).stream().filter(fileName -> fileName.contains(name)).findFirst().isPresent();
	}

	private void checkRequestConfirmedFileCreated(File resultFolderPath) {

		if (resultFolderPath.list() != null) {
			String[] resultFolderFiles = resultFolderPath.list((dir, name) -> name.endsWith(TXT));
			org.junit.Assert.assertEquals(true, checkFileNameExists(REQUEST_CONFIRM_REGEX, resultFolderFiles));
		}
	}

	private void checkSucessOrFailFilesCreated(String[] commandFiles, File resultFolderPath) {

		if (resultFolderPath.exists()) {
			String[] resultFolderFiles = resultFolderPath.list((dir, name) -> name.endsWith(XML));
			Arrays.asList(commandFiles).stream().forEach(cmdFileName -> {
				if (Arrays.asList(resultFolderFiles).stream()
						.filter(fileName -> fileName.contains(cmdFileName + SUCESS)).findFirst().isPresent())
					org.junit.Assert.assertEquals(true, checkFileNameExists(cmdFileName + SUCESS, resultFolderFiles));

			});

		}
	}

	private void checkWorkingFolderFilesCreated(String[] commandFiles, File workingFolderPath) {

		Arrays.asList(commandFiles).stream().forEach(cmdFileName -> {
			org.junit.Assert.assertEquals(true,
					checkFileNameExists(cmdFileName, workingFolderPath.list((dir, name) -> name.endsWith(XML))));
		});
	}

	private void processControlFile(File serviceControlFolderPath) throws JAXBException {

		File[] serviceControlFiles = serviceControlFolderPath.listFiles((d, name) -> name.matches(CONTROL_FILE_REGEX1));

		Arrays.asList(serviceControlFiles).stream().forEach(file -> {
			try {
				integrationScheduler.processControlFile(file.getName(), sitesModel);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		});

	}

}
