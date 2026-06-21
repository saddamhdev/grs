package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceCellMeetingDTO {
    private Long id;
    private String trackingNumberBangla;
    private Date createdAt;
    private String subject;
    private String statusBangla;
    private String dateBangla;
    private String expectedDateOfClosingBangla;

    public GrievanceCellMeetingDTO(Long id, String trackingNumberBangla, Date createdAt, String subject, String statusBangla) {
        this.id = id;
        this.trackingNumberBangla = trackingNumberBangla;
        this.createdAt = createdAt;
        this.subject = subject;
        this.statusBangla = statusBangla;
    }
}
