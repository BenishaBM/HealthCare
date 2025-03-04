package com.annular.healthCare.webModel;

import java.util.Date;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileOutputWebModel {
	private Integer id; // MySQL Primary key field

	private String fileId;
	private String fileName;
	private long fileSize;
	private String fileType;
	private String filePath;
	private String description;
	private String type;

	private Integer userId;
	private String category;
	private Integer categoryRefId;

	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;

}