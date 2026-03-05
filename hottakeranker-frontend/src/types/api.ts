import { Gender, AgeGroup, Region, Ethnicity, ReligiousView, PoliticalView, RelationshipStatus, TopicStatus, SuggestionStatus } from './enums';

// Auth
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface RegisterRequest {
  displayName: string;
  email: string;
  password: string;
  gender: Gender;
  ageGroup: AgeGroup;
  region: Region;
  ethnicity: Ethnicity;
  religiousView?: ReligiousView;
  politicalView?: PoliticalView;
  relationshipStatus?: RelationshipStatus;
}

export interface RegisterResponse {
  id: number;
  displayName: string;
}

// Topics
export interface Topic {
  id: number;
  question: string;
  category: string;
  options: string[];
  status: TopicStatus;
  activeDate: string;
  createdAt: string;
}

// Votes
export interface VoteRequest {
  userId: number;
  topicId: number;
  ranking: number[];
}

// Results
export interface TopicResultResponse {
  topicId: number;
  question: string;
  crowdRanking: string[];
  totalVotes: number;
  scores: Record<string, number>;
}

export interface ControversialTopicDto {
  topicId: number;
  question: string;
  category: string;
  options: string[];
  totalVotes: number;
  controversyScore: number;
}

export interface DemographicFilter {
  gender?: Gender;
  ageGroup?: AgeGroup;
  region?: Region;
  ethnicity?: Ethnicity;
  religiousView?: ReligiousView;
  politicalView?: PoliticalView;
  relationshipStatus?: RelationshipStatus;
}

// Leaderboard
export interface LeaderboardEntryDto {
  rank: number;
  displayName: string;
  eloRating: number;
  totalVotes: number;
}

export interface LeaderboardResponse {
  leaderboard: LeaderboardEntryDto[];
  totalUsers: number;
}

// Elo History
export interface EloHistoryEntryDto {
  topicId: number;
  question: string;
  eloChange: number;
  similarityScore: number;
  date: string;
}

export interface EloHistoryResponse {
  currentElo: number;
  history: EloHistoryEntryDto[];
}

// Vote History
export interface VoteHistoryEntry {
  topicId: number;
  question: string;
  category: string;
  votedAt: string;
}

// Suggestions
export interface TopicSuggestionRequest {
  question: string;
  category: string;
  options: string[];
}

export interface TopicSuggestion {
  id: number;
  userId: number;
  question: string;
  category: string;
  options: string[];
  status: SuggestionStatus;
  upvotes: number;
  createdAt: string;
}
