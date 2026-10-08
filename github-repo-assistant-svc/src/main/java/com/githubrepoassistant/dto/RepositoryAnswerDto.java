package com.githubrepoassistant.dto;

import java.util.List;

public record RepositoryAnswerDto(
        String answer,
        List<RepositorySourceDto> sources,
        int indexedFiles,
        int indexedChunks,
        boolean modelGenerated) {
}
