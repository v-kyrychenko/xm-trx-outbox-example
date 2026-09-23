package ROOT0CORE.entity.lep.domainevent.db.filter

import com.icthh.xm.commons.domainevent.db.domain.JpaEntityContext
import com.icthh.xm.commons.domainevent.domain.enums.DefaultDomainEventOperation
import com.icthh.xm.commons.domainevent.outbox.domain.RecordStatus
import groovy.transform.Field
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Field Logger log = LoggerFactory.getLogger(getClass())
String tableName = lepContext.inArgs.tableName
JpaEntityContext ctx = lepContext.inArgs.context

// DELETE events are never intercepted.
if (ctx.domainEventOperation == DefaultDomainEventOperation.DELETE) {
    return false
}

// Dispatch: table -> predicate
Map<String, Closure<Boolean>> interceptors = [
    'outbox'    : { isOutboxIntercepted(ctx) }
]

def isIntercepted = (interceptors[tableName] ?: { false }).call()
log.info("tableName:${tableName}, isIntercepted:${isIntercepted}")
return isIntercepted

private static boolean isOutboxIntercepted(JpaEntityContext ctx) {
    if (ctx.entity.status == RecordStatus.NEW) {
        return true
    }
    return false
}
