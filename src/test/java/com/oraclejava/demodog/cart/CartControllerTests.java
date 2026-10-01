package com.oraclejava.demodog.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.oraclejava.demodog.dog.Dog;
import com.oraclejava.demodog.dog.DogRepository;
import com.oraclejava.demodog.dog.DogRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	DogRepository dogRepository;

	@Test
	void addAndList() throws Exception {
		Dog poodle = dogRepository.save(new Dog(new DogRequest("Poodle", 1500000, "France", null, null, null)));
		Dog jindo = dogRepository.save(new Dog(new DogRequest("Jindo", 800000, "Korea", null, null, null)));
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/cart").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.totalPrice").value(0));

		mockMvc.perform(post("/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":" + poodle.getId() + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(1));

		// 같은 상품을 다시 담으면 수량이 더해진다.
		mockMvc.perform(post("/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":" + poodle.getId() + ",\"quantity\":2}"));
		mockMvc.perform(post("/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":" + jindo.getId() + "}"));

		mockMvc.perform(get("/cart").session(session))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].kind").value("Poodle"))
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.items[0].amount").value(4500000))
				.andExpect(jsonPath("$.items[1].kind").value("Jindo"))
				.andExpect(jsonPath("$.totalQuantity").value(4))
				.andExpect(jsonPath("$.totalPrice").value(5300000));

		// 다른 세션의 장바구니는 따로다.
		mockMvc.perform(get("/cart").session(new MockHttpSession()))
				.andExpect(jsonPath("$.items.length()").value(0));

		mockMvc.perform(delete("/cart/items/" + poodle.getId()).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.totalPrice").value(800000));
	}

	@Test
	void deletedDogDisappearsFromCart() throws Exception {
		Dog poodle = dogRepository.save(new Dog(new DogRequest("Poodle", 1500000, null, null, null, null)));
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":" + poodle.getId() + "}"));

		dogRepository.delete(poodle);

		mockMvc.perform(get("/cart").session(session))
				.andExpect(jsonPath("$.items.length()").value(0));
	}

	@Test
	void invalidRequests() throws Exception {
		mockMvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON).content("{\"dogId\":9999}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":1,\"quantity\":0}"))
				.andExpect(status().isBadRequest());
	}

}
