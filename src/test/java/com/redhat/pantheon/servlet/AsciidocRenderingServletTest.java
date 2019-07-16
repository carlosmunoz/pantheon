package com.redhat.pantheon.servlet;

import com.google.common.base.Function;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.model.ModuleRevision;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class AsciidocRenderingServletTest {

    private final SlingContext slingContext = new SlingContext();

    final Function<Resource, Module> mockModuleAdapter= input -> new Module(input);

    @Mock AsciidoctorService asciidoctorService;

    @Test
    @DisplayName("Generate html content from asciidoc")
    public void testGenerateHtmlFromAsciidoc() throws Exception {
        // Given
        slingContext.build()
                .resource("/module/locales/en_US/revisions/v1/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        slingContext.registerAdapter(Resource.class, Module.class, mockModuleAdapter);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        slingContext.request().setResource(resource);
        lenient().when(asciidoctorService.getModuleHtml(any(ModuleRevision.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(asciidoctorService);
        servlet.init();

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getModuleHtml(any(ModuleRevision.class), anyMap(), eq(false));
    }

    @Test
    @DisplayName("Generate html content from asciidoc specifying the rerender parameter")
    public void testGenerateHtmlFromAsciidocWithRerender() throws Exception {

        // Given
        slingContext.build()
                .resource("/module/locales/en_US/revisions/v1/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        slingContext.registerAdapter(Resource.class, Module.class, mockModuleAdapter);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        slingContext.request().setResource(resource);
        slingContext.request().getParameterMap().put(AsciidocRenderingServlet.PARAM_RERENDER, new String[]{"true"});
        lenient().when(asciidoctorService.getModuleHtml(any(ModuleRevision.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(asciidoctorService);
        servlet.init();

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getModuleHtml(any(ModuleRevision.class), anyMap(), eq(true));
    }

    @Test
    @DisplayName("Generate html content from asciidoc specifying context parameters")
    public void testGenerateHtmlFromAsciidocWithContext() throws Exception {

        // Given
        slingContext.build()
                .resource("/module/locales/en_US/revisions/v1/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        slingContext.registerAdapter(Resource.class, Module.class, mockModuleAdapter);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        slingContext.request().setResource(resource);
        slingContext.request().getParameterMap().put(AsciidocRenderingServlet.PARAM_RERENDER, new String[]{"true"});
        slingContext.request().getParameterMap().put("ctx_arg", new String[]{"value"});
        slingContext.request().getParameterMap().put("non_ctx_arg", new String[]{"unaccepted"});
        lenient().when(asciidoctorService.getModuleHtml(any(ModuleRevision.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(asciidoctorService);
        servlet.init();

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());

        ArgumentCaptor<Map> contextArguments = ArgumentCaptor.forClass(Map.class);
        verify(asciidoctorService).getModuleHtml(any(ModuleRevision.class), contextArguments.capture(), eq(true));
        assertEquals(1, contextArguments.getValue().size());
        assertTrue(contextArguments.getValue().containsKey("arg"));
    }
}
