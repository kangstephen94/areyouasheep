package com.hottakeranker.entity;

import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"users\"")
public class User {

	public User() {}

	public User(String displayName, String email, String passwordHash, Gender gender, AgeGroup ageGroup, Region region, Ethnicity ethnicity) {
		this.displayName = displayName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.gender = gender;
		this.ageGroup = ageGroup;
		this.region = region;
		this.ethnicity = ethnicity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String displayName;

	@Column(unique = true)
	private String email;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	private AgeGroup ageGroup;

	@Enumerated(EnumType.STRING)
	private Region region;

	@Enumerated(EnumType.STRING)
	private Ethnicity ethnicity;

	@Enumerated(EnumType.STRING)
	private ReligiousView religiousView;

	@Enumerated(EnumType.STRING)
	private PoliticalView politicalView;

	@Enumerated(EnumType.STRING)
	private RelationshipStatus relationshipStatus;

	private int eloRating = 1000;

	private int streak = 0;

	private boolean admin = false;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
