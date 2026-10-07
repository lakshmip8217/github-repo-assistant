package com.githubrepoassistant.controller;

import com.githubrepoassistant.service.GitHubRepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GitHubRepositoryControllerTests {
    private MockRestServiceServer github;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://api.github.com");
        github = MockRestServiceServer.bindTo(builder).build();
        mvc = MockMvcBuilders.standaloneSetup(new GitHubRepositoryController(
                new GitHubRepositoryService(builder.build()))).build();
    }

    @Test
    void mapsGitHubResponseToJsonDtos() throws Exception {
        github.expect(requestTo(url(1, 30))).andRespond(withSuccess("""
                [{"id":1,"name":"demo","full_name":"octocat/demo",
                  "html_url":"https://github.com/octocat/demo","description":null,
                  "language":"Java","stargazers_count":12,"forks_count":3,
                  "default_branch":"main","fork":false,"archived":true,
                  "private":false,"unknown_field":"ignored"}]
                """, MediaType.APPLICATION_JSON));
        mvc.perform(get("/api/github/users/octocat/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("octocat/demo"))
                .andExpect(jsonPath("$[0].htmlUrl").value("https://github.com/octocat/demo"))
                .andExpect(jsonPath("$[0].stargazersCount").value(12))
                .andExpect(jsonPath("$[0].forksCount").value(3))
                .andExpect(jsonPath("$[0].defaultBranch").value("main"))
                .andExpect(jsonPath("$[0].archived").value(true))
                .andExpect(jsonPath("$[0].unknown_field").doesNotExist());
        github.verify();
    }

    @Test
    void forwardsPaginationAndReturnsEmptyList() throws Exception {
        github.expect(requestTo(url(2, 100))).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        mvc.perform(get("/api/github/users/octocat/repos?page=2&per_page=100"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        github.verify();
    }

    @ParameterizedTest
    @CsvSource({"404,404", "403,503", "429,503", "500,502", "401,502"})
    void mapsUpstreamErrors(int upstream, int expected) throws Exception {
        github.expect(requestTo(url(1, 30))).andRespond(withStatus(HttpStatus.valueOf(upstream)));
        mvc.perform(get("/api/github/users/octocat/repos")).andExpect(status().is(expected));
        github.verify();
    }

    @ParameterizedTest
    @CsvSource({"octocat,0,30", "octocat,1,101", "octocat,1,0", "invalid_user,1,30"})
    void rejectsInvalidParameters(String username, int page, int perPage) throws Exception {
        mvc.perform(get("/api/github/users/{username}/repos", username)
                        .param("page", String.valueOf(page)).param("per_page", String.valueOf(perPage)))
                .andExpect(status().isBadRequest());
        github.verify();
    }

    @Test
    void mapsConnectionFailure() throws Exception {
        github.expect(requestTo(url(1, 30))).andRespond(request -> {
            throw new java.io.IOException("Connection failed");
        });
        mvc.perform(get("/api/github/users/octocat/repos")).andExpect(status().isBadGateway());
        github.verify();
    }

    private String url(int page, int perPage) {
        return "https://api.github.com/users/octocat/repos?type=owner&sort=full_name&direction=asc&page="
                + page + "&per_page=" + perPage;
    }
}
