package com.githubrepoassistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepositoryDto(
        long id,
        String name,
        @JsonAlias("full_name") String fullName,
        @JsonAlias("html_url") String htmlUrl,
        String description,
        String language,
        @JsonAlias("stargazers_count") int stargazersCount,
        @JsonAlias("forks_count") int forksCount,
        @JsonAlias("default_branch") String defaultBranch,
        boolean fork,
        boolean archived) {
}
