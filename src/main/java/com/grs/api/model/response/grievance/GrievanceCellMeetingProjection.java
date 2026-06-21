package com.grs.api.model.response.grievance;

import java.util.Date;

public interface GrievanceCellMeetingProjection {
    Long getId();
    String getTrackingNumber();
    Date getCreatedAt();
    String getSubject();
    String getGrievanceCurrentStatus();
}
