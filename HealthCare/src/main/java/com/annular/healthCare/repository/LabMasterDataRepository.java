package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.LabMasterData;

@Repository
public interface LabMasterDataRepository extends JpaRepository<LabMasterData,Integer>{

	List<LabMasterData> findByIsActiveTrue();

}
