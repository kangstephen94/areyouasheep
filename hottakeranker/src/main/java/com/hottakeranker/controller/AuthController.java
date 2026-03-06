package com.hottakeranker.controller;

import com.hottakeranker.dto.LoginRequest;
import com.hottakeranker.dto.RegisterRequest;
import com.hottakeranker.entity.User;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
		}

		User user = new User(
			request.getDisplayName(),
			request.getEmail(),
			passwordEncoder.encode(request.getPassword()),
			request.getGender(),
			request.getAgeGroup(),
			request.getRegion(),
			request.getEthnicity()
		);
		user.setReligiousView(request.getReligiousView());
		user.setPoliticalView(request.getPoliticalView());
		user.setRelationshipStatus(request.getRelationshipStatus());

		userRepository.save(user);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
			"id", user.getId(),
			"displayName", user.getDisplayName()
		));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElse(null);

		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
		}

		String token = jwtTokenProvider.generateToken(user.getId(), user.isAdmin());
		return ResponseEntity.ok(Map.of("token", token));
	}
}
