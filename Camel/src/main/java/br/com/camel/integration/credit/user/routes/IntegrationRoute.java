package br.com.camel.integration.credit.user.routes;

import br.com.camel.integration.credit.user.enums.StatusMessage;
import br.com.camel.integration.credit.user.model.Credit;
import br.com.camel.integration.credit.user.model.CreditUser;
import br.com.camel.integration.credit.user.processors.ChangeStatus;
import br.com.camel.integration.credit.user.processors.FailExecution;
import br.com.camel.integration.credit.user.processors.RetryExecution;
import br.com.camel.integration.credit.user.aggregation.IntegrationAggregationStrategy;
import com.mongodb.DBObject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Victor Tripeno
 * This route is responsible to receive two messages with the same Correlation Id and make the aggregation
 */
@Component
public class IntegrationRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);
        xmlJsonFormat.setRootName("CreditUser");
        xmlJsonFormat.setSkipNamespaces(true);
        xmlJsonFormat.setRemoveNamespacePrefixes(true);
        xmlJsonFormat.setExpandableProperties(Arrays.asList("d", "e"));

        /**
         * Dead Letter Channel, it will try delivery the message three times each 60 seconds
         */
        errorHandler(
                deadLetterChannel("rabbitmq:{{RABBITMQ_ADDRESS}}/tasks?username={{RABBITMQ_USERNAME}}&password={{RABBITMQ_PSWD}}&autoDelete=false&routingKey=camel&queue={{RABBITMQ_QUEUE_DLQ}}&bridgeEndpoint=true")
                        .logExhaustedMessageHistory(true)
                        .maximumRedeliveries(3)
                        .redeliveryDelay(10000)
                        .onPrepareFailure(new FailExecution())
                        .onRedelivery(new RetryExecution())
        );


        from("seda:integration").id("integrationRoute")
            .to("log:pre_aggregate")
            .aggregate(header("correlationId"), new IntegrationAggregationStrategy()).eagerCheckCompletion().completionSize(2)
            .marshal().json(JsonLibrary.Jackson)
            .to("direct:out-queue");

        from("direct:out-queue").id("outQueue")
            .to("log:out_queue")
//            .convertBodyTo(DBObject.class)
//            .to("mongodb:myDb?database={{DATABASE}}&collection={{COLLECTION}}&operation=save")
            .to("log:status-in-progress")
            /* TODO: CHANGE THE STATUS MESSAGE TO 'FINISHED' and transform the Json to XML to send to the queue */
            .unmarshal().json(JsonLibrary.Jackson, CreditUser.class)
            .to("log:unmarshal-json")
            .process(new ChangeStatus(StatusMessage.FINISHED))
            .marshal().json(JsonLibrary.Jackson)
            .unmarshal(xmlJsonFormat)
            .to("rabbitmq:{{RABBITMQ_ADDRESS}}/tasks?username={{RABBITMQ_USERNAME}}&password={{RABBITMQ_PSWD}}&autoDelete=false&routingKey=camel&queue={{RABBITMQ_QUEUE_OUT}}&bridgeEndpoint=true")
            .to("log:foo").end();
            /* TODO: Transform the XML in JSON to save in MongoDB */
//            .convertBodyTo(DBObject.class)
//            .to("mongodb:myDb?database={{DATABASE}}&collection={{COLLECTION}}&operation=save");
    }

}
