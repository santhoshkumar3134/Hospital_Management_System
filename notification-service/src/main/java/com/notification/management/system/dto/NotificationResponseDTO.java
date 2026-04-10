package  com.notification.management.system.dto;


import com.notification.management.system.model.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private Long notificationId;
    private Long patientId;
    private Long doctorId;
    private String message;
    private NotificationStatus status;
}