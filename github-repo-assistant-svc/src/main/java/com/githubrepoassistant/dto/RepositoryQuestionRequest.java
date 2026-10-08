package com.githubrepoassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RepositoryQuestionRequest(
        @NotBlank @Size(max = 500) String question) {
}
