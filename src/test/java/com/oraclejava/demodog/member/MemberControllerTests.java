package com.oraclejava.demodog.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void signupLoginLogout() throws Exception {
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"dogfan\",\"password\":\"pass1234\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("dogfan"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist());

		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(get("/members/me").session(session))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/members/login").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"dogfan\",\"password\":\"pass1234\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("dogfan"));

		mockMvc.perform(get("/members/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("USER"));

		mockMvc.perform(post("/members/logout").session(session))
				.andExpect(status().isNoContent());
	}

	@Test
	void adminIsPreinserted() throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/members/login").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin1234\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));

		mockMvc.perform(get("/members/me").session(session))
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void invalidRequests() throws Exception {
		// 이미 있는 아이디로는 가입할 수 없다.
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"whatever\"}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"ab\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/members/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/members/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"nobody\",\"password\":\"pass1234\"}"))
				.andExpect(status().isUnauthorized());
	}

}
