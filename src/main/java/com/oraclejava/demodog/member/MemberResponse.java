package com.oraclejava.demodog.member;

import java.io.Serializable;

/**
 * 비밀번호를 뺀 회원 정보. 로그인하면 이 값을 세션에 넣어 두고 로그인 회원으로 쓴다.
 */
public record MemberResponse(Long id, String username, Role role) implements Serializable {

	public static MemberResponse of(Member member) {
		return new MemberResponse(member.getId(), member.getUsername(), member.getRole());
	}

}
