package com.githubrepoassistant.controller;

import com.githubrepoassistant.dto.GitHubRepositoryDto;
import com.githubrepoassistant.service.GitHubRepositoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github/users")
public class GitHubRepositoryController {
    private final GitHubRepositoryService repositoryService;

    public GitHubRepositoryController(GitHubRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping("/{username}/repos")
    public List<GitHubRepositoryDto> getRepositories(
            @PathVariable @Pattern(regexp = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?") String username,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "per_page", defaultValue = "30") @Min(1) @Max(100) int perPage) {
        return repositoryService.getPublicRepositories(username, page, perPage);
    }
}
