package ar.com.sondeos.batch.integration.processor.batch;

import ar.com.sondeos.batch.integration.processor.domain.AgencyContact;
import ar.com.sondeos.batch.integration.processor.domain.AgencyContactFieldSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReaderAgencyContact {

    private static Logger logger = LoggerFactory.getLogger(ReaderAgencyContact.class);

    @Bean
    @StepScope
    public FlatFileItemReader<AgencyContact> reader(@Value("file:///#{jobParameters['input.file.name']}") Resource resource) {

        logger.info("---> Leyendo archivo");

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(";");
        lineTokenizer.setStrict(false);

        DefaultLineMapper<AgencyContact> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(new AgencyContactFieldSetMapper());

        FlatFileItemReader<AgencyContact> reader = new FlatFileItemReader<>();
        reader.setResource(resource);
        reader.setLineMapper(lineMapper);

        return reader;
    }
}
