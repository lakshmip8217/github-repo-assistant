package com.githubrepoassistant.controller;

import com.githubrepoassistant.dto.RepositoryAnswerDto;
import com.githubrepoassistant.dto.RepositoryQuestionRequest;
import com.githubrepoassistant.service.RepositoryQuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github/repos")
public class RepositoryQuestionController {
    private static final String GITHUB_NAME = "[A-Za-z0-9_.-]+";
    private final RepositoryQuestionService questionService;

    public RepositoryQuestionController(RepositoryQuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/{owner}/{repository}/questions")
    public RepositoryAnswerDto ask(
            @PathVariable @Pattern(regexp = GITHUB_NAME) String owner,
            @PathVariable @Pattern(regexp = GITHUB_NAME) String repository,
            @Valid @RequestBody RepositoryQuestionRequest request) {
        return questionService.answer(owner, repository, request.question());
    }
}
