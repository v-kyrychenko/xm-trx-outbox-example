package XM.entity.lep.commons

import groovy.util.logging.Slf4j
import com.icthh.xm.commons.domainevent.service.EventPublisher
import com.icthh.xm.commons.domainevent.service.builder.DomainEventFactory
import com.icthh.xm.commons.domainevent.domain.DomainEventPayload
import com.icthh.xm.commons.domainevent.outbox.domain.RecordStatus
import com.icthh.xm.commons.domainevent.outbox.service.OutboxTransportService

@Slf4j
class DomainEventsService {

    EventPublisher eventPublisher
    DomainEventFactory domainEventFactory
    OutboxTransportService outboxTransportService

    DomainEventsService(def lepContext) {
        this.outboxTransportService = lepContext.outboxTransportService
        this.eventPublisher = lepContext.services.eventPublisher
        this.domainEventFactory = lepContext.services.domainEventFactory
    }

    void publishEvent(def input) {
        def event = domainEventFactory
                .withTransaction()          // optional parameter that enrich domain event with DB transaction id
                .build(CREATE,              // operation type
                        input.entityId,     //aggregation id - id of external system
                        input.typeKey,      //aggregation type - type of the external entity
                        new DomainEventPayload(input.payload)) //raw event payload

        eventPublisher.publish(LEP.name(), event)
    }

    void setEventProcessed(String eventId) {
        outboxTransportService.changeStatusById(RecordStatus.COMPLETE, UUID.fromString(eventId))
    }

}
