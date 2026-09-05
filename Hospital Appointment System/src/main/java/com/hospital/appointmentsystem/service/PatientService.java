package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public long count() {
        return patientRepository.count();
    }

    public List<Patient> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return patientRepository.search(query.trim());
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public boolean emailExists(String email, Long excludeId) {
        return patientRepository.findByEmailIgnoreCase(email)
                .filter(p -> excludeId == null || !p.getId().equals(excludeId))
                .isPresent();
    }

    public void deleteById(Long id) {
        patientRepository.deleteById(id);
    }

    public Patient update(Long id, Patient updated) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        existing.setBloodGroup(updated.getBloodGroup());
        return patientRepository.save(existing);
    }
}
