package com.cm.spring_ai_demo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

/**
 * Servlet filter that intercepts MaxUploadSizeExceededException thrown during
 * multipart request parsing — before the DispatcherServlet can handle it.
 * Without this filter, Tomcat resets the TCP connection causing ERR_CONNECTION_RESET in browsers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UploadSizeExceptionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (Throwable ex) {
            Throwable cause = ex;
            boolean isSizeExceeded = false;
            while (cause != null) {
                String className = cause.getClass().getName();
                if (cause instanceof MaxUploadSizeExceededException ||
                    className.contains("SizeLimitExceededException") ||
                    className.contains("FileSizeLimitExceededException")) {
                    isSizeExceeded = true;
                    break;
                }
                cause = cause.getCause();
            }

            if (isSizeExceeded) {
                if (!httpResponse.isCommitted()) {
                    httpResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write(
                        "{\"status\":\"error\",\"code\":\"FILE_TOO_LARGE\"," +
                        "\"message\":\"The uploaded PDF exceeds the 50MB size limit. Please upload a smaller PDF.\"}"
                    );
                    httpResponse.getWriter().flush();
                }
                return;
            }

            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof ServletException) {
                throw (ServletException) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new ServletException(ex);
            }
        }
    }
}
