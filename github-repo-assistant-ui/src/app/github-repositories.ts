import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface Repository {
  id: number;
  name: string;
  fullName: string;
  htmlUrl: string;
  description: string | null;
  language: string | null;
  stargazersCount: number;
  forksCount: number;
  defaultBranch: string;
  fork: boolean;
  archived: boolean;
}

export interface RepositorySource {
  path: string;
  url: string;
  excerpt: string;
}

export interface RepositoryAnswer {
  answer: string;
  sources: RepositorySource[];
  indexedFiles: number;
  indexedChunks: number;
  modelGenerated: boolean;
}

@Injectable({ providedIn: 'root' })
export class GitHubRepositories {
  private readonly http = inject(HttpClient);

  list(username: string, page: number) {
    return this.http.get<Repository[]>(`/api/github/users/${encodeURIComponent(username)}/repos`, {
      params: { page, per_page: 30 },
      timeout: 20000,
    });
  }

  askRepository(owner: string, repository: string, question: string) {
    return this.http.post<RepositoryAnswer>(
      `/api/github/repos/${encodeURIComponent(owner)}/${encodeURIComponent(repository)}/questions`,
      { question },
      { timeout: 60000 },
    );
  }
}
