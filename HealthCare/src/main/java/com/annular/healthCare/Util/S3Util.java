package com.annular.healthCare.Util;




import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Configuration
@Getter
@Setter
public class S3Util {

    public static final Logger logger = LoggerFactory.getLogger(S3Util.class);

    @Value("${s3.region.name}")
    private String s3RegionName;
    
    
    @Value("${s3.bucket.name}")
    private String s3BucketName;

    @Value("${s3.baseURL}")
    private String s3BaseURL;

    public static final String S3_PATH_DELIMITER = "/";

   // public AwsCredentialsProvider getAwsCredentialsProvider() {
    	
    	public AwsCredentialsProvider getAwsCredentialsProvider() {
    	    ProfileCredentialsProvider customProfileProvider = ProfileCredentialsProvider.builder()
    	            .profileFile(ProfileFile.builder()
    	                    .type(ProfileFile.Type.CREDENTIALS)  // Specify the type!
    	                    .content(Paths.get("C:\\Users\\.aws\\credentials.txt"))
 // Your custom credentials file path
    	                   // .content(Paths.get("/home/ubuntu/.aws/credentials.txt"))
    	                    .build())
    	            .build();
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(DefaultCredentialsProvider.create())
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(customProfileProvider)
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
    
//    public AwsCredentialsProvider getAwsCredentialsProvider() {
//        return AwsCredentialsProviderChain.builder()
//                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
//                .addCredentialsProvider(DefaultCredentialsProvider.create())
//                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
//                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
//                .addCredentialsProvider(ProfileCredentialsProvider.create())
//                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
//                .build();
//    }

    public S3AsyncClient buildS3ClientAsync() {
        return S3AsyncClient.builder()
                .credentialsProvider(getAwsCredentialsProvider())
                .region(Region.of(s3RegionName))
                .build();
    }

    public S3Client buildS3ClientSync() {
        return S3Client.builder()
                .region(Region.of(s3RegionName))
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    public List<S3Object> getAllObjectsFromS3Bucket(String bucketName) {
        logger.info("In getAllObjectsFromS3Bucket() Bucket Name :- [{}] ", bucketName);
        List<S3Object> objects = null;
        try (S3Client client = buildS3ClientSync()) {
            ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).build();
            ListObjectsResponse response = client.listObjects(request);
            objects = response.contents();
            logger.info("S3 Objects count :- [{}] ", objects.size());
        } catch (S3Exception e) {
            logger.error("Error in getAllObjectsFromS3Bucket{}", e.awsErrorDetails().errorMessage());
            e.printStackTrace();
        }
        return objects;
    }

    public List<S3Object> getAllObjectsFromS3Bucket(String bucketName, String destinationPath) {
        logger.info("In getAllObjectsFromS3Bucket() Bucket Name :- [{}], Destination path :- [{}] ", bucketName, destinationPath);
        List<S3Object> objects = null;
        try (S3Client client = buildS3ClientSync()) {
            ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).prefix(destinationPath).build();
            ListObjectsResponse response = client.listObjects(request);
            objects = response.contents();
            logger.info("S3 Objects count :- [{}] ", objects.size());
        } catch (S3Exception e) {
            logger.error("Error in getListOfFileFromS3Bucket{}", e.awsErrorDetails().errorMessage());
            e.printStackTrace();
        }
        return objects;
    }

