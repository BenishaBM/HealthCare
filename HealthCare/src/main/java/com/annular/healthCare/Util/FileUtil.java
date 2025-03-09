package com.annular.healthCare.Util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import com.annular.healthCare.model.User;
import com.annular.healthCare.service.AwsS3Service;

import software.amazon.awssdk.services.s3.model.S3Object;

@Configuration
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final Set<String> IMAGE_FORMATS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> VIDEO_FORMATS = Set.of(".mp4", ".mkv", ".flv", ".mov", ".avi", ".wmv");

    @Autowired
    AwsS3Service awsS3Service;

    @Autowired
    S3Util s3Util;

    public String uploadFile(File file, String destinationPath) {
        return awsS3Service.putObjectIntoS3(s3Util.getS3BucketName(), destinationPath, file);
    }

    public void uploadFileAsync(File file, String destinationPath) {
        awsS3Service.putObjectIntoS3Async(s3Util.getS3BucketName(), destinationPath, file);
    }

    public byte[] downloadFile(List<S3Object> s3data) {
        return awsS3Service.getObjectFromS3(s3Util.getS3BucketName(), s3data);
    }

    public byte[] downloadFile(String objectKey) {
        return awsS3Service.getObjectFromS3(s3Util.getS3BucketName(), objectKey);
    }

    public void deleteFiles(List<S3Object> s3ObjectList) {
        awsS3Service.deleteObjectsFromS3(s3Util.getS3BucketName(), s3ObjectList);
    }

    public void deleteFile(String objectKey) {
        awsS3Service.deleteObjectFromS3(s3Util.getS3BucketName(), objectKey);
    }

    public List<S3Object> getS3Objects(String destinationPath) {
        return awsS3Service.getAllObjectsByBucketAndDestination(s3Util.getS3BucketName(), destinationPath);
    }

    public static void convertMultiPartFileToFile(MultipartFile multipartFile, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
    }

    public static String generateDestinationPath(User user, String category) {
        return new StringBuilder()
                .append(category)
                .append("/")
                .append(user.getUserId())
                .append("/")
                .toString();
    }

    public static String generateFilePath(User user, String category, String fileName) {
        return new StringBuilder()
                .append(category)
                .append("/")
                .append(user.getUserId())
                .append("/")
                .append(fileName)
                .toString();
    }

    public static void convertMultiPartFileToFile(MultipartFile[] images, File file) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
            for (MultipartFile image : images) {
                byte[] bytes = image.getBytes();
                outputStream.write(bytes);
            }
        }
    }

    public static String generateFilePath(String category) {
        return new StringBuilder()
                .append(category)
                .append("/")
                .toString();
    }

    public static String generateDestinationPath(String category) {
        return new StringBuilder()
                .append(category)
                .append("/")
                .toString();
    }

    public static boolean isImageFile(String fileFormat) {
        return IMAGE_FORMATS.contains(fileFormat);
    }

    public static boolean isVideoFile(String fileFormat) {
        return VIDEO_FORMATS.contains(fileFormat);
    }


//    public static void compressImageFile(File inputFile, String inputFileType, File outputFile) {
//        try {
//            logger.info("Input image file size to compress -> [{}] with file format -> [{}]", inputFile.length(), inputFileType);
//
//            /*BufferedImage inputImage = ImageIO.read(inputFile);
//
//            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(inputFileType);
//            ImageWriter writer = writers.next();
//
//            ImageOutputStream outputStream = ImageIO.createImageOutputStream(outputFile);
//            writer.setOutput(outputStream);
//
//            ImageWriteParam params = writer.getDefaultWriteParam();
//            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            params.setCompressionQuality(0.5f);
//
//            writer.write(null, new IIOImage(inputImage, null, null), params);
//
//            outputStream.close();
//            writer.dispose();
//            logger.info("Compressed image file size -> [{}]", outputFile.length());*/
//
//            IVCompressor compressor = new IVCompressor();
//            byte[] compressedFile = compressor.resizeImageUsingFile(inputFile, ImageFormats.valueOf(inputFileType.toUpperCase()), ResizeResolution.R360P);
//            convertByteArrayToFile(outputFile, compressedFile);
//            logger.info("Compressed image file size using IVCompressor -> [{}]", compressedFile.length);
//
//        } catch (Exception e) {
//            logger.error("Error at compressImageFile() -> {}", e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public static void compressVideoFile(File inputFile, String inputFileType, File outputFile) {
//        try {
//            logger.info("Input video file size to compress -> [{}] with file format -> [{}]", inputFile.length(), inputFileType);
//
//            IVCompressor compressor = new IVCompressor();
//            byte[] compressedFile = compressor.reduceVideoSize(inputFile, VideoFormats.valueOf(inputFileType.toUpperCase()), ResizeResolution.R360P);
//            convertByteArrayToFile(outputFile, compressedFile);
//
//            logger.info("Compressed video file size -> [{}]", outputFile.length());
//        } catch (Exception e) {
//            logger.error("Error at compressVideoFile() -> {}", e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public static void convertByteArrayToFile(File outputFile, byte[] bytes) {
//        try {
//            OutputStream os = new FileOutputStream(outputFile);
//            os.write(bytes);
//            os.close();
//        } catch (Exception e) {
//            logger.error("Error at convertByteArrayToFile() -> {}", e.getMessage());
//            e.printStackTrace();
//        }
//    }

}