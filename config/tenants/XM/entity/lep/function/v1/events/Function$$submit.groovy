package ROOT0CORE.entity.lep.function.v1.events

import XM.entity.lep.commons.DomainEventsService
import com.icthh.xm.commons.exceptions.BusinessException
import org.springframework.http.HttpMethod

def domainEventsService = new DomainEventsService(lepContext)
def request = lepContext.inArgs.functionInput
def requestMethod = lepContext.inArgs.httpMethod

switch (requestMethod) {
    case HttpMethod.POST.name():
        domainEventsService.publishEvent(request)
        return [:]
    default: throw new BusinessException("error.api.events", "Unsupported http method $requestMethod")
}
