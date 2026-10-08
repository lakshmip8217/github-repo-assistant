import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GitHubRepositories, Repository, RepositoryAnswer } from './github-repositories';

@Component({
  imports: [FormsModule],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(GitHubRepositories);
  private request?: Subscription;
  private questionRequest?: Subscription;
  username = '';
  readonly searchedUser = signal('');
  readonly repositories = signal<Repository[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly loaded = signal(false);
  readonly hasMore = signal(false);
  readonly selectedRepository = signal<Repository | null>(null);
  readonly question = signal('');
  readonly answer = signal<RepositoryAnswer | null>(null);
  readonly asking = signal(false);
  readonly questionError = signal('');
  private page = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.request?.unsubscribe();
      this.questionRequest?.unsubscribe();
    });
  }

  search() {
    const username = this.username.trim();
    this.request?.unsubscribe();
    this.loading.set(false);
    this.repositories.set([]);
    this.closeAssistant();
    this.loaded.set(false);
    this.hasMore.set(false);
    this.searchedUser.set('');
    this.page = 0;
    if (!/^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$/.test(username)) {
      this.error.set('Enter a valid GitHub username (1–39 letters, numbers, or hyphens).');
      return;
    }
    this.username = username;
    this.searchedUser.set(username);
    this.fetchPage(1);
  }

  loadMore() {
    if (!this.loading() && this.hasMore()) this.fetchPage(this.page + 1);
  }

  selectRepository(repository: Repository) {
    this.selectedRepository.set(repository);
    this.question.set('What does this repository do?');
    this.answer.set(null);
    this.questionError.set('');
    queueMicrotask(() => document.getElementById('repository-assistant')?.scrollIntoView?.({
      behavior: 'smooth', block: 'start',
    }));
  }

  closeAssistant() {
    this.questionRequest?.unsubscribe();
    this.selectedRepository.set(null);
    this.question.set('');
    this.answer.set(null);
    this.questionError.set('');
    this.asking.set(false);
  }

  askQuestion() {
    const repository = this.selectedRepository();
    const question = this.question().trim();
    if (!repository || !question) return;
    this.questionRequest?.unsubscribe();
    this.asking.set(true);
    this.questionError.set('');
    this.answer.set(null);
    const [owner, name] = repository.fullName.split('/', 2);
    this.questionRequest = this.api.askRepository(owner, name, question).subscribe({
      next: answer => {
        this.answer.set(answer);
        this.asking.set(false);
      },
      error: error => {
        this.asking.set(false);
        this.questionError.set(error.status === 404
          ? 'This repository could not be found on GitHub.'
          : error.status === 503 || error.status === 429
            ? 'GitHub is temporarily unavailable or rate limited. Please try again shortly.'
            : 'Unable to index this repository. Check that the service is running and try again.');
      },
    });
  }

  private fetchPage(page: number) {
    this.error.set('');
    this.loading.set(true);
    this.request = this.api.list(this.searchedUser(), page).subscribe({
      next: (repositories) => {
        this.repositories.update(current => [...current, ...repositories]);
        this.page = page;
        this.hasMore.set(repositories.length === 30);
        this.loaded.set(true);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(error.status === 404
          ? `GitHub user “${this.searchedUser()}” was not found. Check the spelling and try again.`
          : error.status === 503 || error.status === 429
            ? 'GitHub is temporarily unavailable or rate limited. Please try again later.'
            : 'Unable to load repositories. Check that the service is running and try again.');
      },
    });
  }
}
