package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public long count() {
        return doctorRepository.count();
    }

    public List<Doctor> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return doctorRepository.search(query.trim());
    }

    public List<Doctor> findAvailable() {
        return doctorRepository.findByAvailableTrue();
    }

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public boolean emailExists(String email, Long excludeId) {
        return doctorRepository.findByEmailIgnoreCase(email)
                .filter(d -> excludeId == null || !d.getId().equals(excludeId))
                .isPresent();
    }

    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }

    public Doctor update(Long id, Doctor updated) {
        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
        existing.setName(updated.getName());
        existing.setSpecialization(updated.getSpecialization());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setDepartment(updated.getDepartment());
        existing.setExperienceYears(updated.getExperienceYears());
        existing.setQualification(updated.getQualification());
        existing.setAvailable(updated.getAvailable() != null ? updated.getAvailable() : true);
        return doctorRepository.save(existing);
    }

    public void toggleAvailability(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
        doctor.setAvailable(!Boolean.TRUE.equals(doctor.getAvailable()));
        doctorRepository.save(doctor);
    }
}
