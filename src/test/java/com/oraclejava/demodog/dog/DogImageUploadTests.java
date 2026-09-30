package com.oraclejava.demodog.dog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DogImageUploadTests {

	@TempDir
	static Path imageDir;

	@DynamicPropertySource
	static void imageDir(DynamicPropertyRegistry registry) {
		registry.add("demodog.image-dir", imageDir::toString);
	}

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3 };

	private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3 };

	@Autowired
	MockMvc mockMvc;

	@Test
	void uploadServeReplaceAndDelete() throws Exception {
		String location = "/dogs/" + createDog();

		String first = uploadedImage(location, new MockMultipartFile("file", "a.png", "image/png", PNG));
		assertThat(first).startsWith("/images/").endsWith(".png");
		assertThat(imageDir.resolve(first.substring("/images/".length()))).exists();

		mockMvc.perform(get(first))
				.andExpect(status().isOk())
				.andExpect(content().bytes(PNG));

		String second = uploadedImage(location, new MockMultipartFile("file", "b.jpg", "image/jpeg", JPEG));
		assertThat(second).endsWith(".jpg");
		assertThat(imageDir.resolve(first.substring("/images/".length()))).doesNotExist();

		mockMvc.perform(delete(location)).andExpect(status().isNoContent());
		assertThat(imageDir.resolve(second.substring("/images/".length()))).doesNotExist();
	}

	@Test
	void detectsImageTypeFromContentNotFromClientContentType() throws Exception {
		String location = "/dogs/" + createDog();

		// Postman 등은 파일 파트의 Content-Type을 application/octet-stream이나 image/jpg로 보내기도 한다.
		assertThat(uploadedImage(location, new MockMultipartFile("file", "a.png", "application/octet-stream", PNG)))
				.endsWith(".png");
		assertThat(uploadedImage(location, new MockMultipartFile("file", "b.jpg", "image/jpg", JPEG)))
				.endsWith(".jpg");
		assertThat(uploadedImage(location, new MockMultipartFile("file", "noext", null, PNG)))
				.endsWith(".png");
	}

	@Test
	void rejectsNonImageAndEmptyFiles() throws Exception {
		String location = "/dogs/" + createDog();

		mockMvc.perform(multipart(location + "/image")
				.file(new MockMultipartFile("file", "evil.html", "text/html", "<script>".getBytes())))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Only JPEG")));
		mockMvc.perform(multipart(location + "/image")
				.file(new MockMultipartFile("file", "fake.png", "image/png", "<script>".getBytes())))
				.andExpect(status().isUnsupportedMediaType());
		mockMvc.perform(multipart(location + "/image")
				.file(new MockMultipartFile("file", "empty.png", "image/png", new byte[0])))
				.andExpect(status().isBadRequest());
		try (var files = Files.list(imageDir)) {
			assertThat(files).isEmpty();
		}
	}

	@Test
	void uploadToMissingDogReturns404() throws Exception {
		mockMvc.perform(multipart("/dogs/9999/image")
				.file(new MockMultipartFile("file", "a.png", "image/png", PNG)))
				.andExpect(status().isNotFound());
	}

	private long createDog() throws Exception {
		String body = mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"kind":"Maltese","price":900000}
						"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	private String uploadedImage(String location, MockMultipartFile file) throws Exception {
		String body = mockMvc.perform(multipart(location + "/image").file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.image").exists())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.image");
	}

}
