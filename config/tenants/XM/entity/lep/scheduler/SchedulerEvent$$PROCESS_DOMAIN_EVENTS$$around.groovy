package XM.entity.lep.scheduler

import com.icthh.xm.commons.domainevent.outbox.domain.Outbox
import com.icthh.xm.commons.domainevent.outbox.domain.RecordStatus
import com.icthh.xm.commons.domainevent.outbox.service.OutboxTransportService
import com.icthh.xm.commons.domainevent.domain.DomainEvent
import com.icthh.xm.ms.entity.service.SeparateTransactionExecutor
import groovy.transform.Field
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.domain.Specification

@Field Logger log = LoggerFactory.getLogger(getClass())
@Field OutboxTransportService outboxTransportService = lepContext.outboxTransportService
@Field def commonsTenantConf = lepContext.services.tenantConfigService.getConfig().commons
@Field int BATCH_SIZE = commonsTenantConf?.commons?.paging?.defaultSize ?: 1000

Slice<DomainEvent> slice = outboxTransportService.findAll(buildSpecification(), PageRequest.of(0, BATCH_SIZE))
processDomainEventsBatch(slice.getContent())

return

private static Specification<Outbox> buildSpecification() {
    return { root, query, criteriaBuilder ->
        criteriaBuilder.or(
                criteriaBuilder.equal(root.get("status"), RecordStatus.NEW),
                criteriaBuilder.equal(root.get("status"), RecordStatus.ERROR))
    }
}

private void processDomainEventsBatch(Collection<DomainEvent> domainEvents) {
    log.info("Found: {} domain events to process", domainEvents.size())
    lepContext.services.separateTransactionExecutor.doInSeparateTransaction((SeparateTransactionExecutor.Task) {
        outboxTransportService.changeStatus(RecordStatus.PROCESSING, domainEvents.collect { it.id })
    })

    List<UUID> errorDomainEventIds = new ArrayList<>()
    domainEvents.each {
        try {
            processDomainEvent(it)
        } catch (Exception e) {
            log.error("Error processing domain event with id: {}", it.id, e)
            errorDomainEventIds.add(it.id)
        }
    }
    outboxTransportService.changeStatus(RecordStatus.ERROR, errorDomainEventIds)
}

private void processDomainEvent(DomainEvent domainEvent) {
    def notificationChannel = commonsTenantConf.notification.defaultChannel
    lepContext.templates.kafka.send(notificationChannel, domainEvent.payload.data)
}
