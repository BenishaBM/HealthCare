package com.annular.healthCare.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;


public class Base64FileUpload {
	
	 private static final Logger logger = LoggerFactory.getLogger(Base64FileUpload.class);
	    
	    @Value("${file.upload.path}")
	    private static String fileUploadPath;


	public static void saveFile(String uploadDirectory, String base64Image, String fileName) throws IOException {

		byte[] imageData = DatatypeConverter.parseBase64Binary(base64Image);
		Path uploadPath = Paths.get(uploadDirectory);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		String path = uploadDirectory + "/" + fileName;
		File file = new File(path);

		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			outputStream.write(imageData);
		}

	}
	
	public static void saveFiles(String uploadDirectory, String base64Data, String fileName) throws IOException {
	    byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
	    Path uploadPath = Paths.get(uploadDirectory);

	    if (!Files.exists(uploadPath)) {
	        Files.createDirectories(uploadPath); // Ensure directory exists
	    }

	    Path filePath = uploadPath.resolve(fileName);
	    Files.write(filePath, decodedBytes);
	}

	public static String encodeToBase64Strings(String uploadDirectory, String fileName) throws IOException {
	    Path filePath = Paths.get(uploadDirectory, fileName);

	    if (!Files.exists(filePath)) {
	        throw new FileNotFoundException("File not found: " + filePath.toString());
	    }

	    byte[] fileContent = Files.readAllBytes(filePath);
	    return Base64.getEncoder().encodeToString(fileContent);
	}


	public static String encodeToBase64String(String uploadDirectory, String fileName) throws IOException {
		String filePath = uploadDirectory + "/" + fileName;
		File fi = new File(filePath);
		byte[] fileContent = Files.readAllBytes(fi.toPath());

		String encodedString = Base64.getEncoder().encodeToString(fileContent);

		return encodedString;

	}

	public static void deleteFile(String uploadDirectory, String fileName) throws IOException {
		Path uploadPath = Paths.get(uploadDirectory);
		Path file = uploadPath.resolve(fileName);
		Files.deleteIfExists(file);
	}

	 public static String encodeToBase64String(String fileName) throws IOException {
	        try {
	            Path filePath = Paths.get(fileUploadPath, fileName);
	            if (!Files.exists(filePath)) {
	                logger.error("File does not exist: {}", filePath);
	                return "";
	            }

	            byte[] fileContent = Files.readAllBytes(filePath);
	            if (fileContent == null || fileContent.length == 0) {
	                logger.error("File is empty: {}", filePath);
	                return "";
	            }

	            return Base64.getEncoder().encodeToString(fileContent);
	        } catch (IOException e) {
	            logger.error("Error reading file {}: {}", fileName, e.getMessage());
	            throw e;
	        }
	    

	 }



	 public static void deleteFile(String fileName) throws IOException {
	     Path filePath = Paths.get(fileName);
	     Files.delete(filePath);
	 }

	 public static void saveFile(String fileData, String fileName) {
	        try {
	            // Decode the Base64 string into a byte array
	            byte[] decodedBytes = Base64.getDecoder().decode(fileData);

	            // Create a FileOutputStream to write the bytes to a file
	            try (FileOutputStream fos = new FileOutputStream(fileName)) {
	                fos.write(decodedBytes);
	            }

	            System.out.println("File saved successfully: " + fileName);
	        } catch (IOException e) {
	            System.err.println("Error saving file: " + e.getMessage());
	        }
	    }
}