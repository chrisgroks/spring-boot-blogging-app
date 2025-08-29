package io.spring.infrastructure.repository;

import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.mapper.ArticleBookmarkMapper;
import io.spring.infrastructure.mybatis.readservice.ArticleBookmarksReadService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleBookmarkRepository implements ArticleBookmarkRepository {
  private ArticleBookmarkMapper mapper;
  private ArticleBookmarksReadService readService;

  @Autowired
  public MyBatisArticleBookmarkRepository(ArticleBookmarkMapper mapper, ArticleBookmarksReadService readService) {
    this.mapper = mapper;
    this.readService = readService;
  }

  @Override
  public void save(ArticleBookmark bookmark) {
    if (mapper.find(bookmark.getArticleId(), bookmark.getUserId()) == null) {
      mapper.insert(bookmark);
    }
  }

  @Override
  public Optional<ArticleBookmark> find(String articleId, String userId) {
    return Optional.ofNullable(mapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleBookmark bookmark) {
    mapper.delete(bookmark);
  }

  @Override
  public Set<String> userBookmarks(List<String> articleIds, User user) {
    return readService.userBookmarks(articleIds, user);
  }
}
