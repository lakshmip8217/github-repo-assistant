# GitHub Repo Assistant UI

Search for a GitHub username to browse public repositories, descriptions,
languages, stars, and forks. Repository links open GitHub in a new tab.
Use **Load more repositories** to fetch additional pages of 30 results.

Select **Analyze this repository** on any repository card to ask a question. The
assistant indexes a bounded set of public README and source files for that
repository and shows the retrieved excerpts with links to their GitHub files.

## Local development

1. Start the Spring Boot service in `../github-repo-assistant-svc` with `./mvnw spring-boot:run` (port 8080).
2. In this directory, run `npm ci` if dependencies are not installed, then `npm start`.
3. Open http://localhost:4200 and enter a username such as `lakshmip8217`.

The development proxy in `proxy.conf.json` forwards `/api/**` to
`http://localhost:8080`, including `/api/github/users/{username}/repos`.
Restart `npm start` after changing proxy configuration.

## Verification and deployment

- `npm test -- --watch=false` runs the UI tests with mocked HTTP responses.
- `npm run build` creates the production build.
- For production, configure your web server to route `/api/**` to the Spring Boot
  service on the same origin. The development proxy does not apply to production
  or the standalone SSR server.
