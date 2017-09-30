package net.mksat.gan.keysmanager.service;

/**
 * Created by 2 on 12.07.2014.
 */
public class AuditoryButton {

    private String id;
    private String campus;
    private String auditoryName;
    private String status;
    private String securityAlarm;

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuditoryName() {
        return auditoryName;
    }

    public void setAuditoryName(String auditoryName) {
        this.auditoryName = auditoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecurityAlarm() {
        return securityAlarm;
    }

    public void setSecurityAlarm(String securityAlarm) {
        this.securityAlarm = securityAlarm;
    }
}
