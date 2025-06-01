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

//
//	public static void saveFile(String uploadDirectory, String base64Image, String fileName) throws IOException {
//
//		byte[] imageData = DatatypeConverter.parseBase64Binary(base64Image);
//		Path uploadPath = Paths.get(uploadDirectory);
//		if (!Files.exists(uploadPath)) {
//			Files.createDirectories(uploadPath);
//		}
//		String path = uploadDirectory + "/" + fileName;
//		File file = new File(path);
//
//		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
//			outputStream.write(imageData);
//		}
//
//	}
	    public static void saveFile(String uploadDirectory, String base64Image, String fileName) throws IOException {
	        System.out.println("▶ Attempting to save file...");
	        System.out.println("→ Upload Directory: " + uploadDirectory);
	        System.out.println("→ File Name: " + fileName);

	        byte[] imageData = DatatypeConverter.parseBase64Binary(base64Image);
	        Path uploadPath = Paths.get(uploadDirectory);

	        if (!Files.exists(uploadPath)) {
	            Files.createDirectories(uploadPath);
	            System.out.println("📁 Directory created at: " + uploadPath.toAbsolutePath());
	        }

	        String path = uploadDirectory + "/" + fileName;
	        File file = new File(path);

	        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
	            outputStream.write(imageData);
	            System.out.println("✅ File saved at: " + file.getAbsolutePath());
	        } catch (IOException e) {
	            System.err.println("❌ Error writing file: " + e.getMessage());
	            throw e;
	        }
	    }

	

	    public static String encodeToBase64String(String uploadDirectory, String fileName) throws IOException {
	        Path path = Paths.get(uploadDirectory, fileName);  // Proper platform-independent join
	        if (!Files.exists(path)) {
	            System.err.println("File not found at: " + path.toAbsolutePath());
	            return "";
	        }
	        byte[] fileContent = Files.readAllBytes(path);
	        return Base64.getEncoder().encodeToString(fileContent);
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