package gov.cmr.minfi.db.gbe.app.notification;

import gov.cmr.minfi.db.gbe.app.notification.dto.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
