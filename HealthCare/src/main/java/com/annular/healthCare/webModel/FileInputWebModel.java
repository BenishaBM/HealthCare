package com.annular.healthCare.webModel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInputWebModel {
	
	private Integer fileId;
	
	private String fileName;

	private String fileSize;

	private String fileType;

	private String fileData;
	
	private Integer fileCategory;

}
