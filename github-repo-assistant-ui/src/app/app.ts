import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GitHubRepositories, Repository } from './github-repositories';

@Component({
  imports: [FormsModule],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(GitHubRepositories);
  private request?: Subscription;
  username = '';
  readonly searchedUser = signal('');
  readonly repositories = signal<Repository[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly loaded = signal(false);
  readonly hasMore = signal(false);
  private page = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.request?.unsubscribe());
  }

  search() {
    const username = this.username.trim();
    this.request?.unsubscribe();
    this.loading.set(false);
    this.repositories.set([]);
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
