package com.oraclejava.demodog.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청. role은 받지 않고 항상 USER로 가입한다.
 */
public record SignupRequest(
		@NotBlank @Size(min = 4, max = 50) String username,
		@NotBlank @Size(min = 4, max = 100) String password) {
}
