package com.annular.healthCare.webModel;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.annular.healthCare.model.MediaFileCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInputWebModel {
	
	// For save purpose
		private Integer userId;
		private MediaFileCategory category;
		private Integer categoryRefId;
		private List<MultipartFile> files;
		private String description;

		// For read purpose
		private String fileIds;
		private String fileType;
		private String filePath;
		private String type;
		
		private Integer fileId;
		
		private String fileName;

		private String fileSize;


		private String fileData;
		
		private Integer fileCategory;

}
