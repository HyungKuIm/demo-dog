package com.oraclejava.demodog.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 장바구니 담기 요청. quantity를 빼면 1개로 담는다.
 */
public record CartItemRequest(
		@NotNull Long dogId,
		@Positive @Max(99) Integer quantity) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}

}
