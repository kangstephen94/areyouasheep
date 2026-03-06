import api from './api';
import type { Topic, AdminTopicRequest, TopicSuggestion } from '../types/api';
import type { TopicStatus } from '../types/enums';

export async function getAllTopics(): Promise<Topic[]> {
  const res = await api.get<Topic[]>('/admin/topics');
  return res.data;
}

export async function createTopic(data: AdminTopicRequest): Promise<Topic> {
  const res = await api.post<Topic>('/admin/topics', data);
  return res.data;
}

export async function updateTopicStatus(id: number, status: TopicStatus): Promise<Topic> {
  const res = await api.put<Topic>(`/admin/topics/${id}/status`, null, { params: { status } });
  return res.data;
}

export async function deleteTopic(id: number): Promise<void> {
  await api.delete(`/admin/topics/${id}`);
}

export async function getPendingSuggestions(): Promise<TopicSuggestion[]> {
  const res = await api.get<TopicSuggestion[]>('/admin/suggestions');
  return res.data;
}

export async function promoteSuggestion(id: number): Promise<Topic> {
  const res = await api.post<Topic>(`/admin/suggestions/${id}/promote`);
  return res.data;
}
