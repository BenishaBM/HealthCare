package com.annular.healthCare.Cron;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.service.SmsService;



@Component
public class AppointmentNotificationScheduler {
	
    @Autowired
    private PatientAppoitmentTablerepository patientAppointmentRepository;

    @Autowired
    private PatientDetailsRepository patientDetailsRepository;

    @Autowired
    private SmsService smsService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
    @Scheduled(cron = "0 * * * * ?") // This cron expression runs every minute
    @Transactional
    public void sendAppointmentReminder() {
        Date now = new Date();
        String currentTime = dateFormat.format(now);
        String targetTime = calculateTargetTime(currentTime); // Current time + 30 minutes
        
        // Find appointments that are 30 minutes ahead of the current time
        List<PatientAppointmentTable> appointments = patientAppointmentRepository
                .findAppointmentsByTimeSlot(targetTime);

        for (PatientAppointmentTable appointment : appointments) {
            sendSmsNotification(appointment);
        }
    }


    private String calculateTargetTime(String currentTime) {
		// TODO Auto-generated method stub
    	return currentTime;
	}


	private void sendSmsNotification(PatientAppointmentTable appointment) {
        Integer patientId = appointment.getPatient().getPatientDetailsId();
        Optional<PatientDetails> patientDetails = patientDetailsRepository.findById(patientId);

        if (patientDetails.isPresent()) {
            String mobileNumber = patientDetails.get().getMobileNumber();
            String appointmentDate = appointment.getAppointmentDate();
            String startTime = appointment.getSlotStartTime();
            String smsMessage = "Reminder: Your appointment is in 30 minutes on " + appointmentDate + " at " + startTime;
            
            // Send SMS using your smsService
            smsService.sendSms(mobileNumber, smsMessage);
        }
    }
	

}
