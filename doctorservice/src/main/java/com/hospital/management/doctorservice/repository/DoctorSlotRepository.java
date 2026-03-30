package com.hospital.management.doctorservice.repository;
import com.hospital.management.doctorservice.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    // Gets all slots for a specific doctor on a specific day
    List<DoctorSlot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);

    // Used for the "Wipe" logic to remove old slots
    void deleteBySlotDateBefore(LocalDate date);
}