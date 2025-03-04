package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MediaFile;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Integer>{

}
