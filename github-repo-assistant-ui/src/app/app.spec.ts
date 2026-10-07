import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { Repository } from './github-repositories';

const repo: Repository = {
  id: 1, name: 'demo', fullName: 'octocat/demo', htmlUrl: 'https://github.com/octocat/demo',
  description: 'A public project', language: 'Java', stargazersCount: 12, forksCount: 3,
  defaultBranch: 'main', fork: false, archived: false,
};

describe('Repository explorer', () => {
  let http: HttpTestingController;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App], providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  function setup() {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    return fixture;
  }
  function expectPage(user = 'octocat', page = 1) {
    return http.expectOne(`/api/github/users/${user}/repos?page=${page}&per_page=30`);
  }

  it('submits the form and renders repository details and links', async () => {
    const fixture = setup();
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const input = element.querySelector('input')!;
    input.value = ' octocat ';
    input.dispatchEvent(new Event('input'));
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    expect(fixture.componentInstance.loading()).toBe(true);
    expectPage().flush([repo]);
    fixture.detectChanges();
    expect(element.querySelector('.repo-card')?.textContent).toContain('A public project');
    expect(element.querySelector('.repo-card a')?.getAttribute('href')).toBe(repo.htmlUrl);
    expect(element.textContent).not.toContain('Congratulations');
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('rejects invalid usernames without making requests', () => {
    const fixture = setup();
    fixture.componentInstance.username = 'invalid/user';
    fixture.componentInstance.search();
    expect(fixture.componentInstance.error()).toContain('valid GitHub username');
  });

  it('shows an empty state', () => {
    const fixture = setup();
    fixture.componentInstance.username = 'octocat';
    fixture.componentInstance.search();
    expectPage().flush([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No public repositories yet');
  });

  it.each([404, 503, 502, 0])('handles HTTP %s failures', status => {
    const fixture = setup();
    fixture.componentInstance.username = 'octocat';
    fixture.componentInstance.search();
    const request = expectPage();
    if (status === 0) request.error(new ProgressEvent('error'));
    else request.flush({}, { status, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('retains results after a pagination failure and retries the same page', () => {
    const fixture = setup();
    const app = fixture.componentInstance;
    app.username = 'octocat';
    app.search();
    expectPage().flush(Array.from({ length: 30 }, (_, i) => ({ ...repo, id: i })));
    app.loadMore();
    expectPage('octocat', 2).flush({}, { status: 502, statusText: 'Error' });
    expect(app.repositories()).toHaveLength(30);
    app.loadMore();
    expectPage('octocat', 2).flush([{ ...repo, id: 31 }]);
    expect(app.repositories()).toHaveLength(31);
    expect(app.hasMore()).toBe(false);
  });

  it('cancels an older search so stale results cannot replace the new user', () => {
    const fixture = setup();
    const app = fixture.componentInstance;
    app.username = 'octocat';
    app.search();
    const old = expectPage();
    app.username = 'lakshmip8217';
    app.search();
    expect(old.cancelled).toBe(true);
    expectPage('lakshmip8217').flush([]);
    expect(app.searchedUser()).toBe('lakshmip8217');
    expect(app.repositories()).toEqual([]);
  });
});
