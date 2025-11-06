package com.reportingdashboard.core.service;

import com.reportingdashboard.core.article.Article;
import com.reportingdashboard.core.comment.Comment;
import com.reportingdashboard.core.user.User;

public class AuthorizationService {
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  public static boolean canWriteComment(User user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
