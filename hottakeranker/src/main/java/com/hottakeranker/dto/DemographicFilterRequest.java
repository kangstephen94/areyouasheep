package com.hottakeranker.dto;

import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.Region;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemographicFilterRequest {
	private Gender gender;
	private AgeGroup ageGroup;
	private Region region;
	private Ethnicity ethnicity;

	public boolean hasFilters() {
		return gender != null || ageGroup != null || region != null || ethnicity != null;
	}
}
