package com.outsera.goldenraspberry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outsera.goldenraspberry.dto.IntervalResponseDto;
import com.outsera.goldenraspberry.dto.ProducerIntervalDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GoldenraspberryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Movie Controller Integration Tests")
class MovieControllerIntegrationTest {

	private static final String PRODUCER_INTERVALS_ENDPOINT = "/api/producers/intervals";
	private static final String JOEL_SILVER = "Joel Silver";
	private static final String MATTHEW_VAUGHN = "Matthew Vaughn";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Nested
	@DisplayName("CSV Data Loading Tests")
	class CsvDataLoadingTests {

		@Test
		@DisplayName("Should load CSV data successfully")
		void shouldLoadCsvDataSuccessfully() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			assertThat(response.getMin())
					.as("Minimum intervals should not be empty after CSV load")
					.isNotEmpty();

			assertThat(response.getMax())
					.as("Maximum intervals should not be empty after CSV load")
					.isNotEmpty();
		}

		@Test
		@DisplayName("Should contain expected producers from CSV")
		void shouldContainExpectedProducersFromCsv() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			List<String> allProducers = response.getMin().stream()
					.map(ProducerIntervalDto::getProducer)
					.toList();

			assertThat(allProducers)
					.as("Joel Silver should be present in results")
					.contains(JOEL_SILVER);
		}
	}

	@Nested
	@DisplayName("Minimum Interval Tests")
	class MinimumIntervalTests {

		@Test
		@DisplayName("Should have Joel Silver with 1-year minimum interval")
		void shouldHaveJoelSilverWithOneYearInterval() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			ProducerIntervalDto joelSilver = findProducerInList(response.getMin(), JOEL_SILVER);

			assertThat(joelSilver).isNotNull();
			assertThat(joelSilver.getInterval())
					.as("Joel Silver's minimum interval should be 1 year")
					.isEqualTo(1);
			assertThat(joelSilver.getPreviousWin())
					.as("Previous win year")
					.isEqualTo(1990);
			assertThat(joelSilver.getFollowingWin())
					.as("Following win year")
					.isEqualTo(1991);
		}

		@Test
		@DisplayName("All minimum intervals should have the same interval value")
		void allMinimumIntervalsShouldHaveSameValue() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			List<Integer> intervals = response.getMin().stream()
					.map(ProducerIntervalDto::getInterval)
					.distinct()
					.toList();

			assertThat(intervals)
					.as("All minimum intervals should have the same value")
					.hasSize(1)
					.containsOnly(1);
		}

		@Test
		@DisplayName("Minimum interval should be 1 year based on CSV data")
		void minimumIntervalShouldBeOneYear() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			assertThat(response.getMin())
					.as("Should have minimum intervals")
					.isNotEmpty();

			int minInterval = response.getMin().get(0).getInterval();

			assertThat(minInterval)
					.as("Minimum interval should be 1 year")
					.isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("Maximum Interval Tests")
	class MaximumIntervalTests {

		@Test
		@DisplayName("Should have Matthew Vaughn with 13-year maximum interval")
		void shouldHaveMatthewVaughnWithThirteenYearInterval() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			ProducerIntervalDto matthewVaughn = findProducerInList(response.getMax(), MATTHEW_VAUGHN);

			assertThat(matthewVaughn).isNotNull();
			assertThat(matthewVaughn.getInterval())
					.as("Matthew Vaughn's maximum interval should be 13 years")
					.isEqualTo(13);
			assertThat(matthewVaughn.getPreviousWin())
					.as("Previous win year")
					.isEqualTo(2002);
			assertThat(matthewVaughn.getFollowingWin())
					.as("Following win year")
					.isEqualTo(2015);
		}

		@Test
		@DisplayName("All maximum intervals should have the same interval value")
		void allMaximumIntervalsShouldHaveSameValue() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			List<Integer> intervals = response.getMax().stream()
					.map(ProducerIntervalDto::getInterval)
					.distinct()
					.toList();

			assertThat(intervals)
					.as("All maximum intervals should have the same value")
					.hasSize(1)
					.containsOnly(13);
		}

		@Test
		@DisplayName("Maximum interval should be 13 years based on CSV data")
		void maximumIntervalShouldBeThirteenYears() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			assertThat(response.getMax())
					.as("Should have maximum intervals")
					.isNotEmpty();

			int maxInterval = response.getMax().get(0).getInterval();

			assertThat(maxInterval)
					.as("Maximum interval should be 13 years")
					.isEqualTo(13);
		}
	}

	@Nested
	@DisplayName("API Response Structure Tests")
	class ApiResponseStructureTests {

		@Test
		@DisplayName("Should return OK status with JSON content type")
		void shouldReturnOkStatusWithJsonContentType() throws Exception {
			mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		}

		@Test
		@DisplayName("Response should have min and max arrays")
		void responseShouldHaveMinAndMaxArrays() throws Exception {
			mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.min").exists())
					.andExpect(jsonPath("$.min").isArray())
					.andExpect(jsonPath("$.max").exists())
					.andExpect(jsonPath("$.max").isArray());
		}

		@Test
		@DisplayName("Min intervals should have required fields")
		void minIntervalsShouldHaveRequiredFields() throws Exception {
			mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.min[0].producer").exists())
					.andExpect(jsonPath("$.min[0].interval").exists())
					.andExpect(jsonPath("$.min[0].previousWin").exists())
					.andExpect(jsonPath("$.min[0].followingWin").exists());
		}

		@Test
		@DisplayName("Max intervals should have required fields")
		void maxIntervalsShouldHaveRequiredFields() throws Exception {
			mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.max[0].producer").exists())
					.andExpect(jsonPath("$.max[0].interval").exists())
					.andExpect(jsonPath("$.max[0].previousWin").exists())
					.andExpect(jsonPath("$.max[0].followingWin").exists());
		}

		@Test
		@DisplayName("Response should be valid JSON")
		void responseShouldBeValidJson() throws Exception {
			MvcResult result = mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT))
					.andExpect(status().isOk())
					.andReturn();

			String jsonResponse = result.getResponse().getContentAsString();
			IntervalResponseDto response = objectMapper.readValue(jsonResponse, IntervalResponseDto.class);

			assertThat(response).isNotNull();
			assertThat(response.getMin()).isNotNull();
			assertThat(response.getMax()).isNotNull();
		}
	}

	@Nested
	@DisplayName("Data Consistency Tests")
	class DataConsistencyTests {

		@Test
		@DisplayName("Minimum intervals should have valid data")
		void minimumIntervalsShouldHaveValidData() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			response.getMin().forEach(interval -> {
				assertThat(interval.getProducer())
						.as("Producer name should not be null or empty")
						.isNotNull()
						.isNotBlank();

				assertThat(interval.getInterval())
						.as("Interval should be non-negative")
						.isGreaterThanOrEqualTo(0);

				assertThat(interval.getPreviousWin())
						.as("Previous win year should be positive")
						.isPositive();

				assertThat(interval.getFollowingWin())
						.as("Following win should be after previous win")
						.isGreaterThan(interval.getPreviousWin());

				assertThat(interval.getInterval())
						.as("Interval should equal year difference")
						.isEqualTo(interval.getFollowingWin() - interval.getPreviousWin());
			});
		}

		@Test
		@DisplayName("Maximum intervals should have valid data")
		void maximumIntervalsShouldHaveValidData() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			response.getMax().forEach(interval -> {
				assertThat(interval.getProducer())
						.as("Producer name should not be null or empty")
						.isNotNull()
						.isNotBlank();

				assertThat(interval.getInterval())
						.as("Interval should be non-negative")
						.isGreaterThanOrEqualTo(0);

				assertThat(interval.getPreviousWin())
						.as("Previous win year should be positive")
						.isPositive();

				assertThat(interval.getFollowingWin())
						.as("Following win should be after previous win")
						.isGreaterThan(interval.getPreviousWin());

				assertThat(interval.getInterval())
						.as("Interval should equal year difference")
						.isEqualTo(interval.getFollowingWin() - interval.getPreviousWin());
			});
		}

		@Test
		@DisplayName("Minimum interval should be less than or equal to maximum interval")
		void minimumShouldBeLessThanOrEqualToMaximum() throws Exception {
			IntervalResponseDto response = performRequestAndGetResponse();

			int minInterval = response.getMin().get(0).getInterval();
			int maxInterval = response.getMax().get(0).getInterval();

			assertThat(minInterval)
					.as("Minimum interval should be <= maximum interval")
					.isLessThanOrEqualTo(maxInterval);
		}
	}

	private IntervalResponseDto performRequestAndGetResponse() throws Exception {
		MvcResult result = mockMvc.perform(get(PRODUCER_INTERVALS_ENDPOINT)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		return objectMapper.readValue(jsonResponse, IntervalResponseDto.class);
	}

	private ProducerIntervalDto findProducerInList(List<ProducerIntervalDto> intervals, String producerName) {
		return intervals.stream()
				.filter(p -> producerName.equals(p.getProducer()))
				.findFirst()
				.orElse(null);
	}
}