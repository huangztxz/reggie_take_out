package com.itcast.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itcast.reggie.common.BaseContext;
import com.itcast.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1. 获取本次请求的URI
        String uri = request.getRequestURI();
        //定义不需要处理的请求路径,放行
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 2. 判断本次请求, 是否需要登录, 才可以访问
        boolean check = check(urls, uri);

        // 3. 如果不需要，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",uri);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1. 判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("employee"));

            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            // long id = Thread.currentThread().getId();
            // log.info("线程id：{}",id);

            filterChain.doFilter(request, response);
            return;
        }

        // 4-2. 判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user")!=null){
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("user"));

            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            // long id = Thread.currentThread().getId();
            // log.info("线程id：{}",id);

            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果未登录, 则返回未登录结果
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 进行路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param uri
     * @return
     */
    public boolean check(String[] urls, String uri){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if(match){
                return true;
            }
        }
        return false;
    }
}
