package filters.pre

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.exception.ZuulException
import com.moguhu.zuul.stats.ErrorStatsManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ErrorResponse extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponse.class);

    @Override
    String filterType() {
        return 'error'
    }

    @Override
    int filterOrder() {
        return 1
    }


    boolean shouldFilter() {
        return RequestContext.getCurrentContext().get("ErrorHandled") == null
    }


    Object run() {

        RequestContext context = RequestContext.currentContext
        Throwable ex = context.getThrowable()
        try {
            LOG.error(ex.getMessage(), ex);
            throw ex
        } catch (ZuulException e) {
            String cause = e.errorCause
            if (cause == null) cause = "UNKNOWN"
            RequestContext.getCurrentContext().getResponse().addHeader("X-Netflix-Error-Cause", "Zuul Error: " + cause)
            if (e.nStatusCode == 404) {
                ErrorStatsManager.manager.putStats("ROUTE_NOT_FOUND", "")
            } else {
                ErrorStatsManager.manager.putStats(RequestContext.getCurrentContext().route, "Zuul_Error_" + cause)
            }

            if (overrideStatusCode) {
                RequestContext.getCurrentContext().setResponseStatusCode(200);


            } else {
                RequestContext.getCurrentContext().setResponseStatusCode(e.nStatusCode);
            }
            context.setSendZuulResponse(false)
            context.setResponseBody("${getErrorMessage(e, e.nStatusCode)}")

        } catch (Throwable throwable) {
            RequestContext.getCurrentContext().getResponse().addHeader("X-Zuul-Error-Cause", "Zuul Error UNKNOWN Cause")
            ErrorStatsManager.manager.putStats(RequestContext.getCurrentContext().route, "Zuul_Error_UNKNOWN_Cause")

            if (overrideStatusCode) {
                RequestContext.getCurrentContext().setResponseStatusCode(200);
            } else {
                RequestContext.getCurrentContext().setResponseStatusCode(500);
            }
            context.setSendZuulResponse(false)
            context.setResponseBody("${getErrorMessage(throwable, 500)}")

        } finally {
            context.set("ErrorHandled") //ErrorResponse was handled
            return null;
        }

    }

    /**
     * JSON/ xml ErrorResponse responses
     *
     * v=1 or unspecified:
     * <status>
     *     <status_code>status_code</status_code>
     *     <message>message</message>
     * </status>
     *
     * v=1.5,2.0:
     * <status>
     *     <message>user_id is invalid</message>
     * </status>
     *
     * v=1.5,2.0:
     *{"status": {"message": "user_id is invalid"}}*
     * v=1 or unspecified:
     */
    String getErrorMessage(Throwable ex, int status_code) {
        String ver = version
        String format = outputType
        switch (ver) {
            case '1':
            case '1.0':
                switch (format) {
                    case 'json':
                        RequestContext.getCurrentContext().getResponse().setContentType('application/json')
                        String response = """{"status": {"message": "${ex.message}", "status_code": ${status_code}}}"""
                        if (callback) {
                            response = callback + "(" + response + ");"
                        }
                        return response
                    case 'xml':
                    default:
                        RequestContext.getCurrentContext().getResponse().setContentType('application/xml')
                        return """<status>
  <status_code>${status_code}</status_code>
  <message>${ex.message}</message>
</status>"""
                }
                break;
            case '1.5':
            case '2.0':
            default:
                switch (format) {
                    case 'json':
                        RequestContext.getCurrentContext().getResponse().setContentType('application/json')
                        String response = """{"status": {"message": "${ex.message}"}}"""
                        if (callback) {
                            response = callback + "(" + response + ");"
                        }
                        return response
                    case 'xml':
                    default:
                        RequestContext.getCurrentContext().getResponse().setContentType('application/xml')
                        return """<status>
<message>${ex.message}</message>
</status>"""
                }
                break;

        }

    }

    boolean getOverrideStatusCode() {
        String override = RequestContext.currentContext.getRequest().getParameter("override_error_status")
        if (callback != null) return true;
        if (override == null) return false
        return Boolean.valueOf(override)

    }

    String getCallback() {
        String callback = RequestContext.currentContext.getRequest().getParameter("callback")
        if (callback == null) return null;
        return callback;
    }

    String getOutputType() {
        String output = RequestContext.currentContext.getRequest().getParameter("output")
        if (output == null) return "xml"
        return output;
    }

    String getVersion() {
        String version = RequestContext.currentContext.getRequest().getParameter("v")
        if (version == null) return "1"
        if (overrideStatusCode) return "1"
        return version;
    }

}