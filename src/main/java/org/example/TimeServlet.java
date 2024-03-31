package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;


@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timezone = TimezoneValidateFilter.getTimezone(req);;

        if (!req.getParameterMap().containsKey("timezone") && Objects.nonNull(req.getCookies()) && req.getCookies().length > 0) {
            timezone = req.getCookies()[0].getValue();
        }

        resp.addCookie(new Cookie("timezone", timezone));
        String dataTime = LocalDateTime.now(ZoneId.of(timezone)).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        resp.setContentType("text/html");

        Context timeContext = new Context(req.getLocale(), Map.of("timezone", timezone, "dateTime", dataTime));
        engine.process("getTime", timeContext, resp.getWriter());
        resp.getWriter().close();

    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
