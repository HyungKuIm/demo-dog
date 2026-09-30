package com.oraclejava.demodog.dog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DogControllerTests {

	private static final String POODLE = """
			{"kind":"Poodle","price":1500000,"country":"France",
			 "height":38.5,"weight":6.2,"content":"Smart and friendly"}
			""";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	DogRepository dogRepository;

	@Test
	void crud() throws Exception {
		String location = mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON).content(POODLE))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.kind").value("Poodle"))
				.andExpect(jsonPath("$.readcount").value(0))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get("/dogs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get(location)).andExpect(jsonPath("$.readcount").value(1));
		mockMvc.perform(get(location)).andExpect(jsonPath("$.readcount").value(2));

		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
				.content(POODLE.replace("1500000", "1200000")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(1200000))
				.andExpect(jsonPath("$.readcount").value(2));

		mockMvc.perform(delete(location)).andExpect(status().isNoContent());
		mockMvc.perform(get(location)).andExpect(status().isNotFound());
	}

	@Test
	void missingDogReturns404() throws Exception {
		mockMvc.perform(get("/dogs/9999")).andExpect(status().isNotFound());
		mockMvc.perform(put("/dogs/9999").contentType(MediaType.APPLICATION_JSON).content(POODLE))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/dogs/9999")).andExpect(status().isNotFound());
	}

	@Test
	void invalidRequestReturns400() throws Exception {
		mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON).content("""
				{"kind":"","price":-1}
				"""))
				.andExpect(status().isBadRequest());
	}

}
