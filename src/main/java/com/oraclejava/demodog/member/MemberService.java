package com.oraclejava.demodog.member;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	/** 일반 회원(USER)으로 가입한다. 이미 있는 아이디면 409. */
	@Transactional
	public MemberResponse signup(SignupRequest request) {
		if (memberRepository.existsByUsername(request.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + request.username());
		}
		Member member = memberRepository.save(new Member(request.username(), request.password(), Role.USER));
		return MemberResponse.of(member);
	}

	/** 아이디가 없거나 비밀번호가 틀리면 어느 쪽인지 알리지 않고 401. */
	public MemberResponse login(LoginRequest request) {
		return memberRepository.findByUsername(request.username())
				.filter(member -> member.matchesPassword(request.password()))
				.map(MemberResponse::of)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
	}

}
