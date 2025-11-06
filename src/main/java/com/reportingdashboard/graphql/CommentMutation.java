package com.reportingdashboard.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import com.reportingdashboard.api.exception.NoAuthorizationException;
import com.reportingdashboard.api.exception.ResourceNotFoundException;
import com.reportingdashboard.application.CommentQueryService;
import com.reportingdashboard.application.data.CommentData;
import com.reportingdashboard.core.article.Article;
import com.reportingdashboard.core.article.ArticleRepository;
import com.reportingdashboard.core.comment.Comment;
import com.reportingdashboard.core.comment.CommentRepository;
import com.reportingdashboard.core.service.AuthorizationService;
import com.reportingdashboard.core.user.User;
import com.reportingdashboard.graphql.DgsConstants.MUTATION;
import com.reportingdashboard.graphql.exception.AuthenticationException;
import com.reportingdashboard.graphql.types.CommentPayload;
import com.reportingdashboard.graphql.types.DeletionStatus;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(body, user.getId(), article.getId());
    commentRepository.save(comment);
    CommentData commentData =
        commentQueryService
            .findById(comment.getId(), user)
            .orElseThrow(ResourceNotFoundException::new);
    return DataFetcherResult.<CommentPayload>newResult()
        .localContext(commentData)
        .data(CommentPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteComment)
  public DeletionStatus removeComment(
      @InputArgument("slug") String slug, @InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);

    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user, article, comment)) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return DeletionStatus.newBuilder().success(true).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
