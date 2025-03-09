package com.annular.healthCare.service.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.annular.healthCare.Util.FileUtil;
import com.annular.healthCare.Util.S3Util;
import com.annular.healthCare.Util.Utility;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.service.AuthService;
import com.annular.healthCare.service.MediaFileService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.FileOutputWebModel;

@Service
public class MediaFilesServiceImpl implements MediaFileService {

    public static final Logger logger = LoggerFactory.getLogger(MediaFilesServiceImpl.class);

    @Autowired
    MediaFileRepository mediaFilesRepository;

    @Autowired
    FileUtil fileUtil;

    @Autowired
    AuthService userService;

    @Autowired
    S3Util s3Util;

    @Override
    public List<FileOutputWebModel> saveMediaFiles(FileInputWebModel fileInputWebModel, User user) {
        List<FileOutputWebModel> fileOutputWebModelList = new ArrayList<>();
        try {
            // 1. Save first in MySQL
            Map<MediaFile, MultipartFile> mediaFilesMap = this.prepareMultipleMediaFilesData(fileInputWebModel, user);
            logger.info("Saved MediaFiles rows list size :- [{}]", mediaFilesMap.size());

            // 2. Upload into S3
            mediaFilesMap.forEach((mediaFile, inputFile) -> {
                mediaFilesRepository.saveAndFlush(mediaFile);
                try {
                    File tempFile = File.createTempFile(mediaFile.getFileIds(), null);
                    FileUtil.convertMultiPartFileToFile(inputFile, tempFile);

                    String s3Key = mediaFile.getFilePath() + mediaFile.getFileName();
                    String response = fileUtil.uploadFile(tempFile, s3Key);

                    if ("File Uploaded".equalsIgnoreCase(response)) {
                        tempFile.delete(); // delete temp file
                        fileOutputWebModelList.add(this.transformData(mediaFile));
                    }
                } catch (IOException e) {
                    logger.error("Error at saveMediaFiles() during S3 upload", e);
                }
            });

            fileOutputWebModelList.sort(Comparator.comparing(FileOutputWebModel::getId));
        } catch (Exception e) {
            logger.error("Error at saveMediaFiles()", e);
        }
        return fileOutputWebModelList;
    }

    private Map<MediaFile, MultipartFile> prepareMultipleMediaFilesData(FileInputWebModel fileInput, User user) {
        Map<MediaFile, MultipartFile> mediaFilesMap = new HashMap<>();

        try {
            if (user == null) {
                logger.error("User is null in prepareMultipleMediaFilesData()");
                throw new IllegalArgumentException("User cannot be null");
            }

            if (!Utility.isNullOrEmptyList(fileInput.getFiles())) {
                for (MultipartFile file : fileInput.getFiles()) {
                    if (file != null && !file.isEmpty()) {
                        MediaFile mediaFile = new MediaFile();
                        mediaFile.setUser(user);
                        mediaFile.setCategory(fileInput.getCategory());
                        mediaFile.setFileName(file.getOriginalFilename());
                        mediaFile.setFileOriginalName(file.getOriginalFilename());
                        mediaFile.setFileType(file.getContentType());
                        mediaFile.setFileSize(String.valueOf(file.getSize()));
                        mediaFile.setFileIsActive(true);
                        mediaFile.setFileIds(UUID.randomUUID().toString());
                        mediaFile.setFileCreatedBy(user.getUserId());
                        mediaFile.setFileCreatedOn(new Date());
                        mediaFile.setFileDomainId(fileInput.getCategory().ordinal());
                        mediaFile.setFileDomainReferenceId(fileInput.getCategoryRefId());

                        // Set a proper file path for S3
                        String filePath = "patient/" + user.getUserId() + "/" + mediaFile.getFileIds() + "/";
                        mediaFile.setFilePath(filePath);

                        // Save to DB and put in map
                        mediaFilesRepository.save(mediaFile);
                        mediaFilesMap.put(mediaFile, file);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred at prepareMultipleMediaFilesData()", e);
        }

        return mediaFilesMap;
    }

    private FileOutputWebModel transformData(MediaFile mediaFile) {
        FileOutputWebModel fileOutputWebModel = new FileOutputWebModel();
        try {
            fileOutputWebModel.setFileId(mediaFile.getFileId());

            if (mediaFile.getUser() != null) {
                fileOutputWebModel.setUserId(mediaFile.getUser().getUserId());
            }

            if (mediaFile.getCategory() != null) {
                fileOutputWebModel.setCategory(mediaFile.getCategory().toString());
            }

            fileOutputWebModel.setCategoryRefId(mediaFile.getFileDomainReferenceId());
            fileOutputWebModel.setFileName(mediaFile.getFileName());
            fileOutputWebModel.setFileType(mediaFile.getFileType());
            fileOutputWebModel.setFileSize(mediaFile.getFileSize());

            String filePath = s3Util.generateS3FilePath(mediaFile.getFilePath() + mediaFile.getFileName());
            fileOutputWebModel.setFilePath(filePath);

            fileOutputWebModel.setCreatedBy(mediaFile.getFileCreatedBy());
            fileOutputWebModel.setCreatedOn(mediaFile.getFileCreatedOn());
            fileOutputWebModel.setUpdatedBy(mediaFile.getFileUpdatedBy());
            fileOutputWebModel.setUpdatedOn(mediaFile.getFileUpdatedOn());

        } catch (Exception e) {
            logger.error("Error at transformData()", e);
        }
        return fileOutputWebModel;
    }
}
