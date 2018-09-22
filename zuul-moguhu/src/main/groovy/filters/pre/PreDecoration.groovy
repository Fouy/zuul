package filters.pre

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.exception.ZuulException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.moguhu.zuul.constants.ZuulHeaders.*

/**
 *
 */
public class PreDecoration extends ZuulFilter {

    @Override
    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return 20
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
        if (RequestContext.currentContext.getRequest().getParameter("url") != null) {
            try {
                RequestContext.getCurrentContext().routeHost = new URL(RequestContext.currentContext.getRequest().getParameter("url"))
                RequestContext.currentContext.setResponseGZipped(true)
            } catch (MalformedURLException e) {
                throw new ZuulException(e, "Malformed URL", 400, "MALFORMED_URL")
            }
        }
        setOriginRequestHeaders()
        return null
    }

    void setOriginRequestHeaders() {
        RequestContext context = RequestContext.currentContext
        context.addZuulRequestHeader("X-Netflix.request.toplevel.uuid", UUID.randomUUID().toString())
        context.addZuulRequestHeader(X_FORWARDED_FOR, context.getRequest().remoteAddr)
        context.addZuulRequestHeader(X_NETFLIX_CLIENT_HOST, context.getRequest().getHeader(HOST))
        if (context.getRequest().getHeader(X_FORWARDED_PROTO) != null) {
            context.addZuulRequestHeader(X_NETFLIX_CLIENT_PROTO, context.getRequest().getHeader(X_FORWARDED_PROTO))
        }
    }


    @RunWith(MockitoJUnitRunner.class)
    public static class TestUnit {

        @Mock
        HttpServletResponse response
        @Mock
        HttpServletRequest request

        @Test
        public void testPreHeaders() {
            PreDecoration ppd = new PreDecoration()
            HttpServletRequest request = Mockito.mock(HttpServletRequest.class)
            RequestContext.currentContext.request = request
            Mockito.when(request.remoteAddr).thenReturn("1.1.1.1")
            Mockito.when(request.getHeader("Host")).thenReturn("moldfarm.com")
            Mockito.when(request.getHeader("X-Forwarded-Proto")).thenReturn("https")

            ppd.setOriginRequestHeaders()

            Map<String, String> headers = RequestContext.currentContext.zuulRequestHeaders
            Assert.assertNotNull(headers["x-netflix.request.toplevel.uuid"])
            Assert.assertNotNull(headers["x-forwarded-for"])
            Assert.assertNotNull(headers["x-netflix.client-host"])
            Assert.assertNotNull(headers["x-netflix.client-proto"])

        }

    }

}
