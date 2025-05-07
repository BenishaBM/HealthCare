package com.annular.healthCare.Cron;

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
public class AppointmentNotificationScheduler {
	

	    
	    private static final Logger logger = LoggerFactory.getLogger(AppointmentNotificationScheduler.class);
	    
	    @Autowired
	    private PatientAppoitmentTablerepository patientAppointmentRepository;
	    
	    @Autowired
	    private PatientDetailsRepository patientDetailsRepository;
	    
	    @Autowired
	    private SmsService smsService;
	    
	    // Define time zones
	    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
	    private static final TimeZone IST_TIME_ZONE = TimeZone.getTimeZone("Asia/Kolkata");
	    
	    // Initialize date formatters with IST time zone
	    private final SimpleDateFormat dateFormat = createDateFormat("yyyy-MM-dd", IST_TIME_ZONE);
	    private final SimpleDateFormat timeFormat = createDateFormat("HH:mm", IST_TIME_ZONE);
	    private final SimpleDateFormat dateTimeFormat = createDateFormat("yyyy-MM-dd HH:mm", IST_TIME_ZONE);
	    
	    // Helper method to create and configure date formatters
	    private SimpleDateFormat createDateFormat(String pattern, TimeZone timeZone) {
	        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
	        formatter.setTimeZone(timeZone);
	        return formatter;
	    }
	    
	    /**
	     * Scheduled job that runs every minute to send SMS reminders for appointments 
	     * that will occur 30 minutes from now
	     */
	    @Scheduled(cron = "0 * * * * ?") // This cron expression runs every minute
	    @Transactional
	    public void sendAppointmentReminder() {
	        try {
	            // Get current UTC time
	            Date utcNow = new Date();
	            
	            // Convert UTC to IST (UTC+5:30)
	            Calendar istCalendar = Calendar.getInstance(IST_TIME_ZONE);
	            istCalendar.setTime(utcNow);
	            
	            // Format the IST time
	            String currentDate = dateFormat.format(istCalendar.getTime());
	            String currentTime = timeFormat.format(istCalendar.getTime());
	            
	            logger.info("Running appointment reminder job at IST date: {} time: {}", currentDate, currentTime);
	            
	            // Calculate the target time (30 minutes ahead in IST)
	            istCalendar.add(Calendar.MINUTE, 30);
	            
	            String targetDate = dateFormat.format(istCalendar.getTime());
	            String targetTime = timeFormat.format(istCalendar.getTime());
	            
	            logger.info("Looking for appointments at IST date: {} time: {}", targetDate, targetTime);
	            
	            // Find appointments that match the target date and time
	            List<PatientAppointmentTable> appointments = patientAppointmentRepository
	                    .findUpcomingAppointments(targetDate, targetTime);
	            
	            logger.info("Found {} appointments to send reminders for", appointments.size());
	            
	            // Send SMS for each matching appointment
	            for (PatientAppointmentTable appointment : appointments) {
	                sendSmsNotification(appointment);
	            }
	        } catch (Exception e) {
	            logger.error("Error in appointment reminder job: {}", e.getMessage(), e);
	        }
	    }
	    
	    /**
	     * Sends an SMS notification for an upcoming appointment
	     * @param appointment The appointment to send a reminder for
	     */
	    private void sendSmsNotification(PatientAppointmentTable appointment) {
	        try {
	            // Get patient details
	            Integer patientId = appointment.getPatient().getPatientDetailsId();
	            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(patientId);
	            
	            if (patientDetailsOpt.isPresent()) {
	                PatientDetails patientDetails = patientDetailsOpt.get();
	                String mobileNumber = patientDetails.getMobileNumber();
	                
	                if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
	                    logger.warn("Cannot send reminder: Missing mobile number for patient ID: {}", patientId);
	                    return;
	                }
	                
	                // Get doctor name if available
	                String doctorName = "your doctor";
	                if (appointment.getDoctor() != null && appointment.getDoctor().getUserName() != null) {
	                    doctorName = "Dr. " + appointment.getDoctor().getUserName();
	                }
	                
	                // Format the message
	                StringBuilder smsBuilder = new StringBuilder();
	                smsBuilder.append("Reminder: Your appointment with ");
	                smsBuilder.append(doctorName);
	                smsBuilder.append(" is in 30 minutes today at ");
	                smsBuilder.append(appointment.getSlotStartTime());
	                
	                // Add location if available (assuming this might be stored elsewhere)
	                // smsBuilder.append(" at [Location]");
	                
	                smsBuilder.append(". Please arrive 10 minutes early. Thank you!");
	                String smsMessage = smsBuilder.toString();
	                
	                // Send the SMS and log the result
	                logger.info("Sending reminder SMS to {} for appointment at {}", 
	                        mobileNumber, appointment.getSlotStartTime());
	                
	                try {
	                    smsService.sendSms(mobileNumber, smsMessage);
	                    logger.info("Successfully sent reminder SMS to patient ID: {}", patientId);
	                } catch (Exception e) {
	                    logger.error("Failed to send reminder SMS to patient ID: {}: {}", 
	                            patientId, e.getMessage(), e);
	                }
	            } else {
	                logger.warn("Patient details not found for ID: {}", patientId);
	            }
	        } catch (Exception e) {
	            logger.error("Error sending reminder for appointment ID {}: {}", 
	                    appointment.getAppointmentId(), e.getMessage(), e);
	        }
	    }
}
