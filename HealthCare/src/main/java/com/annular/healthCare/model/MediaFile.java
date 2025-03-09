package com.annular.healthCare.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "MediaFiles",
    uniqueConstraints = @UniqueConstraint(
        name = "UNIQUE_FILE_ID_CATEGORY_REF_ID_STATUS",
        columnNames = {"file_ids", "category", "category_ref_id", "file_is_active"}
    )
)
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer fileId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_original_name")
    private String fileOriginalName;

    @Column(name = "file_domain_id")
    private Integer fileDomainId;

    @Column(name = "file_domain_reference_id")
    private Integer fileDomainReferenceId;

    @Column(name = "file_is_active")
    private Boolean fileIsActive;

    @Column(name = "file_created_by")
    private Integer fileCreatedBy;

    @CreationTimestamp
    @Column(name = "file_created_on")
    private Date fileCreatedOn;

    @Column(name = "file_updated_by")
    private Integer fileUpdatedBy;

    @Column(name = "category_ref_id")
    private Integer categoryRefId;

    @CreationTimestamp
    @Column(name = "file_updated_on")
    private Date fileUpdatedOn;

    @Column(name = "file_size")
    private String fileSize;

    @Column(name = "file_type")
    private String fileType;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private MediaFileCategory category;

    @Column(name = "file_ids")
    private String fileIds;

    @Column(name = "file_path")
    private String filePath;
}
