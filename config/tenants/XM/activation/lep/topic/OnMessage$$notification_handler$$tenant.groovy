package XM.activation.lep.topic

import com.fasterxml.jackson.databind.ObjectMapper
import com.icthh.xm.tmf.ms.activation.domain.SagaTransaction
import com.icthh.xm.tmf.ms.activation.service.SagaService
import XM.activation.lep.commons.MsEntityService
import groovy.transform.Field
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Field Logger log = LoggerFactory.getLogger(getClass())
@Field SagaService sagaService = lepContext.services.sagaService
@Field MsEntityService msEntityService = new MsEntityService()

StopWatch start = StopWatch.createStarted()
String json = lepContext.inArgs.topicMessage

Map notification = new ObjectMapper().readValue(json, Map.class)

log.info('### NOTIFICATION-HANDLER:start: {}', notification.id)

try {
    msEntityService.setEventProcessed(notification.id)
    sagaService.createNewSaga(buildTransaction("EVENT-PROCESSING-SAGA", notification))
} catch (Exception ex) {
    log.warn("Error during notification handling:{}", notification, ex)
}

log.info('### NOTIFICATION-HANDLER:stop: time = {} ms', start.getTime())
return [:]

private static SagaTransaction buildTransaction(String typeKey, Map context) {
    return new SagaTransaction(
            typeKey: typeKey,
            context: context
    )
}

