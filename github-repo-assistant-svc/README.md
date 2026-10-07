# GitHub repository API

Run the service with Java 17 or later:

```sh
./mvnw spring-boot:run
```

## List public repositories

```sh
curl 'http://localhost:8080/api/github/users/octocat/repos?page=1&per_page=30'
```

`GET /api/github/users/{username}/repos` calls GitHub's public user repository
endpoint and returns a JSON array of DTOs. No token is required. Results include
forks and archived repositories and are sorted by full name in ascending order.

Optional query parameters:

- `page`: page number, starting at 1 (default: 1).
- `per_page`: repositories per page, from 1 to 100 (default: 30).

Each request returns one page; request subsequent pages until an empty array is
returned to retrieve all repositories.

Example response:

```json
[
  {
    "id": 123,
    "name": "demo",
    "fullName": "octocat/demo",
    "htmlUrl": "https://github.com/octocat/demo",
    "description": "Example repository",
    "language": "Java",
    "stargazersCount": 12,
    "forksCount": 3,
    "defaultBranch": "main",
    "fork": false,
    "archived": false
  }
]
```

`description` and `language` may be null. Users without public repositories return
`200` with `[]`. Invalid input returns `400`, unknown users return `404`, GitHub
access restrictions or rate limits return `503`, and other upstream failures
(including connection failures and timeouts) return `502`. Errors use Spring's
Problem Details JSON responses.

The GitHub base URL is configured with `github.api.base-url` (default:
`https://api.github.com`). Requests have a 5-second connect timeout and a
10-second read timeout. Unauthenticated requests are subject to GitHub rate limits.

Contract reference: [GitHub REST API: list repositories for a user](https://docs.github.com/en/rest/repos/repos#list-repositories-for-a-user).

Run automated tests with `./mvnw test`. Tests mock GitHub and require no token or
live GitHub connection.
