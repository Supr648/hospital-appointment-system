package com.hospital.appointmentsystem.config;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DataLoader(DoctorRepository doctorRepository,
                      PatientRepository patientRepository,
                      AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            return;
        }

        Doctor d1 = createDoctor("Dr. Ananya Sharma", "Cardiology", "ananya.sharma@medicare.com",
                "9876543210", "Cardiology", 12, "MD, DM Cardiology", true);
        Doctor d2 = createDoctor("Dr. Rohan Mehta", "Orthopedics", "rohan.mehta@medicare.com",
                "9876543211", "Orthopedics", 8, "MS Orthopedics", true);
        Doctor d3 = createDoctor("Dr. Priya Nair", "Pediatrics", "priya.nair@medicare.com",
                "9876543212", "Pediatrics", 10, "MD Pediatrics", true);
        Doctor d4 = createDoctor("Dr. Vikram Singh", "Dermatology", "vikram.singh@medicare.com",
                "9876543213", "Dermatology", 6, "MD Dermatology", false);
        Doctor d5 = createDoctor("Dr. Sneha Patel", "General Medicine", "sneha.patel@medicare.com",
                "9876543214", "General Medicine", 15, "MBBS, MD", true);

        doctorRepository.save(d1);
        doctorRepository.save(d2);
        doctorRepository.save(d3);
        doctorRepository.save(d4);
        doctorRepository.save(d5);

        Patient p1 = createPatient("Aarav Kumar", "aarav.kumar@email.com", "9123456780", 28, "Male", "Mumbai, MH", "B+");
        Patient p2 = createPatient("Ishita Reddy", "ishita.reddy@email.com", "9123456781", 34, "Female", "Hyderabad, TS", "O+");
        Patient p3 = createPatient("Kabir Joshi", "kabir.joshi@email.com", "9123456782", 45, "Male", "Pune, MH", "A+");
        Patient p4 = createPatient("Meera Iyer", "meera.iyer@email.com", "9123456783", 22, "Female", "Chennai, TN", "AB+");
        Patient p5 = createPatient("Arjun Das", "arjun.das@email.com", "9123456784", 51, "Male", "Kolkata, WB", "O-");

        patientRepository.save(p1);
        patientRepository.save(p2);
        patientRepository.save(p3);
        patientRepository.save(p4);
        patientRepository.save(p5);

        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        Appointment a1 = createAppointment(p1, d1, now.plusDays(1).withHour(10), "SCHEDULED", "Chest pain follow-up", null);
        Appointment a2 = createAppointment(p2, d3, now.plusDays(1).withHour(11), "SCHEDULED", "Child vaccination", null);
        Appointment a3 = createAppointment(p3, d2, now.plusDays(2).withHour(15), "SCHEDULED", "Knee pain", "MRI recommended");
        Appointment a4 = createAppointment(p4, d5, now.minusDays(2).withHour(9), "COMPLETED", "Fever and cough", "Prescribed antibiotics");
        Appointment a5 = createAppointment(p5, d1, now.minusDays(5).withHour(14), "CANCELLED", "ECG review", "Patient requested cancel");

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);
        appointmentRepository.save(a3);
        appointmentRepository.save(a4);
        appointmentRepository.save(a5);
    }

    private Doctor createDoctor(String name, String specialization, String email, String phone,
                                String department, int exp, String qualification, boolean available) {
        Doctor d = new Doctor();
        d.setName(name);
        d.setSpecialization(specialization);
        d.setEmail(email);
        d.setPhone(phone);
        d.setDepartment(department);
        d.setExperienceYears(exp);
        d.setQualification(qualification);
        d.setAvailable(available);
        return d;
    }

    private Patient createPatient(String name, String email, String phone, int age,
                                  String gender, String address, String bloodGroup) {
        Patient p = new Patient();
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        p.setAge(age);
        p.setGender(gender);
        p.setAddress(address);
        p.setBloodGroup(bloodGroup);
        return p;
    }

    private Appointment createAppointment(Patient patient, Doctor doctor, LocalDateTime time,
                                          String status, String reason, String notes) {
        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setAppointmentTime(time);
        a.setStatus(status);
        a.setReason(reason);
        a.setNotes(notes);
        a.setCreatedAt(LocalDateTime.now().minusDays(1));
        return a;
    }
}
