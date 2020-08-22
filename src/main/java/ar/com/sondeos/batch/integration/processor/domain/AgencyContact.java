package ar.com.sondeos.batch.integration.processor.domain;

import java.io.Serializable;

public class AgencyContact implements Serializable {

    int dni;
    String agencyName;
    int topicId;
    Long companyId;

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public AgencyContact(int dni, int topicId, Long companyId) {
        this.dni = dni;
        this.topicId = topicId;
        this.companyId = companyId;
    }

    public AgencyContact() {
    }

    @Override
    public String toString() {
        return "dni=" + dni + ",topicId=" + topicId + ",companyId=" + companyId;
    }
}
