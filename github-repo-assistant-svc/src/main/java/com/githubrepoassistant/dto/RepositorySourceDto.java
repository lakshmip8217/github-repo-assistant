package com.githubrepoassistant.dto;

public record RepositorySourceDto(
        String path,
        String url,
        String excerpt) {
}
