package com.annular.healthCare.service.serviceImpl;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.MedicalTestConfig;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Integer>{

	Optional<Department> findByName(String department);

}
