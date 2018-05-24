/*package com.kibo.ng.bis.service;


 * (C) Copyright MarketLive. 2007. All rights reserved.
 * MarketLive is a trademark of MarketLive, Inc.
 * Warning: This computer program is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this program, or any portion of it, may result
 * in severe civil and criminal penalties, and will be prosecuted to the maximum extent
 * possible under the law.
 

import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
//TODO Refactor this to use slf4j logger with appenders.
import org.apache.log4j.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.jdom.Namespace;
import org.w3c.dom.Document;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.charset.Charset;

*//**
 * Class replaces the DecryptUtil class from the Util module in ML5.
 * It uses most of the code from DecryptUtil.java, but with some
 * modifications to accomodate the new PBE routines for RSA private
 * key decryption.
 * 
 * Its sole function is to decrypt XML that was exported by the 
 * Integration module.  Functions that previously existed in the
 * DecryptUtil.java - generating RSA key-pairs - and in 
 * InsertKeys.java  - inserting an encrypted key-pair into the
 * database - is now performed by KMDriver.java.
 *//*

public class DecryptXML {

    private static final Logger logger = Logger.getLogger(DecryptXML.class);
    private static int errorCount = 0;
    private static int idocCount = 0;
    private static int replacementCount = 0;
    private static int totalReplacementCount = 0;
    private static ArrayList config = new ArrayList();
    private static List row = new ArrayList();
    private static SAXBuilder builder = null;
    private static org.jdom.Document doc = null;
    private static final String PROVIDER = "BC";
    
    private static String propertiesFile = "decryption.properties";
    private static String configFile = "encryptedFields.xml";
    private static String logPropertiesFile = "log4jDecryptUtil.properties";
    
    private static String sourceXMLPath = "";
    private static String destinationXMLPath = "";
    private static boolean deleteEncryptedXMLFilesAfterDecryption = false;
    
    private static String defaultXMLDestinationPath = "decrypted_xml_files";
    private static final  String simpleDateFormat = "yyyyMMddHHmmss";
    private static final  String errorMessage = "===> One or more failures, see log for details";

    private static String defaultNameSpacePrefix = "ml";
    
    // Added for PCI-DSS
    private static DBPropertiesBean dbpb = null;        // JDBC properties
    private static KEKPropertiesBean kekpb = null;      // KEK properties
    private static SecretKey pbekey = null;             // PBE key
    private static CryptoManager cm = null;             // CryptoManager instance
    private static RSAPrivateKey pvk = null;            // RSA Private Key

    *//**
     * This is the only public method in this class; it decrypts XML
     * files that were previously exported with encrypted CC numbers. 
     * 
     * @param   keklocation - the KEK filename
     * @param   dbplocation - the location of the db.properties file
     * @param   configdir - the directory location containing
     * properties and XML configuration files that tells this class
     * what it needs to do
     *
     * @throws FileNotFoundException when the KEK file or the 
     * db.properties file is not found
     * @throws InvalidLocationException when the directory location
     * of the db.properties file is not found
     * @throws NonexistentJDBCParameterException when a required JDBC 
     * property is missing from db.properties
     * @throws  NonexistentKEKParameterException when a required KEK 
     * property is missing from the KEK file
     * @throws NoSuchAlgorithmException when the crypto algorith is 
     * not found
     * @throws InvalidKeySpecException when the PBE key-spec is invalid
     * @throws InvalidKeyException when the PBE or Private key are invalid
     * @throws NoSuchPaddingException when the padding for encryption is
     * invalid
     * @throws InvalidAlgorithmParameterException when a parameter for a
     * crypto algorithm is invalid
     * @throws UnsupportedEncodingException when the Base64 encoding is
     * not available on this machine
     * @throws OtherKeyMgtException for any other error
     *//*
    public static void execute(
            String keklocation, String dbplocation, String configdir) 
        throws FileNotFoundException, InvalidLocationException,
            NonexistentJDBCParameterException, 
            NonexistentKEKParameterException, NoSuchAlgorithmException, 
            InvalidKeySpecException, InvalidKeyException, 
            NoSuchPaddingException, InvalidAlgorithmParameterException, 
            UnsupportedEncodingException, OtherKeyMgtException {

        String logDir = "";
        boolean isLogging = false;

        if((null != System.getenv("ENVIRONMENT")) && System.getenv("ENVIRONMENT").equalsIgnoreCase("production")) {
            System.out.println("Can't run on production servers");
            System.exit(1);
        }
        
        // Is configuration directory location zero-length?
        if (configdir.length() == 0)
            throw new InvalidLocationException("DecryptXML configuration directory location cannot be zero-length");
        else if (!configdir.substring((configdir.length() - 1), configdir.length()).equals(System.getProperty("file.separator")))
            throw new InvalidLocationException("DecryptXML configuration directory location " +
                    "must be a directory and end with a " + System.getProperty("file.separator"));
        
        // Does decryption.properties file exist in the directory?
        File dpf = new File(configdir + propertiesFile);
        if (!dpf.exists())
            throw new FileNotFoundException(
                    "Properties file does NOT exist at this location: " + dpf.getAbsolutePath());
        
        propertiesFile = configdir + propertiesFile;
        
        // Does encryptedFields.xml file exist in the directory?
        File cf = new File(configdir + configFile);
        if (!cf.exists())
            throw new FileNotFoundException(
                    "Configuration file does NOT exist at this location: " + cf.getAbsolutePath());

        configFile = configdir + configFile;
        
        // Does log4jDecryptUtil.properties file exist in the directory?
        File lpf = new File(configdir + logPropertiesFile);
        if (!lpf.exists())
            throw new FileNotFoundException(
                    "Properties file does NOT exist at this location: " + lpf.getAbsolutePath());
        
        logPropertiesFile = configdir + logPropertiesFile;
        
         read the config file, initialize the logger 
        Properties log4jProperties = new Properties();
        try {
            log4jProperties.load(new FileInputStream(lpf));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat formatter = new SimpleDateFormat(simpleDateFormat);
        Date startTime = new Date();
        Date endTime = null;

        try {
            logDir = log4jProperties.getProperty("log4j.appender.A1.File");
            new File(logDir).mkdir();
            log4jProperties.setProperty("log4j.appender.A1.File", logDir + "/DecryptXML_" +
                formatter.format(new Date()) + ".html");
            PropertyConfigurator.configure(log4jProperties);
            isLogging = true;
        } catch(Exception e) {
            System.out.println("Failed to initialize logging to file ----> Logging to Console instead:");
            ConsoleAppender appender = new ConsoleAppender(new SimpleLayout());
            logger.addAppender(appender);
        }
        logger.setLevel(Level.DEBUG);

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(dpf));
        } catch (IOException e) {
            System.out.println(errorMessage);
            logger.error("Could not read properties file: " + e.getMessage());
            System.exit(1);
        }

        // Get JDBC parameters
        dbpb = Utilities.getDBProperties(dbplocation);
        
        File file = null;
        sourceXMLPath = properties.getProperty("sourceXMLPath");
        if(sourceXMLPath == null || sourceXMLPath.length() < 1) {
            logger.error("No path found to source XML files: Set property: sourceXMLPath correctly in " +
                propertiesFile);
            terminateProcess();
        } else {
            file = new File(sourceXMLPath);
            if(!file.exists()) {
                logger.error("The specified path to the XML file(s) does not exist: Set property: sourceXMLPath correctly in "
                    + propertiesFile);
                terminateProcess();
            }
        }

        destinationXMLPath = properties.getProperty("destinationXMLPath");
        if(destinationXMLPath == null || destinationXMLPath.length() < 1) {
            new File(defaultXMLDestinationPath).mkdir();
            destinationXMLPath = defaultXMLDestinationPath;
        } else {
            File path = new File(destinationXMLPath);
            if(!path.exists())
                path.mkdirs();
        }
        String del = properties.getProperty("deleteEncryptedXMLFilesAfterDecryption", "false");
        deleteEncryptedXMLFilesAfterDecryption = Boolean.parseBoolean(del);

        // Get KEK properties
        kekpb = Utilities.getKEKProperties(keklocation);
        
        // Generate the new PBE key 
        pbekey = new KeystoreManager().generatePBEKey(kekpb.getKeycode());
        
        // Extract RSA Private Key from database
        String b64pvkct = new KeystoreManager().getKey(kekpb.getKeycode(), dbpb, "private");
        
        // Unwrap RSA PrivateKey
        cm = new CryptoManager();
        pvk = (RSAPrivateKey) cm.unwrap(b64pvkct.getBytes("utf-8"), pbekey, 
                kekpb.getSalt(), kekpb.getIterations());

        // Process the configuration file
        processConfigFile();
        
        // Process the XML documents
        processDocs(file);

        formatter   =   new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        endTime     =   new Date();

        if(idocCount == 0) {
            logger.info("No XML documents found in " + sourceXMLPath + ", check sourceXMLPath value in " +
                propertiesFile);
        }
        logger.info(idocCount + " XML document(s) processed, " + errorCount + " error(s) encountered, " +
            totalReplacementCount + " replacement(s) made");
        logger.info(formatter.format(endTime) + " Job took: " + ((endTime.getTime() - startTime.getTime()) / 1000) +
            " seconds");
        if(isLogging) {
            System.out.println(idocCount + " XML document(s) processed, " + errorCount +
            " error(s) encountered, " + totalReplacementCount + " replacement(s) made, see log for details");
        }
    }

    *//**
     * Parses the config file and collects data regarding the XML files to be decrypted.
     *//*
    private static void processConfigFile() {

        builder = new SAXBuilder();
        boolean debugging = false;

        try {
            doc = builder.build(new File(configFile));
        } catch (JDOMException e) {
            logger.error("Error parsing " + configFile + "\n" + e.getMessage());
            errorCount++;
            terminateProcess();
        } catch (IOException e) {
            logger.error("Error parsing " + configFile + "\n" + e.getMessage());
            errorCount++;
            terminateProcess();
        }

        Element root = doc.getRootElement();

        if(root.getChildren("encryptedfield") == null || root.getChildren("encryptedfield").size() < 1) {
            logger.debug("No encrypted field configuration elements found in " + configFile);
            errorCount++;
            terminateProcess();
        }

        List configNodes = root.getChildren("encryptedfield");
        Iterator iterator = configNodes.iterator();
        //Iterate through every encryptedfield block
        while(iterator.hasNext()) {

            boolean xPath = false;
            boolean key = false;
            boolean field = false;
            Element encryptedField = (Element)iterator.next();
            if(encryptedField.getChildren() != null) {
                List encryptedFieldNodes = encryptedField.getChildren();
                Element node;
                ArrayList row = new ArrayList();
                //Iterate through each element in a single encryptedfield block, and collect the field names
                for(int j=0; j < encryptedFieldNodes.size(); j++) {
                    node = (Element)encryptedFieldNodes.get(j);
                    if(node.getName().equalsIgnoreCase("xpath") && node.getText().length() > 0) {
                        row.add(0,node.getText());
                        xPath = true;
                    }
                    if(node.getName().equalsIgnoreCase("key") && node.getText().length() > 0) {
                        row.add(1,node.getText());
                        key = true;
                    }
                    if(node.getName().equalsIgnoreCase("field") && node.getText().length() > 0) {
                        row.add(node.getText());
                        field = true;
                    }
                }
                if(!xPath) {
                    logger.debug("No xPath config parameter found in an <encryptedfield> block");
                } else if(!key) {
                    logger.debug("No key config parameter found in an <encryptedfield> block");
                } else if(!field) {
                    logger.debug("No field config parameter found in an <encryptedfield> block");
                } else {
                    config.add(row);
                }
            } else {
                logger.info("Empty encryptedfield block found in config file");
            }
        }
        if(config.size() < 1) {
            logger.error("No configuration parameters found");
            errorCount++;
            terminateProcess();
        }
        if (debugging) {
            for (int j=0; j < config.size(); j++) {
                 row = (ArrayList)config.get(j);
                 for (int k=0; k < row.size(); k++)
                    System.out.println("config entry " + j + " is: " + ((String)row.get(k)));
            }
        }
    }

    *//**
     * Parses and decrypts required fields in XML Export Files
     *//*
    private static void processDocs(File file) {

        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                processDocs(new File(file, files[i]));
            }
        } else {
            if (file.getName().endsWith(".xml")) {

                logger.info("Processing: " + file.getName());
                replacementCount = 0;
                int fileErrorCount = 0;
                try {
                    doc = builder.build(file);
                } catch (JDOMException e) {
                    logger.error("Error parsing " + file.getName() + "\n" + e.getMessage());
                    errorCount++;
                    fileErrorCount++;
                } catch (IOException e) {
                    logger.error("Error parsing " + file.getName() + "\n" + e.getMessage());
                    errorCount++;
                    fileErrorCount++;
                }

                XPath toEncryptedField;
                List encFieldElements;
                String xPath = "";

                String keyID = null;
                String encryptedField = null;
                Namespace docNameSpace = null;
                String docNameSpacePrefix = "";

                if(doc.getRootElement() != null)
                    docNameSpace = doc.getRootElement().getNamespace();
                if(docNameSpace.hashCode() != Namespace.NO_NAMESPACE.hashCode()) {
                    if(docNameSpace.getURI() != null && docNameSpace.getURI().length() > 0) {
                        if(docNameSpace.getPrefix() == null || docNameSpace.getPrefix().length() < 1) {
                            docNameSpacePrefix = defaultNameSpacePrefix;
                            docNameSpace = Namespace.getNamespace(docNameSpacePrefix, docNameSpace.getURI());
                        }

                    }
                }

                for(int i=0; i < config.size(); i++) {

                    encFieldElements = null;
                    xPath = "";
                    toEncryptedField = null;

                    ArrayList encFieldInfo = (ArrayList)config.get(i);

                    String prefixedXPath = "";

                    if(docNameSpacePrefix != null && docNameSpacePrefix.length() > 0) {
                            prefixedXPath = ((String)encFieldInfo.get(0)).replace("/","/" + docNameSpacePrefix + ":");
                            xPath = "//" + docNameSpacePrefix + ":" + prefixedXPath;
                    }
                    else
                        xPath = "//" + (String)encFieldInfo.get(0);

                    try {
                        toEncryptedField = XPath.newInstance(xPath);
                        toEncryptedField.addNamespace(docNameSpace);
                    } catch (JDOMException e) {
                        logger.error(e.getMessage());
                        errorCount++;
                        continue;
                    }
                    try {
                        encFieldElements = toEncryptedField.selectNodes(doc);
                    } catch (JDOMException e) {
                        logger.error("Evaluation of XPath: " + xPath + " failed\n" + e.getMessage());
                        errorCount++;
                        fileErrorCount++;
                        continue;
                    }
                    if(encFieldElements == null || encFieldElements.size() < 1) {
                        logger.warn("Cant find any XML elements in " + file.getName() + " that match XPath: " + (String)encFieldInfo.get(0));
                        continue;
                    }
                    Iterator encFieldIter = encFieldElements.iterator();
                    while (encFieldIter.hasNext()) {

                        Element encFieldParent = (Element)encFieldIter.next();
                        Element key = encFieldParent.getChild((String)encFieldInfo.get(1),docNameSpace);
                        if(key == null)
                            key = encFieldParent.getChild((String)encFieldInfo.get(1));
                        if(key == null) {
                            logger.error("Failed to get key element <" + (String)encFieldInfo.get(1) + "> from <" +
                                encFieldParent.getName() + "> in " + file.getName());
                            errorCount++;
                            fileErrorCount++;
                            continue;
                        }
                        keyID = key.getText();

                        for(int j=2; j < encFieldInfo.size(); j++) {
                            Element field = encFieldParent.getChild((String)encFieldInfo.get(j),docNameSpace);
                            if(field == null)
                                field = encFieldParent.getChild((String)encFieldInfo.get(j));
                            if(field != null) {
                                encryptedField = field.getText();
                                String decryptedField = "";
                                try {
                                    decryptedField = decrypt(encryptedField, pvk);
                                    field.setText(decryptedField);
                                    replacementCount++;
                                    totalReplacementCount++;
                                } catch (Exception e) {
                                    logger.error("Could not decrypt " + encryptedField + " in file: " + file.getName() +
                                        " using key file: " + keyID + ".pri" + "\n" + e.getMessage());
                                    errorCount++;
                                    fileErrorCount++;
                                }
                            } else {
                                logger.debug("Cannot find encrypted element: <{}" + (String)encFieldInfo.get(j) +
                                    "> under <{}" + encFieldParent.getName() + "> in file: {}" + file.getName());
                                errorCount++;
                            }
                        }
                    }
                }

                if(replacementCount > 0) {

                    DOMOutputter out = new DOMOutputter();
                    Document document = null;
                    try {
                        document = out.output(doc);
                    } catch (JDOMException e) {
                        logger.error("Error converting document" + e.getMessage());
                        fileErrorCount++;
                    }
                    OutputFormat format = new OutputFormat(document);
                    format.setLineSeparator(LineSeparator.Windows);
                    format.setIndenting(true);
                    format.setLineWidth(0);
                    format.setEncoding("UTF-8");
                    format.setPreserveSpace(true);

                    XMLSerializer serializer = null;
                    try {
                        serializer = new XMLSerializer (new OutputStreamWriter
                                (new FileOutputStream(destinationXMLPath + "/DECRYPTED_" +
                                file.getName()), Charset.forName("UTF-8")), format);
                    } catch (IOException e) {
                        errorCount++;
                        fileErrorCount++;
                        logger.error("Output XML file could not be generated " + e.getMessage());
                        terminateProcess();
                    }
                    try {
                        serializer.asDOMSerializer();
                    } catch (IOException e) {
                        errorCount++;
                        fileErrorCount++;
                        logger.error("Output XML file could not be generated: " + e.getMessage());
                        terminateProcess();
                    }
                    try {
                        serializer.serialize(document);
                    } catch (IOException e) {
                        errorCount++;
                        fileErrorCount++;
                        logger.error("Output XML file could not be generated: " + e.getMessage());
                        terminateProcess();
                    }
                    logger.info("XML file: " + destinationXMLPath + "/DECRYPTED_" + file.getName() + " created, " +
                        replacementCount + " encrypted field replacement(s) made, " + fileErrorCount +
                        " error(s) encountered");
                    if(deleteEncryptedXMLFilesAfterDecryption && (fileErrorCount == 0)) {
                        if(file.delete()) {
                            logger.info("XML File: " + file.getName() + " deleted");
                        } else {
                            logger.error("Could not delete XML file: " + file.getName());
                        }
                    }
                } else {
                    logger.info("XML file: " + file.getName() + ": No decryptions performed, " + fileErrorCount +
                        " error(s) encountered");
                }
                idocCount++;
            }
        }
    }

    private static void terminateProcess() {
            System.out.println("Process terminated, see log for details");
            System.exit(1);
    }

    *//**
     * Decrypt the input string and return the decrypted string.
     * @param encryptedString the encrypted string to decipher.
     * @return returns an unencrypted string.
     *//*
    private static String decrypt(String encryptedString, RSAPrivateKey privateKey) {
        String decryptedString = "";
        byte[] encryptedData = null;

        try {
            // Base-64 decode the string
            encryptedData = decodeBase64(encryptedString);
        } catch (DecoderException de) {
            throw new RuntimeException("Base64 Error", de);
        }
        try {
            // RSA decrypt the data
            decryptedString = decryptRSA(encryptedData, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt field: " + e.getMessage(), e);
        }

        return decryptedString;
    }

    *//**
     * Decrypts a string using the implemented decryption scheme and returns decrypted string.
     * @param encryptedData the string to decrypt.
     * @return returns a decryptedString.
     *//*
    private static final String decryptRSA(byte[] encryptedData, RSAPrivateKey privateKey) {

        Security.addProvider(new BouncyCastleProvider());
        String decryptedString = null;
        try {
            Cipher decoder = Cipher.getInstance("RSA/ECB/PKCS1Padding",PROVIDER);
            decoder.init(Cipher.DECRYPT_MODE,privateKey);
            decryptedString = new String(decoder.doFinal(encryptedData));
        } catch (Exception e) {
            throw new RuntimeException("Decryption Error", e);
        }
        return decryptedString;
    }


    *//**
    * Decode Base64 encoded string to byte array.
    * @param inString The string
    * @return Bytes array
    * @throws DecoderException
    *//*
    private static byte[] decodeBase64(String inString) throws DecoderException {
        byte [] decodedBytes = null;
        decodedBytes = Base64.decodeBase64(inString.getBytes());
        return decodedBytes;
    }

}
*/