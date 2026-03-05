import api from './api';
import type { Topic, TopicResultResponse, DemographicFilter, ControversialTopicDto } from '../types/api';

export async function getActiveTopics(): Promise<Topic[]> {
  const res = await api.get<Topic[]>('/topics');
  return res.data;
}

export async function getVotedTopicIds(): Promise<number[]> {
  const res = await api.get<number[]>('/users/me/voted-topics');
  return res.data;
}

export async function getResults(topicId: number): Promise<TopicResultResponse> {
  const res = await api.get<TopicResultResponse>(`/topics/${topicId}/results`);
  return res.data;
}

export async function getControversialTopics(): Promise<ControversialTopicDto[]> {
  const res = await api.get<ControversialTopicDto[]>('/topics/controversial');
  return res.data;
}

export async function getDemographicResults(
  topicId: number,
  filter: DemographicFilter
): Promise<TopicResultResponse> {
  const params = new URLSearchParams();
  if (filter.gender) params.set('gender', filter.gender);
  if (filter.ageGroup) params.set('ageGroup', filter.ageGroup);
  if (filter.region) params.set('region', filter.region);
  if (filter.ethnicity) params.set('ethnicity', filter.ethnicity);
  if (filter.religiousView) params.set('religiousView', filter.religiousView);
  if (filter.politicalView) params.set('politicalView', filter.politicalView);
  if (filter.relationshipStatus) params.set('relationshipStatus', filter.relationshipStatus);
  const res = await api.get<TopicResultResponse>(
    `/topics/${topicId}/results/demographics?${params.toString()}`
  );
  return res.data;
}
