package com.reportingdashboard.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import com.reportingdashboard.api.exception.ResourceNotFoundException;
import com.reportingdashboard.application.ProfileQueryService;
import com.reportingdashboard.application.data.ArticleData;
import com.reportingdashboard.application.data.CommentData;
import com.reportingdashboard.application.data.ProfileData;
import com.reportingdashboard.core.user.User;
import com.reportingdashboard.graphql.DgsConstants.ARTICLE;
import com.reportingdashboard.graphql.DgsConstants.COMMENT;
import com.reportingdashboard.graphql.DgsConstants.QUERY;
import com.reportingdashboard.graphql.DgsConstants.USER;
import com.reportingdashboard.graphql.types.Article;
import com.reportingdashboard.graphql.types.Comment;
import com.reportingdashboard.graphql.types.Profile;
import com.reportingdashboard.graphql.types.ProfilePayload;
import java.util.Map;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ProfileDatafetcher {

  private ProfileQueryService profileQueryService;

  @DgsData(parentType = USER.TYPE_NAME, field = USER.Profile)
  public Profile getUserProfile(DataFetchingEnvironment dataFetchingEnvironment) {
    User user = dataFetchingEnvironment.getLocalContext();
    String username = user.getUsername();
    return queryProfile(username);
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Author)
  public Profile getAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
    Map<String, ArticleData> map = dataFetchingEnvironment.getLocalContext();
    Article article = dataFetchingEnvironment.getSource();
    return queryProfile(map.get(article.getSlug()).getProfileData().getUsername());
  }

  @DgsData(parentType = COMMENT.TYPE_NAME, field = COMMENT.Author)
  public Profile getCommentAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
    Comment comment = dataFetchingEnvironment.getSource();
    Map<String, CommentData> map = dataFetchingEnvironment.getLocalContext();
    return queryProfile(map.get(comment.getId()).getProfileData().getUsername());
  }

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Profile)
  public ProfilePayload queryProfile(
      @InputArgument("username") String username, DataFetchingEnvironment dataFetchingEnvironment) {
    Profile profile = queryProfile(dataFetchingEnvironment.getArgument("username"));
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  private Profile queryProfile(String username) {
    User current = SecurityUtil.getCurrentUser().orElse(null);
    ProfileData profileData =
        profileQueryService
            .findByUsername(username, current)
            .orElseThrow(ResourceNotFoundException::new);
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
