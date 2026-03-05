package com.hottakeranker.repository;

import com.hottakeranker.entity.User;
import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	List<User> findTop50ByOrderByEloRatingDesc();
	List<User> findTop50ByRegionOrderByEloRatingDesc(Region region);
	List<User> findTop50ByGenderOrderByEloRatingDesc(Gender gender);
	List<User> findTop50ByAgeGroupOrderByEloRatingDesc(AgeGroup ageGroup);
	List<User> findTop50ByEthnicityOrderByEloRatingDesc(Ethnicity ethnicity);
	List<User> findTop50ByReligiousViewOrderByEloRatingDesc(ReligiousView religiousView);
	List<User> findTop50ByPoliticalViewOrderByEloRatingDesc(PoliticalView politicalView);
	List<User> findTop50ByRelationshipStatusOrderByEloRatingDesc(RelationshipStatus relationshipStatus);
}