    public byte[] getObjectAsBytes(String bucketName, String objectKey) {
        logger.info("In getObjectAsBytes() Bucket Name :- [{}], objectKey :- [{}] ", bucketName, objectKey);
        try (S3Client s3Client = buildS3ClientSync()) {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(objectKey)
                    .bucket(bucketName)
                    .build();
            return s3Client.getObjectAsBytes(objectRequest).asByteArray();
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Put object into the s3 bucket Synchronously
     */
    public String putObjectIntoS3(String bucketName, String destinationPath, File file) {
        logger.info("In putObjectIntoS3() Details to upload into S3 :: Bucket Name :- [{}], destinationPath :- [{}] ", bucketName, destinationPath);
        try (S3Client s3Client = buildS3ClientSync()) {
            logger.info("File Path :- " + file.getAbsoluteFile().getAbsolutePath() + ", File Name :- " + file.getName());
            PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(destinationPath).build();
            PutObjectResponse response = s3Client.putObject(objectRequest, Paths.get(file.getAbsoluteFile().getAbsolutePath()));
            return response != null ? "File uploaded" : "Error";
        } catch (Exception e) {
            logger.error("Error at object upload...", e);
            return null;
        }
    }

    /**
     * Put object into the s3 bucket Asynchronously
     */
    public void putObjectIntoS3Async(String bucketName, String destinationPath, File file) {
        logger.info("In putObjectIntoS3Async() Details to upload into S3 :: Bucket Name :- [{}], destinationPath :- [{}] ", bucketName, destinationPath);
        String responseFromS3 = null;
        try (S3AsyncClient s3AsyncClient = buildS3ClientAsync()) {
            logger.info("File Path :- " + file.getAbsoluteFile().getAbsolutePath() + ", File Name :- " + file.getName());
            CompletableFuture<PutObjectResponse> futureCompletion = this.putObjectInS3bucketAsync(bucketName, destinationPath, file.getAbsoluteFile().getAbsolutePath(), file.getName(), s3AsyncClient);
            responseFromS3 = this.executeFutureCompletion(futureCompletion, "Uploaded successfully");
        } catch (Exception e) {
            responseFromS3 = "Error";
        }
        if (!responseFromS3.equalsIgnoreCase("Uploaded successfully")) {
            logger.info("Error while uploading file into S3...");
        }
    }

    /**
     * Put object into the s3 bucket Asynchronously
     */
    private CompletableFuture<PutObjectResponse> putObjectInS3bucketAsync(String s3Bucket, String destinationPath, String absolutePath, String name, S3AsyncClient s3AsyncClient) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(s3Bucket).key(destinationPath + name).build();
        return s3AsyncClient.putObject(objectRequest, Paths.get(absolutePath));
    }

    private String executeFutureCompletion(CompletableFuture<?> futureCompletion, String msgToPrint) {
        AtomicReference<String> returnMessage = new AtomicReference<>("");
        futureCompletion.whenComplete((resp, throwable) -> {
            try {
                if (resp != null) {
                    returnMessage.set(msgToPrint);
                    logger.info("{}: {}", msgToPrint, resp);
                } else {
                    returnMessage.set("Error");
                    logger.error("Uploaded failed..{}", throwable.getMessage());
                    throwable.printStackTrace(); // Handle error
                }
            } catch (Exception e) {
                returnMessage.set("Error");
                logger.error("Uploaded failed: {}", throwable.getMessage());
                throwable.printStackTrace();
            }
        });
        futureCompletion.join();
        return returnMessage.get();
    }

    public void deleteAllObjectFromS3Async(String bucketName, String destPath) {
        logger.info("In deleteAllObjectFromS3Async() Destination to delete all objects from S3 :- [{}]", destPath);
        List<S3Object> s3ObjectList = this.getAllObjectsFromS3Bucket(bucketName, destPath);
        if (!s3ObjectList.isEmpty()) this.deleteObjectsFromS3(bucketName, s3ObjectList);
    }

    public void deleteObjectsFromS3(String bucketName, List<S3Object> s3ObjectList) {
        logger.info("S3 objects size to delete :- [{}]", s3ObjectList.size());
        try (S3AsyncClient s3Client = buildS3ClientAsync()) {
            if (!s3ObjectList.isEmpty()) {
                List<ObjectIdentifier> objectIdentifiers = s3ObjectList.stream()
                        .map(item -> ObjectIdentifier.builder().key(item.key()).build())
                        .collect(Collectors.toList());

                DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(objectIdentifiers).build())
                        .build();

                CompletableFuture<DeleteObjectsResponse> deleteObjectsFuture = s3Client.deleteObjects(dor);
                this.executeFutureCompletion(deleteObjectsFuture, "Objects Deleted Successfully");
            }
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    public void deleteObjectFromS3(String bucketName, String key) {
        logger.info("S3 objects key to delete :- [{}]", key);
        try (S3AsyncClient s3Client = buildS3ClientAsync()) {
            DeleteObjectRequest dr = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            CompletableFuture<DeleteObjectResponse> deleteObjectsFuture = s3Client.deleteObject(dr);
            this.executeFutureCompletion(deleteObjectsFuture, "Objects Deleted Successfully");
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    public byte[] getObjectAsBytes(String bucketName, List<S3Object> s3data) {
        try (S3Client s3Client = buildS3ClientSync()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (S3Object s3Object : s3data) {
                GetObjectRequest objectRequest = GetObjectRequest.builder()
                        .key(s3Object.key())
                        .bucket(bucketName)
                        .build();
                try {
                    s3Client.getObject(objectRequest, ResponseTransformer.toOutputStream(outputStream));
                } catch (S3Exception e) {
                    logger.error("Error downloading object: {}", s3Object.key(), e);
                    // Handle the error as needed
                }
            }
            return outputStream.toByteArray();
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    public String generateS3FilePath(String filePath) {
        return s3BaseURL + S3Util.S3_PATH_DELIMITER + filePath;
    }
    
    public void deleteFile(String filePath) {
        logger.info("Attempting to delete file from S3: [{}]", filePath);
        try (S3AsyncClient s3Client = buildS3ClientAsync()) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(filePath)
                    .build();

            CompletableFuture<DeleteObjectResponse> deleteFuture = s3Client.deleteObject(deleteRequest);

            deleteFuture.whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Error deleting file from S3: {}", exception.getMessage());
                } else {
                    logger.info("File deleted successfully from S3: [{}]", filePath);
                }
            });

            // Wait for async operation to finish if needed
            deleteFuture.join();

        } catch (S3Exception e) {
            logger.error("S3Exception while deleting file: {}", e.awsErrorDetails().errorMessage());
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected error deleting file from S3", ex);
            throw new RuntimeException("Error deleting S3 file", ex);
        }
    }


}
        