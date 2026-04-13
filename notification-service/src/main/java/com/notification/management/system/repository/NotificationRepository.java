package  com.notification.management.system.repository;


import com.notification.management.system.model.Notification;
import com.notification.management.system.model.NotificationStatus;
import com.notification.management.system.model.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByPatientIdAndRecipientType(Long patientId, RecipientType type);
    List<Notification> findByDoctorIdAndRecipientType(Long doctorId, RecipientType type);

    List<Notification> findByRecipientType(RecipientType type);
    List<Notification> findByNotificationStatus(NotificationStatus status);
}
