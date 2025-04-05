package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlotTimeOverride;

@Repository
public interface DoctorSlotTimeOverrideRepository extends JpaRepository<DoctorSlotTimeOverride, Integer>{

	List<DoctorSlotTimeOverride> findByOriginalSlotDoctorSlotTimeIdAndOverrideDate(Integer doctorSlotTimeId,
			Date overrideDate);

	Optional<DoctorSlotTimeOverride> findByOriginalSlot_DoctorSlotTimeIdAndOverrideDateAndIsActive(
			Integer doctorSlotTimeId, Date overrideCheckDate, boolean b);



}
