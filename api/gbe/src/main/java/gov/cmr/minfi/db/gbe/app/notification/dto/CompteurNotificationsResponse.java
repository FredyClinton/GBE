package gov.cmr.minfi.db.gbe.app.notification.dto;

/** Nombre de notifications non lues, interrogé régulièrement par le frontend. */
public record CompteurNotificationsResponse(long nonLues) {}
