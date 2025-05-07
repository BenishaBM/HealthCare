package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.SupportStaffMasterData;

@Repository
public interface SupportStaffMasterDataRepository extends JpaRepository<SupportStaffMasterData,Integer> {

	List<SupportStaffMasterData> findByIsActiveTrue();

}
