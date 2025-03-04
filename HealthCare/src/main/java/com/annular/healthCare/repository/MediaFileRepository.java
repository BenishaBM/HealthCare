package com.annular.healthCare.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MediaFile;


@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Integer>{

	Optional<MediaFile> findByFileId(Integer fileId);
    // Query to find media files based on fileDomainId and fileDomainReferenceId
    @Query("SELECT mf FROM MediaFile mf WHERE mf.fileDomainId = :fileDomainId AND mf.fileDomainReferenceId = :fileDomainReferenceId")
    List<MediaFile> findByFileDomainIdAndFileDomainReferenceId(int fileDomainId, int fileDomainReferenceId);
	
    @Query("SELECT mf FROM MediaFile mf WHERE  mf.fileDomainReferenceId = :hospitalDataId")
    List<MediaFile> findByFileDomainReferenceId(Integer hospitalDataId);

}
