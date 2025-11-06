package com.reportingdashboard.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import com.reportingdashboard.api.exception.ResourceNotFoundException;
import com.reportingdashboard.application.ProfileQueryService;
import com.reportingdashboard.application.data.ProfileData;
import com.reportingdashboard.core.user.FollowRelation;
import com.reportingdashboard.core.user.User;
import com.reportingdashboard.core.user.UserRepository;
import com.reportingdashboard.graphql.DgsConstants.MUTATION;
import com.reportingdashboard.graphql.exception.AuthenticationException;
import com.reportingdashboard.graphql.types.Profile;
import com.reportingdashboard.graphql.types.ProfilePayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class RelationMutation {

  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
  public ProfilePayload follow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return userRepository
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              userRepository.saveRelation(followRelation);
              Profile profile = buildProfile(username, user);
              return ProfilePayload.newBuilder().profile(profile).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
  public ProfilePayload unfollow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    return userRepository
        .findRelation(user.getId(), target.getId())
        .map(
            relation -> {
              userRepository.removeRelation(relation);
              Profile profile = buildProfile(username, user);
              return ProfilePayload.newBuilder().profile(profile).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Profile buildProfile(@InputArgument("username") String username, User current) {
    ProfileData profileData = profileQueryService.findByUsername(username, current).get();
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
