package com.kibo.ng.bis.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.springframework.stereotype.Component;

@Component("pgpFileDecryptService")
public class PGPFileDecryptService {

	/**
	 * Logger
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String BOUNCY_CASTLE_PROVIDER = "BC";

	private static final String DECRYPTED_TEXT_FILESTREAM = "decryptedTextFileStream";

	private static final String ENCRYPTED_FILESTREAM = "encryptedFileStream";

	private static final String PRIVATE_KEY_STREAM = "privKeyStream";

	public static final String SKIP_FOR_SECOND_RUN = "NOT VALID ENCRYTED MESSAGE 2ND RUN";
	
	private String fileExtension;

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/***
	 *  process the encrypted file and decrypt it.
	 * @param sourceLocation
	 * @param destinationLocation
	 * @param pgpKeyLocation
	 * @param pgpPassphrase
	 * @param errorFileLocation
	 * @param archiveLocation
	 * @return
	 */
	public boolean processFiles(String sourceLocation, String destinationLocation, String pgpKeyLocation, 
			String pgpPassphrase, String errorFileLocation, String archiveLocation) {

		boolean result = false;
		
		if (null != sourceLocation && null != destinationLocation && null != pgpKeyLocation && null != pgpPassphrase &&
				null != errorFileLocation && null != archiveLocation && null != fileExtension) {
            logger.debug("sourceLocation : {}", sourceLocation);
            logger.debug(" destinationLocation {}", destinationLocation);
            logger.debug(" pgpKeyLocation {}", pgpKeyLocation);
            logger.debug(" pgpPassphrase {}", pgpPassphrase );
            logger.debug(" errorFileLocation {}", errorFileLocation );
            logger.debug(" archiveLocation {}", archiveLocation);
            logger.debug(" fileExtension {}", fileExtension);
		} else {
		/*	logger.error("sourceLocation {} destinationLocation {} pgpKeyLocation {} pgpPassphrase {} errorFileLocation {} archiveLocation {} fileExtension {}",
					sourceLocation, destinationLocation, pgpKeyLocation, pgpPassphrase, errorFileLocation, archiveLocation, fileExtension);*/
			//throwError("the required parameter must be set");
			System.out.println("Something Wrong");
		}
		
		File[] encryptedFiles = getEncryptedFile(sourceLocation);
		if (logger.isDebugEnabled()) {
			logger.debug("In POCommandProcessor process() starting decrypting file...");
            logger.debug("encryptedFiles size: {}", encryptedFiles.length);
		}
		if (null != encryptedFiles && encryptedFiles.length > 0) {
			List<String> messages = new ArrayList<String>();
			messages = getFileDecrypted(encryptedFiles, messages,pgpKeyLocation,destinationLocation,sourceLocation,
										pgpPassphrase,errorFileLocation,archiveLocation);
			if (messages.size() > 0) {
				throwError(messages);
			} else {
				result = true;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("In POCommandProcessor process() end");
		}
		return result;
	}
	
	/**
	 * 
	 * @param encryptedFiles
	 * @param messages
	 * @param pgpKeyLocation
	 * @param destinationLocation
	 * @param sourceLocation
	 * @param pgpPassphrase
	 * @param errorFileLocation
	 * @param archiveLocation
	 * @return
	 */
	private List<String> getFileDecrypted(File[] encryptedFiles, List<String> messages, String pgpKeyLocation, 
			String destinationLocation, String sourceLocation, String pgpPassphrase, String errorFileLocation,
			String archiveLocation) {
		StringBuilder errorMessages;

		try {
			if (Security.getProvider(BOUNCY_CASTLE_PROVIDER) == null) {
				Security.addProvider(new BouncyCastleProvider());
			}
			String encryptedFileName = null;
            String decryptedFileName = null;
            String encryptedFilePath = null;
            String decryptedFilePath = null;
			for (File file : encryptedFiles) {
				encryptedFileName = file.getName();
                encryptedFilePath = sourceLocation+encryptedFileName;
                decryptedFileName = FilenameUtils.removeExtension(encryptedFileName);
                decryptedFilePath = destinationLocation + decryptedFileName;
                if(logger.isDebugEnabled()){
                    logger.debug("encryptedFileName: {}", encryptedFileName);
                    logger.debug("encryptedFilePath: {}", encryptedFilePath);
                    logger.debug("decryptedFileName: {}", decryptedFileName);
                    logger.debug("decryptedFilePath: {}", decryptedFilePath);
                }

				try {// Decrypt the file
                    FileInputStream privKeyStream = new FileInputStream(pgpKeyLocation);
                    FileOutputStream decryptedTextFileStream = new FileOutputStream(decryptedFilePath);
                    InputStream encryptedFileStream = new BufferedInputStream(new FileInputStream(encryptedFilePath));
					decryptFile(encryptedFileStream, decryptedTextFileStream, privKeyStream, pgpPassphrase.toCharArray());
				} catch (PGPException pgpException) {
                    logger.error("Error Decrypting File!",pgpException);
					String exceptionMessage = pgpException.getMessage();
					if (SKIP_FOR_SECOND_RUN.equals(exceptionMessage)) {
						errorMessages = new StringBuilder();
						errorMessages.append("Decryption error in Order status file ").append(encryptedFileName).append(" File left for another trial ");
						messages.add(errorMessages.toString());
					} else {
						errorMessages = new StringBuilder();
						errorMessages.append(", Error in decrypting file: ").append(encryptedFileName).append(" Error message ").append(pgpException.getMessage());
						messages.add(errorMessages.toString());
					}
					deleteDecryptedFile(encryptedFileName,destinationLocation);
					copyErrorFile(file, errorFileLocation);
					continue;
				} catch (Exception exception) {
                    logger.error("Error Decrypting File!",exception);
					errorMessages = new StringBuilder();
					errorMessages.append(", Error in decrypting file: ").append(encryptedFileName).append(" Error message ").append(exception.getMessage());
					messages.add(errorMessages.toString());
					errorMessages.append(", Error in decrypting file: " + encryptedFileName + " Error message " + exception.getMessage());
					deleteDecryptedFile(encryptedFileName, destinationLocation);
					copyErrorFile(file, errorFileLocation);
					continue;
				}
				// archive the encrypted file
				copyArchivedFile(file, archiveLocation);

			}

		} catch (Exception ex) {
			errorMessages = new StringBuilder();
			errorMessages.append(", Error in decrypting file: ").append(" Error message ").append(ex.getMessage());
			messages.add(errorMessages.toString());
			logger.error("ERROR", ex);
		}
		return messages;
	}

	private File[] getEncryptedFile(String sourceLocation) {
		File[] encryptedFiles = null;
		try {
            logger.debug("sourceLocation: {}", sourceLocation);
			File encryptedFileFolder = new File(sourceLocation);
            logger.debug("encryptedFileFolder: {}", encryptedFileFolder.getAbsolutePath());
            logger.debug("Exists? {}", encryptedFileFolder.exists());
			encryptedFiles = encryptedFileFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (null != name) {
                        logger.debug("Name: {}", name);
                        logger.debug("fileExtension: {}", fileExtension);
						Pattern pattern = Pattern.compile(fileExtension);
						Matcher matcher = pattern.matcher(name);
                        boolean ret = matcher.matches();
                        logger.debug("Matches? {}", ret);
						return ret;
					} else {
                        logger.debug("Pattern: {}", fileExtension + " did find match in string: {}", name);
						return false;
					}
				}
			});
		} catch (Exception e) {
			//throwError("Error in getting the files from the given location" + e.getMessage());
			e.printStackTrace();
		}
		return encryptedFiles;

	}

	/**
	 * decrypt the passed in message stream
	 */
	private void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd) throws Exception {

		if (Security.getProvider(BOUNCY_CASTLE_PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		in = PGPUtil.getDecoderStream(in);

		PGPObjectFactory pgpF = new PGPObjectFactory(in);
		PGPEncryptedDataList enc;

		Object o = pgpF.nextObject();
		//
		// the first object might be a PGP marker packet.
		//
		if (o != null && o.getClass() != null) {
			logger.warn(" o instance of " + o.getClass().getName());
		} else {
			logger.warn(" o instance of " + o);
		}

		if (o instanceof PGPEncryptedDataList) {
			enc = (PGPEncryptedDataList) o;
		} else {
			enc = (PGPEncryptedDataList) pgpF.nextObject();
		}

		//
		// find the secret key
		//
		if (enc == null) {
			throw new PGPException(SKIP_FOR_SECOND_RUN);
		}
		Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
		PGPPrivateKey sKey = null;
		PGPPublicKeyEncryptedData pbe = null;

		while (sKey == null && it.hasNext()) {
			pbe = it.next();

			sKey = findSecretKey(keyIn, pbe.getKeyID(), passwd);
		}

		if (sKey == null) {
			throw new IllegalArgumentException("Secret key for message not found.");
		}

		InputStream clear = pbe.getDataStream(sKey, BOUNCY_CASTLE_PROVIDER);

		PGPObjectFactory plainFact = new PGPObjectFactory(clear);

		Object message = plainFact.nextObject();

		if (message instanceof PGPCompressedData) {
			PGPCompressedData cData = (PGPCompressedData) message;
			PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

			message = pgpFact.nextObject();
			if (message instanceof PGPOnePassSignatureList) {// Message is a
																// signed object
																// so have to
																// get the
																// literal from
																// PGP from the
																// message.
				message = pgpFact.nextObject();
			}
		}
		if (message instanceof PGPLiteralData) {
			PGPLiteralData ld = (PGPLiteralData) message;
			InputStream unc = ld.getInputStream();
			IOUtils.copy(unc, out);
		} else if (message instanceof PGPOnePassSignatureList) {
			throw new PGPException("Encrypted message contains a signed message - not literal data.");
		} else {
			throw new PGPException("Message is not a simple encrypted file - type unknown.");
		}

		if (pbe.isIntegrityProtected()) {
			if (!pbe.verify()) {
				throw new PGPException("Message failed integrity check");
			}
		}
	}

	/**
	 * Load a secret key ring collection from keyIn and find the secret key
	 * corresponding to keyID if it exists.
	 * 
	 * @param keyIn
	 *            input stream representing a key ring collection.
	 * @param keyID
	 *            keyID we want.
	 * @param pass
	 *            passphrase to decrypt secret key with.
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */
	private static PGPPrivateKey findSecretKey(InputStream keyIn, long keyID, char[] pass) throws IOException, PGPException, NoSuchProviderException {
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

		if (pgpSecKey == null) {
			return null;
		}

		return pgpSecKey.extractPrivateKey(pass, BOUNCY_CASTLE_PROVIDER);
	}

	private void copyErrorFile(File file, String errorFileLocation) throws FileNotFoundException, IOException {
		File errorEncryptedFile = new File(errorFileLocation + file.getName());
		//FileUtils.copyFile(file, errorEncryptedFile); // TODO nithin write with our classes
	}

	private void deleteDecryptedFile(String fileName, String destinationLocation) {
		File decryptFile = new File(destinationLocation + fileName);
		decryptFile.delete();
	}

	private void copyArchivedFile(File file, String archiveLocation) throws FileNotFoundException, IOException {
		File archiveFile = new File(archiveLocation + file.getName());
		//FileUtils.copyFile(file, archiveFile); // TODO nithin write with our classes
		// delete the encrypted file
		file.delete();
	}

	/**
	 * closeOutPutFileStream
	 * 
	 * @param fileOutputStream
	 * @param streamName
	 */
	private void closeOutPutFileStream(OutputStream fileOutputStream, String streamName) {
		if (fileOutputStream != null) {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				logger.error("Error in closing the outputStream " + streamName + " Error message" + e);
			}
		}
	}

	/**
	 * closeInPutFileStream
	 * 
	 * @param fileIntputStream
	 * @param streamName
	 */
	private void closeInPutFileStream(InputStream fileIntputStream, String streamName) {
		if (fileIntputStream != null) {
			try {
				fileIntputStream.close();
			} catch (IOException e) {
				logger.error("Error in closing the inputStream " + streamName + " Error message" + e);
			}
		}
	}

	private void throwError(List<String> messages) {
		for (String message : messages) {
			logger.error(message);
		}
		//throwError("The Decryption process of files falied due to above error logs. ");
	}

}
