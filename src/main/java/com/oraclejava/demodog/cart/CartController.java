package com.oraclejava.demodog.cart;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 세션 장바구니. 상품 상세(GET /dogs/{id})에서 POST /cart/items로 담고 GET /cart로 목록을 본다.
 * 세션 쿠키(JSESSIONID)로 장바구니를 구분하므로 클라이언트는 쿠키를 유지해야 한다.
 */
@RestController
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public CartResponse list() {
		return cartService.view();
	}

	@PostMapping("/items")
	public CartResponse add(@Valid @RequestBody CartItemRequest request) {
		return cartService.add(request);
	}

	@DeleteMapping("/items/{dogId}")
	public CartResponse remove(@PathVariable Long dogId) {
		return cartService.remove(dogId);
	}

}
