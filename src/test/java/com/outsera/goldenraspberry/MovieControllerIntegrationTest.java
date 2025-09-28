package com.outsera.goldenraspberry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outsera.goldenraspberry.dto.IntervalResponseDto;
import com.outsera.goldenraspberry.dto.ProducerIntervalDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GoldenraspberryApplication.class)
@AutoConfigureMockMvc
public class MovieControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetProducerIntervals() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/producers/intervals")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		IntervalResponseDto response = objectMapper.readValue(jsonResponse, IntervalResponseDto.class);

		assertNotNull(response);
		assertNotNull(response.getMin());
		assertNotNull(response.getMax());

		// Verifica se há dados válidos
		if (!response.getMin().isEmpty()) {
			ProducerIntervalDto minInterval = response.getMin().get(0);
			assertNotNull(minInterval.getProducer());
			assertFalse(minInterval.getProducer().trim().isEmpty());
			assertTrue(minInterval.getInterval() >= 0);
			assertTrue(minInterval.getPreviousWin() > 0);
			assertTrue(minInterval.getFollowingWin() > minInterval.getPreviousWin());
		}

		if (!response.getMax().isEmpty()) {
			ProducerIntervalDto maxInterval = response.getMax().get(0);
			assertNotNull(maxInterval.getProducer());
			assertFalse(maxInterval.getProducer().trim().isEmpty());
			assertTrue(maxInterval.getInterval() >= 0);
			assertTrue(maxInterval.getPreviousWin() > 0);
			assertTrue(maxInterval.getFollowingWin() > maxInterval.getPreviousWin());
		}

		// Se ambos têm dados, verifica a lógica de min/max
		if (!response.getMin().isEmpty() && !response.getMax().isEmpty()) {
			int minIntervalValue = response.getMin().get(0).getInterval();
			int maxIntervalValue = response.getMax().get(0).getInterval();
			assertTrue(minIntervalValue <= maxIntervalValue,
				"Min interval should be less than or equal to max interval");
		}
	}

	@Test
	public void testApiEndpointExists() throws Exception {
		mockMvc.perform(get("/api/producers/intervals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.min").exists())
				.andExpect(jsonPath("$.max").exists());
	}

	@Test
	public void testResponseStructure() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/producers/intervals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.min").isArray())
				.andExpect(jsonPath("$.max").isArray())
				.andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		IntervalResponseDto response = objectMapper.readValue(jsonResponse, IntervalResponseDto.class);

		// Verifica que as listas não são nulas (podem estar vazias)
		assertNotNull(response.getMin());
		assertNotNull(response.getMax());

		// Se há intervalos, verifica a estrutura dos objetos
		response.getMin().forEach(interval -> {
			assertNotNull(interval.getProducer());
			assertTrue(interval.getInterval() >= 0);
			assertTrue(interval.getPreviousWin() > 0);
			assertTrue(interval.getFollowingWin() > 0);
		});

		response.getMax().forEach(interval -> {
			assertNotNull(interval.getProducer());
			assertTrue(interval.getInterval() >= 0);
			assertTrue(interval.getPreviousWin() > 0);
			assertTrue(interval.getFollowingWin() > 0);
		});
	}
}