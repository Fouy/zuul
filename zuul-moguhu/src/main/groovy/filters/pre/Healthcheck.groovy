package filters.pre

import com.moguhu.zuul.context.RequestContext
import com.netflix.zuul.filters.StaticResponseFilter

/**
 *
 */
class Healthcheck extends StaticResponseFilter {

    @Override
    String filterType() {
        return "healthcheck"
    }

    @Override
    String uri() {
        return "/healthcheck"
    }

    @Override
    String responseBody() {
        RequestContext.getCurrentContext().getResponse().setContentType('application/xml')
        return "<health>ok</health>"
    }

}
