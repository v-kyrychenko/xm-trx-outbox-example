package XM.entity.lep.scheduler

import com.icthh.xm.commons.domainevent.outbox.domain.Outbox
import com.icthh.xm.commons.domainevent.outbox.domain.RecordStatus
import com.icthh.xm.commons.domainevent.outbox.service.OutboxTransportService
import com.icthh.xm.commons.domainevent.domain.DomainEvent
import groovy.transform.Field
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification

import java.time.ZonedDateTime

@Field Logger log = LoggerFactory.getLogger(getClass())

long errorEvents = countErrorEvents(errorSpec())
long recentActiveEvents = countErrorEvents(recentActiveSpec())
def msg = "Found domain events in error state: $errorEvents, not processed events: $recentActiveEvents" as String
log.info(msg)

if (errorEvents > 0 || recentActiveEvents > 0) {
    //error notification logic
}

return

long countErrorEvents(Specification<Outbox> spec) {
    OutboxTransportService outboxTransportService = lepContext.outboxTransportService
    Pageable tinyPage = PageRequest.of(0, 1)
    Page<DomainEvent> page = outboxTransportService.findAll(spec, tinyPage)
    return page.getTotalElements()
}

private static Specification<Outbox> errorSpec() {
    return (root, query, cb) ->
            cb.equal(root.get("status"), RecordStatus.ERROR)
}

private static Specification<Outbox> recentActiveSpec() {
    return (root, query, cb) -> {
        var oneHourAgo = ZonedDateTime.now().minusHours(1)

        return cb.and(
                root.get("status").in(RecordStatus.NEW, RecordStatus.PROCESSING),
                cb.lessThan(root.get("eventDate"), oneHourAgo)
        )
    }
}
