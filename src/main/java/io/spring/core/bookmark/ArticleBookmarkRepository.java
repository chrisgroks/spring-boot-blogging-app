package io.spring.core.bookmark;

import io.spring.core.user.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArticleBookmarkRepository {
  void save(ArticleBookmark bookmark);

  Optional<ArticleBookmark> find(String articleId, String userId);

  void remove(ArticleBookmark bookmark);

  Set<String> userBookmarks(List<String> articleIds, User user);
}
