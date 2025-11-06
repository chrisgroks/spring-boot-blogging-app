package com.reportingdashboard.application.tag;

import com.reportingdashboard.application.TagsQueryService;
import com.reportingdashboard.core.article.Article;
import com.reportingdashboard.core.article.ArticleRepository;
import com.reportingdashboard.infrastructure.DbTestBase;
import com.reportingdashboard.infrastructure.repository.MyBatisArticleRepository;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({TagsQueryService.class, MyBatisArticleRepository.class})
public class TagsQueryServiceTest extends DbTestBase {
  @Autowired private TagsQueryService tagsQueryService;

  @Autowired private ArticleRepository articleRepository;

  @Test
  public void should_get_all_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("java"), "123"));
    Assertions.assertTrue(tagsQueryService.allTags().contains("java"));
  }
}
