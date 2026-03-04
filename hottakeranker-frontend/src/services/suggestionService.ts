import api from './api';
import type { TopicSuggestionRequest, TopicSuggestion } from '../types/api';

export async function suggest(data: TopicSuggestionRequest): Promise<TopicSuggestion> {
  const res = await api.post<TopicSuggestion>('/topics/suggest', data);
  return res.data;
}

export async function getSuggestions(): Promise<TopicSuggestion[]> {
  const res = await api.get<TopicSuggestion[]>('/topics/suggestions');
  return res.data;
}

export async function upvoteSuggestion(id: number): Promise<void> {
  await api.post(`/topics/suggestions/${id}/upvote`);
}
