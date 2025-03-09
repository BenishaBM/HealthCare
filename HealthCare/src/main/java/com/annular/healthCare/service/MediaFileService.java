package com.annular.healthCare.service;

import java.util.List;

import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.User;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.FileOutputWebModel;

public interface MediaFileService {
	
	List<FileOutputWebModel> saveMediaFiles(FileInputWebModel fileInputWebModel, User user);

	List<FileOutputWebModel> getMediaFilesByCategoryAndRefId(MediaFileCategory yoga, Integer id);

	boolean deleteMediaFilesByUserIdAndCategoryAndRefIds(MediaFileCategory patientdocument, Integer fileId);

}
