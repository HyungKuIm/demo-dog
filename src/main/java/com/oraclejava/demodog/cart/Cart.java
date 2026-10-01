package com.oraclejava.demodog.cart;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * 세션마다 하나씩 생기는 장바구니. 상품 id별 수량만 담고, 상품 정보는 조회할 때 DB에서 가져온다.
 * 같은 세션의 요청이 동시에 들어올 수 있어서 메서드를 동기화한다.
 */
@Component
@SessionScope
public class Cart implements Serializable {

	private final Map<Long, Integer> items = new LinkedHashMap<>();

	/** 이미 담긴 상품이면 수량을 더한다. */
	public synchronized void add(Long dogId, int quantity) {
		items.merge(dogId, quantity, Integer::sum);
	}

	public synchronized void remove(Long dogId) {
		items.remove(dogId);
	}

	/** 담은 순서대로 상품 id와 수량을 복사해서 돌려준다. */
	public synchronized Map<Long, Integer> getItems() {
		return new LinkedHashMap<>(items);
	}

}
