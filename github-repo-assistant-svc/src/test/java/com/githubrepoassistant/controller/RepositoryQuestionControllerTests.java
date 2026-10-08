package com.githubrepoassistant.controller;

import com.githubrepoassistant.service.RepositoryQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpMethod.GET;

class RepositoryQuestionControllerTests {
    private MockRestServiceServer github;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://api.github.com");
        github = MockRestServiceServer.bindTo(builder).build();
        mvc = MockMvcBuilders.standaloneSetup(new RepositoryQuestionController(
                new RepositoryQuestionService(builder.build()))).build();
    }

    @Test
    void indexesPublicFilesAndReturnsGroundedSources() throws Exception {
        github.expect(requestTo("https://api.github.com/repos/octocat/demo"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"default_branch\":\"main\"}", MediaType.APPLICATION_JSON));
        github.expect(requestTo("https://api.github.com/repos/octocat/demo/git/trees/main?recursive=1"))
                .andRespond(withSuccess("""
                        {"tree":[
                          {"path":"README.md","type":"blob","size":100},
                          {"path":"src/Main.java","type":"blob","size":90},
                          {"path":"archive.zip","type":"blob","size":5}
                        ]}
                        """, MediaType.APPLICATION_JSON));
        github.expect(requestTo("https://api.github.com/repos/octocat/demo/contents/README.md?ref=main"))
                .andRespond(withSuccess("""
                        {"encoding":"base64","content":"VGhpcyBhc3Npc3RhbnQgaW5kZXhlcyBHaXRIdWIgcmVwb3NpdG9yaWVzIGFuZCBhbnN3ZXJzIHF1ZXN0aW9ucy4="}
                        """, MediaType.APPLICATION_JSON));
        github.expect(requestTo("https://api.github.com/repos/octocat/demo/contents/src%2FMain.java?ref=main"))
                .andRespond(withSuccess("""
                        {"encoding":"base64","content":"Y2xhc3MgTWFpbiB7IH0="}
                        """, MediaType.APPLICATION_JSON));

        mvc.perform(post("/api/github/repos/octocat/demo/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What does the assistant index?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexedFiles").value(2))
                .andExpect(jsonPath("$.indexedChunks").value(2))
                .andExpect(jsonPath("$.sources[0].path").value("README.md"))
                .andExpect(jsonPath("$.sources[0].url").value("https://github.com/octocat/demo/blob/main/README.md"))
                .andExpect(jsonPath("$.sources[0].excerpt").value(org.hamcrest.Matchers.containsString("indexes GitHub repositories")));
        github.verify();
    }

    @Test
    void rejectsAnEmptyQuestion() throws Exception {
        mvc.perform(post("/api/github/repos/octocat/demo/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest());
        github.verify();
    }
}
