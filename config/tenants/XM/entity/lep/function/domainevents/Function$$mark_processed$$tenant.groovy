package ROOT0CORE.entity.lep.function.domainevents

import XM.entity.lep.commons.DomainEventsService
import com.icthh.xm.commons.exceptions.BusinessException
import org.springframework.http.HttpMethod

def domainEventsService = new DomainEventsService(lepContext)
def request = lepContext.inArgs.functionInput
def requestMethod = lepContext.inArgs.httpMethod

def eventId = request.eventId as String
switch (requestMethod) {
    case HttpMethod.PUT.name():
        domainEventsService.setEventProcessed(eventId)
        return [:]
    default: throw new BusinessException("error.api.domainevents", "Unsupported http method $requestMethod")
}
