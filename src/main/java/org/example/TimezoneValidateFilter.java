package org.example;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.zone.ZoneRulesException;
import java.util.Map;

@WebFilter(value = "/time/*")
public class TimezoneValidateFilter extends HttpFilter {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();
        JakartaServletWebApplication jswa = JakartaServletWebApplication.buildApplication(this.getServletContext());
        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(jswa);
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        engine.addTemplateResolver(resolver);
    }

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException, ZoneRulesException {
        String timezone = getTimezone(req);
        try {
            chain.doFilter(req, resp);
        } catch (ZoneRulesException e) {
            if (e.getMessage().equals("Unknown time-zone ID: " + timezone)) {
                resp.setStatus(400);
                resp.setContentType("text/html");
                Context invalidTimeContext = new Context(req.getLocale(), Map.of("invalidTimezone", "Invalid timezone"));
                engine.process("invalidTimezone", invalidTimeContext, resp.getWriter());
                resp.getWriter().close();
            }
            e.getMessage();
        }
    }
    public static String getTimezone(HttpServletRequest req) {
        if (req.getParameterMap().containsKey("timezone")) {
            String timezone =  req.getParameter("timezone");
            if (timezone.contains(" ")) {
                return timezone.substring(0, 3) + "+" + timezone.substring(4, timezone.length());
            }
            return timezone;
        }
        return "UTC";
    }
}
