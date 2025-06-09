package com.masterello.commons.monitoring.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class AccessLogFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val uri = request.requestURI
        //detect the uri who dont need to be log
        if (uri.startsWith("/actuator")) {
            //add the "No_LOG" Attribute to request, the value is not important, there only need to be not null
            request.setAttribute("NO_LOG", "true")
        }
        filterChain.doFilter(request, response)
    }
}