package com.hottakeranker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottakeranker.entity.User;
import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.security.JwtAuthFilter;
import com.hottakeranker.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;

	@Test
	void register_valid_returns201() throws Exception {
		when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
		when(userRepository.save(any(User.class))).thenAnswer(inv -> {
			User u = inv.getArgument(0);
			u.setId(1L);
			return u;
		});

		Map<String, Object> request = Map.of(
				"displayName", "TestUser",
				"email", "test@example.com",
				"password", "password123",
				"gender", "MALE",
				"ageGroup", "AGE_18_24",
				"region", "NORTHEAST",
				"ethnicity", "WHITE"
		);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.displayName").value("TestUser"));
	}

	@Test
	void register_duplicateEmail_returns409() throws Exception {
		when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

		Map<String, Object> request = Map.of(
				"displayName", "TestUser",
				"email", "taken@example.com",
				"password", "password123",
				"gender", "MALE",
				"ageGroup", "AGE_18_24",
				"region", "NORTHEAST",
				"ethnicity", "WHITE"
		);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void login_validCredentials_returns200WithToken() throws Exception {
		User user = new User("TestUser", "test@example.com", "hashedpw",
				Gender.MALE, AgeGroup.AGE_18_24, Region.NORTHEAST, Ethnicity.WHITE);
		user.setId(1L);

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "hashedpw")).thenReturn(true);
		when(jwtTokenProvider.generateToken(1L, false)).thenReturn("jwt-token-123");

		Map<String, String> request = Map.of(
				"email", "test@example.com",
				"password", "password123"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwt-token-123"));
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		User user = new User("TestUser", "test@example.com", "hashedpw",
				Gender.MALE, AgeGroup.AGE_18_24, Region.NORTHEAST, Ethnicity.WHITE);
		user.setId(1L);

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongpassword", "hashedpw")).thenReturn(false);

		Map<String, String> request = Map.of(
				"email", "test@example.com",
				"password", "wrongpassword"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_unknownEmail_returns401() throws Exception {
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

		Map<String, String> request = Map.of(
				"email", "unknown@example.com",
				"password", "password123"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}
}
