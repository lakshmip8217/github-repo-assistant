package com.githubrepoassistant.service;

import com.githubrepoassistant.dto.GitHubRepositoryDto;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GitHubRepositoryService {
    private final RestClient gitHubRestClient;

    public GitHubRepositoryService(RestClient gitHubRestClient) {
        this.gitHubRestClient = gitHubRestClient;
    }

    public List<GitHubRepositoryDto> getPublicRepositories(String username, int page, int perPage) {
        try {
            var repositories = gitHubRestClient.get()
                    .uri("/users/{username}/repos?type=owner&sort=full_name&direction=asc&page={page}&per_page={perPage}",
                            username, page, perPage)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepositoryDto>>() {});
            if (repositories == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub returned an empty response");
            }
            return repositories;
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GitHub user not found", exception);
            }
            if (status == 403 || status == 429) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "GitHub access is temporarily unavailable or rate limited", exception);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub request failed", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to retrieve GitHub repositories", exception);
        }
    }
}
