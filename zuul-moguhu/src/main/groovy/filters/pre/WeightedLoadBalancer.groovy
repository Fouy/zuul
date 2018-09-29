package filters.pre

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.constants.ZuulConstants
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.dependency.ribbon.RibbonConfig
import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty

/**
 *
 */

class WeightedLoadBalancer extends ZuulFilter {

    DynamicStringProperty AltVIP = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_ROUTER_ALT_ROUTE_VIP, null)
    DynamicStringProperty AltHost = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_ROUTER_ALT_ROUTE_HOST, null)
    DynamicIntProperty AltPercent = DynamicPropertyFactory.getInstance().getIntProperty(ZuulConstants.ZUUL_ROUTER_ALT_ROUTE_PERMYRIAD, 0)
    //0-10000 is 0-100% of traffic
    DynamicIntProperty AltPercentMaxLimit = DynamicPropertyFactory.getInstance().getIntProperty(ZuulConstants.ZUUL_ROUTER_ALT_ROUTE_MAXLIMIT, 500)
    String envRegion = System.getenv("EC2_REGION");

    Random rand = new Random()

    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return 30
    }

    /**
     * returns true if a randomValue is less than the zuul.router.alt.route.permyriad property value
     * @return
     */
    @Override
    boolean shouldFilter() {
        if (AltPercent.get() == 0) return false
        if (AltVIP.get() == null && AltHost.get() == null) return false
        if (NFRequestContext.currentContext.host != null) return false
        //host calls are not going to be loaltPad calculated here.
        if (RequestContext.currentContext.sendZuulResponse == false) return false;
        if (AltPercent.get() > AltPercentMaxLimit.get()) return false

        int randomValue = rand.nextInt(10000)
        return randomValue <= AltPercent.get()
    }

    @Override
    Object run() {
        if (AltVIP.get() != null) {
            (NFRequestContext.currentContext).routeVIP = AltVIP.get()
            if (NFRequestContext.currentContext.routeVIP.startsWith(RibbonConfig.getApplicationName())) {
                NFRequestContext.getCurrentContext().zuulToZuul = true // for zuulToZuul load testing
            }
            return true
        }
        if (AltHost.get() != null) {
            try {
                (NFRequestContext.currentContext).host = new URL(AltHost.get())
                (NFRequestContext.currentContext).routeVIP = null

            } catch (Exception e) {
                e.printStackTrace()
                return false;
            }
            return true
        }

    }

}
