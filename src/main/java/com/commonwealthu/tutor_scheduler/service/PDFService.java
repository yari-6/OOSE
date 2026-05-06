package com.commonwealthu.tutor_scheduler.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PDFService {
    private final TemplateEngine templateEngine;

    public PDFService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateSchedulePdf(String templateName, Map<String, Object> data, WebContext context) throws Exception {

        context.setVariables(data);

        String htmlContent = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            // IMPORTANT: stable base path improves CSS + asset loading
            builder.withHtmlContent(htmlContent, "https://oose-flj7.onrender.com/");

            builder.toStream(os);

            builder.run();

            return os.toByteArray();
        }
    }
}
