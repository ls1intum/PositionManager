package de.tum.cit.aet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import java.nio.charset.StandardCharsets;

/**
 * Base class for REST integration tests using MockMvc.
 * Extends AbstractIntegrationTest to reuse test infrastructure.
 */
public abstract class AbstractRestIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper;

    @BeforeEach
    void setupMockMvcAndObjectMapper() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Performs a POST request with JSON body.
     */
    protected ResultActions postJson(String url, Object body) throws Exception {
        String content = body != null ? objectMapper.writeValueAsString(body) : "";
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    /**
     * Performs a PUT request with JSON body.
     */
    protected ResultActions putJson(String url, Object body) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Performs a GET request.
     */
    protected ResultActions get(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url));
    }

    /**
     * Performs a DELETE request.
     */
    protected ResultActions delete(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url));
    }

    /**
     * Performs a multipart file upload POST request.
     */
    protected ResultActions uploadFile(String url, String paramName, String filename, String content) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                paramName,
                filename,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8));
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url).file(file));
    }

    /**
     * Performs a multipart file upload POST request with bytes.
     */
    protected ResultActions uploadFile(String url, String paramName, String filename, byte[] content, String contentType) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                paramName,
                filename,
                contentType,
                content);
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url).file(file));
    }
}
