package com.oraclejava.demodog.dog;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.demodog.image.ImageStorage;

@Service
@Transactional(readOnly = true)
public class DogService {

	private final DogRepository dogRepository;

	private final ImageStorage imageStorage;

	public DogService(DogRepository dogRepository, ImageStorage imageStorage) {
		this.dogRepository = dogRepository;
		this.imageStorage = imageStorage;
	}

	public List<Dog> findAll() {
		return dogRepository.findAll(Sort.by("id"));
	}

	/** 상품 상세 조회. 조회할 때마다 조회수를 1 올린다. */
	@Transactional
	public Dog view(Long id) {
		Dog dog = get(id);
		dog.increaseReadcount();
		return dog;
	}

	@Transactional
	public Dog create(DogRequest request) {
		return dogRepository.save(new Dog(request));
	}

	@Transactional
	public Dog update(Long id, DogRequest request) {
		Dog dog = get(id);
		dog.apply(request);
		return dog;
	}

	/** 상품 이미지를 업로드한다. 기존 이미지가 있으면 교체하고 이전 파일은 지운다. */
	@Transactional
	public Dog uploadImage(Long id, MultipartFile file) {
		Dog dog = get(id);
		String oldImage = dog.getImage();
		dog.changeImage(imageStorage.store(file));
		imageStorage.delete(oldImage);
		return dog;
	}

	@Transactional
	public void delete(Long id) {
		Dog dog = get(id);
		dogRepository.delete(dog);
		imageStorage.delete(dog.getImage());
	}

	private Dog get(Long id) {
		return dogRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found: " + id));
	}

}
