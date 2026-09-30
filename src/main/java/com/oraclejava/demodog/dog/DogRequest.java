package com.oraclejava.demodog.dog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 상품 등록/수정 요청. id와 readcount는 서버에서 관리하고, image는 POST /dogs/{id}/image로 업로드하므로 받지 않는다.
 */
public record DogRequest(
		@NotBlank @Size(max = 100) String kind,
		@NotNull @PositiveOrZero Integer price,
		@Size(max = 100) String country,
		@PositiveOrZero Double height,
		@PositiveOrZero Double weight,
		@Size(max = 4000) String content) {
}
