package com.annular.healthCare.model;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MediaFiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaFile {

	@Column
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer fileId;

	@Column
	private String fileName;

	@Column
	private String fileOriginalName;

	@Column
	private Integer fileDomainId;

	@Column
	private Integer fileDomainReferenceId;

	@Column
	private Boolean fileIsActive;

	@Column
	private Integer fileCreatedBy;

	@Column
	@CreationTimestamp
	private Date fileCreatedOn;

	@Column
	private Integer fileUpdatedBy;

	@Column
	@CreationTimestamp
	private Date fileUpdatedOn;

	@Column
	private String fileSize;

	@Column
	private String fileType;


}