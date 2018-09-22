package filters.pre

import com.moguhu.zuul.context.RequestContext
import com.netflix.zuul.filters.StaticResponseFilter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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


    @RunWith(MockitoJUnitRunner.class)
    public static class TestUnit {

        @Mock
        HttpServletResponse response
        @Mock
        HttpServletRequest request

        @Test
        public void testHealthcheck() {
            RequestContext.setContextClass(RequestContext.class);
            Healthcheck hc = new Healthcheck();
            HttpServletRequest request = Mockito.mock(HttpServletRequest.class)
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class)
            RequestContext.currentContext.request = request
            RequestContext.currentContext.response = response
            Mockito.when(request.getRequestURI()).thenReturn("/healthcheck")
            hc.run()

            Assert.assertTrue(hc.shouldFilter())

            Assert.assertTrue(RequestContext.currentContext.responseBody != null)
            Assert.assertTrue(RequestContext.currentContext.getResponseBody().contains("<health>ok</health>"))

            Mockito.when(request.getRequestURI()).thenReturn("healthcheck")
            Assert.assertTrue(hc.shouldFilter())

        }

    }

}
