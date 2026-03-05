package com.hottakeranker.dto;

import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemographicFilterRequest {
	private Gender gender;
	private AgeGroup ageGroup;
	private Region region;
	private Ethnicity ethnicity;
	private ReligiousView religiousView;
	private PoliticalView politicalView;
	private RelationshipStatus relationshipStatus;

	public boolean hasFilters() {
		return gender != null || ageGroup != null || region != null || ethnicity != null
			|| religiousView != null || politicalView != null || relationshipStatus != null;
	}
}
