package com.annular.healthCare.Cron;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FollowUpCron {
	
    private static final Logger logger = LoggerFactory.getLogger(FollowUpCron.class);
    
    @Autowired
    private PatientAppoitmentTablerepository patientAppointmentRepository;
    
    @Autowired
    private PatientDetailsRepository patientDetailsRepository;
    
    @Autowired
    private SmsService smsService;
    
    // Define time zones
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    private static final TimeZone IST_TIME_ZONE = TimeZone.getTimeZone("Asia/Kolkata");
    
    // Initialize date formatter with IST time zone
    private final SimpleDateFormat dateFormat = createDateFormat("yyyy-MM-dd", IST_TIME_ZONE);
    
    // Helper method to create and configure date formatters
    private SimpleDateFormat createDateFormat(String pattern, TimeZone timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(timeZone);
        return formatter;
    }
    
    /**
     * Scheduled job that runs every day at 8:00 AM IST to send follow-up reminders
     * Uses cron expression "0 0 8 * * ?" which means:
     * - 0 seconds
     * - 0 minutes 
     * - 8 hours (8 AM)
     * - Every day of month
     * - Every month
     * - Every day of week
     * 
     * Note: Server time is assumed to be in UTC, so we adjust the cron expression
     * to run at 2:30 AM UTC (which is 8:00 AM IST, since IST is UTC+5:30)
     */
    @Scheduled(cron = "0 30 2 * * ?") // Runs at 2:30 AM UTC (8:00 AM IST)
    @Transactional
    public void sendFollowUpReminders() {
        try {
            logger.info("Starting follow-up reminder job");
            
            // Get current date in IST
            Calendar calendar = Calendar.getInstance(IST_TIME_ZONE);
            String todayDate = dateFormat.format(calendar.getTime());
            
            logger.info("Checking for follow-ups scheduled for today: {}", todayDate);
            
            // Find appointments with follow-up date matching today
            List<PatientAppointmentTable> followUpAppointments = 
                    patientAppointmentRepository.findByFollowUpDate(todayDate);
            
            logger.info("Found {} patients with follow-up appointments today", followUpAppointments.size());
            
            // Send reminder for each follow-up
            for (PatientAppointmentTable appointment : followUpAppointments) {
                sendFollowUpReminderSms(appointment);
            }
            
            logger.info("Follow-up reminder job completed");
        } catch (Exception e) {
            logger.error("Error in follow-up reminder job: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Sends a follow-up reminder SMS to the patient
     * @param appointment The appointment with follow-up date
     */
    private void sendFollowUpReminderSms(PatientAppointmentTable appointment) {
        try {
            // Skip if no patient associated with appointment
            if (appointment.getPatient() == null) {
                logger.warn("No patient found for appointment ID: {}", appointment.getAppointmentId());
                return;
            }
            
            Integer patientId = appointment.getPatient().getPatientDetailsId();
            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(patientId);
            
            if (patientDetailsOpt.isPresent()) {
                PatientDetails patientDetails = patientDetailsOpt.get();
                String mobileNumber = patientDetails.getMobileNumber();
                String patientName = patientDetails.getPatientName(); // Assuming this field exists
                
                // Skip if no mobile number
                if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
                    logger.warn("Missing mobile number for patient ID: {}", patientId);
                    return;
                }
                
                // Get doctor name if available
                String doctorName = "your doctor";
                if (appointment.getDoctor() != null && appointment.getDoctor().getUserName() != null) {
                    doctorName = "Dr. " + appointment.getDoctor().getUserName();
                }
                
                // Build the message
                StringBuilder smsBuilder = new StringBuilder();
                smsBuilder.append("Hello ");
                
                // Use patient name if available, otherwise generic greeting
                if (patientName != null && !patientName.trim().isEmpty()) {
                    smsBuilder.append(patientName);
                } else {
                    smsBuilder.append("there");
                }
                
                smsBuilder.append(", hope you are doing better. ");
                smsBuilder.append("You have a follow-up appointment to be scheduled with ");
                smsBuilder.append(doctorName);
                smsBuilder.append(" today (");
                smsBuilder.append(appointment.getFollowUpDate());
                smsBuilder.append("). ");
                smsBuilder.append("Please contact our clinic to book your preferred time slot. ");
                smsBuilder.append("Thank you for choosing our healthcare services.");
                
                String smsMessage = smsBuilder.toString();
                
                // Send SMS
                logger.info("Sending follow-up reminder to patient ID: {}", patientId);
                
                try {
                    smsService.sendSms(mobileNumber, smsMessage);
                    logger.info("Successfully sent follow-up reminder to patient ID: {}", patientId);
                } catch (Exception e) {
                    logger.error("Failed to send follow-up reminder to patient ID: {}: {}", 
                            patientId, e.getMessage());
                }
            } else {
                logger.warn("Patient details not found for ID: {}", patientId);
            }
        } catch (Exception e) {
            logger.error("Error sending follow-up reminder for appointment ID {}: {}", 
                    appointment.getAppointmentId(), e.getMessage(), e);
        }
    }

}
