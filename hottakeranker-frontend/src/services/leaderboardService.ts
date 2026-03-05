import api from './api';
import type { LeaderboardResponse, EloHistoryResponse, DemographicFilter, VoteHistoryEntry } from '../types/api';

export async function getLeaderboard(filter?: DemographicFilter): Promise<LeaderboardResponse> {
  const params = new URLSearchParams();
  if (filter?.gender) params.set('gender', filter.gender);
  if (filter?.ageGroup) params.set('ageGroup', filter.ageGroup);
  if (filter?.region) params.set('region', filter.region);
  if (filter?.ethnicity) params.set('ethnicity', filter.ethnicity);
  if (filter?.religiousView) params.set('religiousView', filter.religiousView);
  if (filter?.politicalView) params.set('politicalView', filter.politicalView);
  if (filter?.relationshipStatus) params.set('relationshipStatus', filter.relationshipStatus);
  const query = params.toString();
  const res = await api.get<LeaderboardResponse>(`/leaderboard${query ? `?${query}` : ''}`);
  return res.data;
}

export async function getEloHistory(): Promise<EloHistoryResponse> {
  const res = await api.get<EloHistoryResponse>('/users/me/elo-history');
  return res.data;
}

export async function getVoteHistory(): Promise<VoteHistoryEntry[]> {
  const res = await api.get<VoteHistoryEntry[]>('/users/me/votes');
  return res.data;
}
