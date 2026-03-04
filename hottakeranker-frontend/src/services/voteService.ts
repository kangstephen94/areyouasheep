import api from './api';
import type { VoteRequest } from '../types/api';

export async function submitVote(data: VoteRequest): Promise<void> {
  await api.post('/votes', data);
}
