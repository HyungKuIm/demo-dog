package com.oraclejava.demodog.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 관리자 계정(role = ADMIN)이 미리 들어 있도록, 시작할 때 없으면 한 번 넣어 둔다.
 * 회원 가입으로는 관리자를 만들 수 없다.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

	private final MemberRepository memberRepository;

	private final String username;

	private final String password;

	public AdminInitializer(MemberRepository memberRepository,
			@Value("${demodog.admin.username:admin}") String username,
			@Value("${demodog.admin.password:admin1234}") String password) {
		this.memberRepository = memberRepository;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!memberRepository.existsByUsername(username)) {
			memberRepository.save(new Member(username, password, Role.ADMIN));
		}
	}

}
