package com.oraclejava.demodog.cart;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.demodog.dog.Dog;
import com.oraclejava.demodog.dog.DogRepository;

@Service
@Transactional(readOnly = true)
public class CartService {

	private final Cart cart;

	private final DogRepository dogRepository;

	public CartService(Cart cart, DogRepository dogRepository) {
		this.cart = cart;
		this.dogRepository = dogRepository;
	}

	/** 상품을 담는다. 없는 상품이면 404. */
	public CartResponse add(CartItemRequest request) {
		if (!dogRepository.existsById(request.dogId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found: " + request.dogId());
		}
		cart.add(request.dogId(), request.quantityOrDefault());
		return view();
	}

	public CartResponse remove(Long dogId) {
		cart.remove(dogId);
		return view();
	}

	/** 장바구니 목록. 담은 뒤에 삭제된 상품은 장바구니에서도 뺀다. */
	public CartResponse view() {
		Map<Long, Integer> items = cart.getItems();
		Map<Long, Dog> dogs = dogRepository.findAllById(items.keySet()).stream()
				.collect(Collectors.toMap(Dog::getId, Function.identity()));
		List<CartResponse.Item> result = items.entrySet().stream()
				.filter(entry -> {
					if (dogs.containsKey(entry.getKey())) {
						return true;
					}
					cart.remove(entry.getKey());
					return false;
				})
				.map(entry -> CartResponse.Item.of(dogs.get(entry.getKey()), entry.getValue()))
				.toList();
		return CartResponse.of(result);
	}

}
