package com.hospital.management.doctorservice.repository;
import com.hospital.management.doctorservice.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    // Finds if a specific doctor is available on a specific date
    Optional<DoctorAvailability> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    // Used for the "Wipe" logic to remove old availability records
    void deleteByDateBefore(LocalDate date);


}