package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleSearchApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_search_articles_by_title() throws Exception {
    String query = "dragon";
    List<String> tagList = asList("reactjs", "angularjs");
    
    ArticleData articleData =
        new ArticleData(
            "123",
            "how-to-train-your-dragon",
            "How to train your dragon",
            "Ever wonder how?",
            "You have to believe",
            false,
            0,
            new DateTime(),
            new DateTime(),
            tagList,
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    List<ArticleData> articles = asList(articleData);
    ArticleDataList articleDataList = new ArticleDataList(articles, 1);

    when(articleQueryService.findArticlesBySearch(eq(query), any(Page.class), any()))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/search?q=" + query)
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(1))
        .body("articles[0].title", equalTo("How to train your dragon"))
        .body("articlesCount", equalTo(1));
  }

  @Test
  public void should_search_articles_with_pagination() throws Exception {
    String query = "test";
    
    when(articleQueryService.findArticlesBySearch(eq(query), any(Page.class), any()))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/search?q=" + query + "&offset=10&limit=5")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_return_empty_results_for_no_matches() throws Exception {
    String query = "nonexistent";
    
    when(articleQueryService.findArticlesBySearch(eq(query), any(Page.class), any()))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/search?q=" + query)
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0))
        .body("articlesCount", equalTo(0));
  }
}
