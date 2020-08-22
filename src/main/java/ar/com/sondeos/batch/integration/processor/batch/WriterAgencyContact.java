package ar.com.sondeos.batch.integration.processor.batch;

import ar.com.sondeos.batch.integration.processor.domain.AgencyContact;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WriterAgencyContact implements ItemWriter<AgencyContact> {

    private static final Logger logger = LoggerFactory.getLogger(WriterAgencyContact.class);

    @Value("${services.api.url}")
    String host;

    @Override
    public void write(List<? extends AgencyContact> agencyContacts) throws Exception {

        logger.info("---> Insertando dni y topic_id en la tabla agency_contact de la BD multichannel");

        //Creating a HttpClient object
        CloseableHttpClient httpclient = HttpClients.createDefault();

        //Creating a HttpPost object
        HttpPost httpPost = new HttpPost(host);

        //Creating a HttpPost object
        for (AgencyContact agencyContact : agencyContacts) {
            int dni = agencyContact.getDni();
            int topicId = agencyContact.getTopicId();
            Long companyId = agencyContact.getCompanyId();

            String dniToString = Integer.toString(dni);
            String topicIdToString = Integer.toString(topicId);
            String companyIdToString = Long.toString(companyId);

            // Create new JSON Object
            JsonObject agencyContactBody = new JsonObject();
            agencyContactBody.addProperty("dni", dniToString);
            agencyContactBody.addProperty("topic_id", topicIdToString);
            agencyContactBody.addProperty("company_id", companyIdToString);

            logger.info("--->" + agencyContactBody.toString());

            String json = agencyContactBody.toString();
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            //Printing the method used
            logger.info("---> Solicitud: " + httpPost.getMethod());

            //Printing the url method used
            logger.info("---> Consumiendo EndPoint de la API: " + httpPost.getURI());

            //Executing the Post request
            HttpResponse response = httpclient.execute(httpPost);
            logger.info("---> Respuesta (se insertó satisfactoriamente): " + response.getStatusLine().getStatusCode());
        }

        httpclient.close();

    }
}
