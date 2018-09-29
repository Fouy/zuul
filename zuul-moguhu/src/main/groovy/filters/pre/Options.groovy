package filters.pre

import com.moguhu.zuul.context.RequestContext
import com.netflix.zuul.filters.StaticResponseFilter

/**
 */
class Options extends StaticResponseFilter {

    boolean shouldFilter() {
        String method = RequestContext.currentContext.getRequest() getMethod();
        if (method.equalsIgnoreCase("options")) return true;
    }


    @Override
    String uri() {
        return "any path here"
    }

    @Override
    String responseBody() {
        return "" // empty response
    }

}
