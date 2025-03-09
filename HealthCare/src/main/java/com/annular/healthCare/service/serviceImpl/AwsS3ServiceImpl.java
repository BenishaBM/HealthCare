package com.annular.healthCare.service.serviceImpl;




import java.io.File;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Util.S3Util;
import com.annular.healthCare.service.AwsS3Service;

import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class AwsS3ServiceImpl implements AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);

    @Autowired
    S3Util s3Util;

    @Override
    public List<S3Object> getAllObjectsByBucket(String bucketName) {
        return s3Util.getAllObjectsFromS3Bucket(bucketName);
    }

    @Override
    public List<S3Object> getAllObjectsByBucketAndDestination(String bucketName, String destinationPath) {
        return s3Util.getAllObjectsFromS3Bucket(bucketName, destinationPath);
    }

    @Override
    public byte[] getObjectFromS3(String bucketName, String key) {
        return s3Util.getObjectAsBytes(bucketName, key);
    }

    @Override
    public String putObjectIntoS3(String bucketName, String destinationPath, File inputFile) {
        return s3Util.putObjectIntoS3(bucketName, destinationPath, inputFile);
    }

    @Override
    public void putObjectIntoS3Async(String bucketName, String destinationPath, File inputFile) {
        s3Util.putObjectIntoS3Async(bucketName, destinationPath, inputFile);
    }

    @Override
    public void deleteAllObjectsFromDestination(String bucketName, String destinationPath) {
        s3Util.deleteAllObjectFromS3Async(bucketName, destinationPath);
    }

    @Override
    public void deleteObjectsFromS3(String bucketName, List<S3Object> s3ObjectList) {
        s3Util.deleteObjectsFromS3(bucketName, s3ObjectList);
    }

    @Override
    public void deleteObjectFromS3(String bucketName, String objectKey) {
        s3Util.deleteObjectFromS3(bucketName, objectKey);
    }

    @Override
    public byte[] getObjectFromS3(String bucketName, List<S3Object> s3data) {
        return s3Util.getObjectAsBytes(bucketName, s3data);
    }


}