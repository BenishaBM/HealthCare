package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.AddressData;

@Repository
public interface AddressDataRepository extends JpaRepository<AddressData,Integer>{

	List<AddressData> findByUserIsActiveTrue();

}
