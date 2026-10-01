package com.oraclejava.demodog.member;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

/**
 * 세션 기반 회원 가입/로그인. 로그인하면 세션의 {@link #LOGIN_MEMBER} 속성에 회원 정보를 넣고,
 * 장바구니와 마찬가지로 세션 쿠키(JSESSIONID)로 로그인 상태를 구분한다.
 */
@RestController
@RequestMapping("/members")
public class MemberController {

	public static final String LOGIN_MEMBER = "loginMember";

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping("/signup")
	public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
		MemberResponse member = memberService.signup(request);
		return ResponseEntity.created(URI.create("/members/me")).body(member);
	}

	/** 로그인에 성공하면 세션 고정 공격을 막으려고 세션 id를 바꾼다. 장바구니 등 기존 세션 속성은 그대로 남는다. */
	@PostMapping("/login")
	public MemberResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		MemberResponse member = memberService.login(request);
		httpRequest.getSession().setAttribute(LOGIN_MEMBER, member);
		httpRequest.changeSessionId();
		return member;
	}

	/** 세션을 통째로 지우므로 장바구니도 비워진다. */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.noContent().build();
	}

	/** 현재 로그인한 회원. 로그인하지 않았으면 401. */
	@GetMapping("/me")
	public MemberResponse me(@SessionAttribute(name = LOGIN_MEMBER, required = false) MemberResponse member) {
		if (member == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
		}
		return member;
	}

}
