package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleBookmarkCount;
import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleBookmarksReadService {
  boolean isUserBookmark(@Param("userId") String userId, @Param("articleId") String articleId);

  int articleBookmarkCount(@Param("articleId") String articleId);

  List<ArticleBookmarkCount> articlesBookmarkCount(@Param("ids") List<String> ids);

  Set<String> userBookmarks(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
