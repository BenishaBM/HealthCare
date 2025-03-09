package com.annular.healthCare.service;




import java.io.File;
import java.util.List;

import software.amazon.awssdk.services.s3.model.S3Object;

public interface AwsS3Service {

    List<S3Object> getAllObjectsByBucket(String bucketName);

    List<S3Object> getAllObjectsByBucketAndDestination(String bucketName, String destinationPath);

    byte[] getObjectFromS3(String bucketName, String key);

    byte[] getObjectFromS3(String bucketName, List<S3Object> s3data);

    String putObjectIntoS3(String bucketName, String destinationPath, File inputData);

    void putObjectIntoS3Async(String bucketName, String destinationPath, File inputData);

    void deleteAllObjectsFromDestination(String bucketName, String destinationPath);

    void deleteObjectsFromS3(String bucketName, List<S3Object> s3ObjectList);

    void deleteObjectFromS3(String bucketName, String objectKey);
}