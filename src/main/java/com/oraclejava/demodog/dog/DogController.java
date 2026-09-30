package com.oraclejava.demodog.dog;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dogs")
public class DogController {

	private final DogService dogService;

	public DogController(DogService dogService) {
		this.dogService = dogService;
	}

	@GetMapping
	public List<Dog> list() {
		return dogService.findAll();
	}

	@GetMapping("/{id}")
	public Dog get(@PathVariable Long id) {
		return dogService.view(id);
	}

	@PostMapping
	public ResponseEntity<Dog> create(@Valid @RequestBody DogRequest request) {
		Dog dog = dogService.create(request);
		return ResponseEntity.created(URI.create("/dogs/" + dog.getId())).body(dog);
	}

	@PutMapping("/{id}")
	public Dog update(@PathVariable Long id, @Valid @RequestBody DogRequest request) {
		return dogService.update(id, request);
	}

	@PostMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Dog uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
		return dogService.uploadImage(id, file);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		dogService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
