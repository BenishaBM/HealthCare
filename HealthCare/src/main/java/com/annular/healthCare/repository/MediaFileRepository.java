package com.annular.healthCare.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;


@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Integer>{

	Optional<MediaFile> findByFileId(Integer fileId);
    // Query to find media files based on fileDomainId and fileDomainReferenceId
    @Query("SELECT mf FROM MediaFile mf WHERE mf.fileDomainId = :fileDomainId AND mf.fileDomainReferenceId = :fileDomainReferenceId")
    List<MediaFile> findByFileDomainIdAndFileDomainReferenceId(int fileDomainId, int fileDomainReferenceId);
	
    @Query("SELECT mf FROM MediaFile mf WHERE  mf.fileDomainReferenceId = :hospitalDataId")
    List<MediaFile> findByFileDomainReferenceId(Integer hospitalDataId);
    
    @Query("SELECT mf FROM MediaFile mf WHERE mf.fileDomainId = :profilephoto AND mf.fileDomainReferenceId = :userId")
	List<MediaFile> findByUserId(int profilephoto, Integer userId);
    
    @Query("Select m from MediaFile m where m.category=:category and m.categoryRefId=:refId and m.fileIsActive=true")
	List<MediaFile> getMediaFilesByCategoryAndRefId(MediaFileCategory category, Integer refId);
    
    
	//List<MediaFile> findByFileDomainReferenceIdAndCategory(Integer appointmentId, MediaFileCategory resutdocument);
	List<MediaFile> findByFileDomainReferenceIdAndCategoryAndFileIsActiveTrue(Integer appointmentId,
			MediaFileCategory resutdocument);

}
