package ar.com.sondeos.batch.integration.processor.domain;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class AgencyContactFieldSetMapper implements FieldSetMapper<AgencyContact> {

    public AgencyContact mapFieldSet(FieldSet fieldSet) {
        AgencyContact agencyContact = new AgencyContact();

        agencyContact.setDni(fieldSet.readInt(3));
        agencyContact.setAgencyName(fieldSet.readString(6));

        return agencyContact;
    }
}
