package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.ContactUsData;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUsData,Integer>{

}
