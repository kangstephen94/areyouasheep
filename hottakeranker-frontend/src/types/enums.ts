export const Gender = {
  MALE: 'MALE',
  FEMALE: 'FEMALE',
  NON_BINARY: 'NON_BINARY',
  PREFER_NOT_TO_SAY: 'PREFER_NOT_TO_SAY',
} as const;
export type Gender = (typeof Gender)[keyof typeof Gender];

export const AgeGroup = {
  AGE_18_UNDER: 'AGE_18_UNDER',
  AGE_18_24: 'AGE_18_24',
  AGE_25_34: 'AGE_25_34',
  AGE_35_44: 'AGE_35_44',
  AGE_45_PLUS: 'AGE_45_PLUS',
} as const;
export type AgeGroup = (typeof AgeGroup)[keyof typeof AgeGroup];

export const Region = {
  NORTHEAST: 'NORTHEAST',
  SOUTH: 'SOUTH',
  MIDWEST: 'MIDWEST',
  WEST: 'WEST',
} as const;
export type Region = (typeof Region)[keyof typeof Region];

export const Ethnicity = {
  WHITE: 'WHITE',
  BLACK: 'BLACK',
  HISPANIC: 'HISPANIC',
  ASIAN: 'ASIAN',
  NATIVE_AMERICAN: 'NATIVE_AMERICAN',
  PACIFIC_ISLANDER: 'PACIFIC_ISLANDER',
  MULTIRACIAL: 'MULTIRACIAL',
  PREFER_NOT_TO_SAY: 'PREFER_NOT_TO_SAY',
} as const;
export type Ethnicity = (typeof Ethnicity)[keyof typeof Ethnicity];

export const TopicStatus = {
  PENDING: 'PENDING',
  ACTIVE: 'ACTIVE',
  ARCHIVED: 'ARCHIVED',
} as const;
export type TopicStatus = (typeof TopicStatus)[keyof typeof TopicStatus];

export const SuggestionStatus = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
export type SuggestionStatus = (typeof SuggestionStatus)[keyof typeof SuggestionStatus];

export const EloTier = {
  BRONZE: 'BRONZE',
  SILVER: 'SILVER',
  GOLD: 'GOLD',
  PLATINUM: 'PLATINUM',
  DIAMOND: 'DIAMOND',
} as const;
export type EloTier = (typeof EloTier)[keyof typeof EloTier];

export const GENDER_LABELS: Record<Gender, string> = {
  [Gender.MALE]: 'Male',
  [Gender.FEMALE]: 'Female',
  [Gender.NON_BINARY]: 'Non-Binary',
  [Gender.PREFER_NOT_TO_SAY]: 'Prefer Not to Say',
};

export const AGE_GROUP_LABELS: Record<AgeGroup, string> = {
  [AgeGroup.AGE_18_UNDER]: '1-18',
  [AgeGroup.AGE_18_24]: '18-24',
  [AgeGroup.AGE_25_34]: '25-34',
  [AgeGroup.AGE_35_44]: '35-44',
  [AgeGroup.AGE_45_PLUS]: '45+',
};

export const REGION_LABELS: Record<Region, string> = {
  [Region.NORTHEAST]: 'Northeast',
  [Region.SOUTH]: 'South',
  [Region.MIDWEST]: 'Midwest',
  [Region.WEST]: 'West',
};

export const ETHNICITY_LABELS: Record<Ethnicity, string> = {
  [Ethnicity.WHITE]: 'White',
  [Ethnicity.BLACK]: 'Black',
  [Ethnicity.HISPANIC]: 'Hispanic',
  [Ethnicity.ASIAN]: 'Asian',
  [Ethnicity.NATIVE_AMERICAN]: 'Native American',
  [Ethnicity.PACIFIC_ISLANDER]: 'Pacific Islander',
  [Ethnicity.MULTIRACIAL]: 'Multiracial',
  [Ethnicity.PREFER_NOT_TO_SAY]: 'Prefer Not to Say',
};
