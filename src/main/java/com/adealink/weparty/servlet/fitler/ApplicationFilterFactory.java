/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adealink.weparty.servlet.fitler;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.mock.web.MockHttpServletRequest;
import com.adealink.weparty.util.SpringContextUtils;

import javax.servlet.ServletRequest;
import java.util.List;

/**
 * Factory for the creation and caching of Filters and creation
 * of Filter Chains.
 *
 * @author Greg Murray
 * @author Remy Maucherat
 */
public final class ApplicationFilterFactory {

    private ApplicationFilterFactory() {
        // Prevent instance creation. This is a utility class.
    }


    /**
     * Construct and return a FilterChain implementation that will wrap the
     * execution of the specified servlet instance.  If we should not execute
     * a filter chain at all, return <code>null</code>.
     *
     * @param ctx
     * @param request The servlet request we are processing
     */
    @SuppressWarnings("deprecation")
    public static ApplicationFilterChain createFilterChain(ChannelHandlerContext ctx, ServletRequest request) {
        String requestPath = ((MockHttpServletRequest) request).getRequestURI();

        ApplicationFilterConfigRegistry registry = SpringContextUtils.getBean(ApplicationFilterConfigRegistry.class);

        List<ApplicationFilterConfig> applicationFilterConfigs = registry.getApplicationFilterConfigs();

        ApplicationFilterChain filterChain = new ApplicationFilterChain();
        for (ApplicationFilterConfig applicationFilterConfig : applicationFilterConfigs) {
            if (ApplicationFilterFactory.matchFiltersURL(applicationFilterConfig.getUrlPatterns(), requestPath)) {
                filterChain.addFilter(applicationFilterConfig);
            }
        }

        // 主要是需要在执行完所有filter后，调用ctx将请求传递下去
        filterChain.setCtx(ctx);

        // Return the completed filter chain
        return (filterChain);
    }

    /**
     * filter路径匹配处理，参考org.apache.catalina.core.ApplicationFilterFactory#matchFiltersURL(java.lang.String, java.lang.String)
     */
    private static boolean matchFiltersURL(String filterPath, String requestPath) {
        if (filterPath == null)
            return false;

        // Case 1 - Exact Match
        if (filterPath.equals(requestPath))
            return true;

        // Case 2 - Path Match ("/.../*")
        if (filterPath.equals("/*"))
            return true;
        if (filterPath.endsWith("/*")) {
            if (filterPath.regionMatches(0, requestPath, 0,
                    filterPath.length() - 2)) {
                if (requestPath.length() == (filterPath.length() - 2)) {
                    return true;
                } else if ('/' == requestPath.charAt(filterPath.length() - 2)) {
                    return true;
                }
            }
            return false;
        }

        // Case 3 - Extension Match
        if (filterPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period)
                    == (filterPath.length() - 1))) {
                return (filterPath.regionMatches(2, requestPath, period + 1,
                        filterPath.length() - 2));
            }
        }

        // Case 4 - "Default" Match
        return false; // NOTE - Not relevant for selecting filters

    }
}
