package com.weekend.frmljslt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = JsltTransformationApplication.class)
@AutoConfigureMockMvc
public class TransformControllerTest extends AbstractTestNGSpringContextTests {

    public static final String SIMPLE_SAMPLE =  "{" +
            "    \"eventId\": \"878237843\"," +
            "    \"device\": {" +
            "        \"osType\": \"Linux\"," +
            "        \"model\": \"Laptop\"" +
            "    }" +
            "}";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void testTransformInput_Success() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/events/transforms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(SIMPLE_SAMPLE);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.eventId", is("878237843")))
                .andExpect(jsonPath("$.device_os", is("Linux")))
        ;
    }

    @Test
    public void testTransformInput_Invalid() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/events/transforms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("");

        mockMvc.perform(mockRequest)
                .andExpect(status().is4xxClientError());
    }
}