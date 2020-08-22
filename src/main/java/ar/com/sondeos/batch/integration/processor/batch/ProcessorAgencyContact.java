package ar.com.sondeos.batch.integration.processor.batch;

import ar.com.sondeos.batch.integration.processor.domain.AgencyContact;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessorAgencyContact implements ItemProcessor<AgencyContact, AgencyContact> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorAgencyContact.class);

    @Value("${services.topics.url}")
    String host;

    @Value("${company.id}")
    Long companyId;

    @Override
    public AgencyContact process(AgencyContact agencyContact) throws Exception {

        logger.info("---> Procesando archivo");

        //Creating a HttpClient object
        CloseableHttpClient httpclient = HttpClients.createDefault();

        //Creating a HttpGet object
        String agencyName = agencyContact.getAgencyName();
        HttpGet httpget = new HttpGet(host + agencyName + "/" + companyId + "/topic_id");

        //Printing the method used
        logger.info("---> Solicitud: " + httpget.getMethod());

        //Printing the url method used
        logger.info("---> Consumiendo EndPoint del TopicService (devuelve el id del Topic): " + httpget.getURI());

        //Executing the Get request
        HttpResponse httpResponse = httpclient.execute(httpget);

        //Now pull back the response object
        HttpEntity httpEntity = httpResponse.getEntity();
        String id = EntityUtils.toString(httpEntity);

        int topicId = Integer.parseInt(id);
        logger.info("---> Respuesta (topicId): " + topicId);

        int dni = agencyContact.getDni();

        AgencyContact transformedAgencyContact = new AgencyContact(dni, topicId, companyId);

        logger.info("---> Creando AgencyContact :" + transformedAgencyContact.toString());

        return transformedAgencyContact;
    }
}
