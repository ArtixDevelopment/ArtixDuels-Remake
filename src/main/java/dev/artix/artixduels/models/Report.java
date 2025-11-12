package dev.artix.artixduels.models;

import java.util.UUID;

/**
 * Representa um relatório de jogador.
 */
public class Report {
    private String id;
    private UUID reporterId;
    private String reporterName;
    private UUID reportedId;
    private String reportedName;
    private ReportType type;
    private String reason;
    private String description;
    private long timestamp;
    private ReportStatus status;
    private String reviewerId;
    private String reviewNotes;

    public enum ReportType {
        CHEATING("Trapaça"),
        BEHAVIOR("Comportamento"),
        SPAM("Spam"),
        OTHER("Outro");

        private String displayName;

        ReportType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReportStatus {
        PENDING("Pendente"),
        REVIEWING("Em Revisão"),
        ACCEPTED("Aceito"),
        REJECTED("Rejeitado"),
        RESOLVED("Resolvido");

        private String displayName;

        ReportStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Report(String id, UUID reporterId, String reporterName, UUID reportedId, String reportedName, 
                  ReportType type, String reason) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reportedId = reportedId;
        this.reportedName = reportedName;
        this.type = type;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.status = ReportStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public UUID getReportedId() {
        return reportedId;
    }

    public String getReportedName() {
        return reportedName;
    }

    public ReportType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}

