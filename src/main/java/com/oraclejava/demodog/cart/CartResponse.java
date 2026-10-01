package com.oraclejava.demodog.cart;

import java.util.List;

import com.oraclejava.demodog.dog.Dog;

/**
 * 장바구니 목록. 금액은 조회 시점의 상품 가격으로 계산한다.
 */
public record CartResponse(List<Item> items, int totalQuantity, long totalPrice) {

	public static CartResponse of(List<Item> items) {
		int totalQuantity = items.stream().mapToInt(Item::quantity).sum();
		long totalPrice = items.stream().mapToLong(Item::amount).sum();
		return new CartResponse(items, totalQuantity, totalPrice);
	}

	public record Item(Long dogId, String kind, Integer price, String image, int quantity, long amount) {

		public static Item of(Dog dog, int quantity) {
			return new Item(dog.getId(), dog.getKind(), dog.getPrice(), dog.getImage(), quantity,
					(long) dog.getPrice() * quantity);
		}

	}

}
