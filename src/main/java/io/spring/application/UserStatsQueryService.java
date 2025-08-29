package io.spring.application;

import io.spring.application.data.UserStatsData;
import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserStatsReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserStatsQueryService {
  private UserReadService userReadService;
  private UserStatsReadService userStatsReadService;

  @Cacheable(value = "userStats", key = "#username")
  public Optional<UserStatsData> findStatsByUsername(String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    }

    UserStatsData stats = new UserStatsData();
    stats.setUsername(userData.getUsername());
    stats.setTotalArticles(userStatsReadService.countArticlesByUserId(userData.getId()));
    stats.setFollowersCount(userStatsReadService.countFollowersByUserId(userData.getId()));
    stats.setFollowingCount(userStatsReadService.countFollowingByUserId(userData.getId()));
    stats.setFavoritesReceived(userStatsReadService.countFavoritesReceivedByUserId(userData.getId()));
    stats.setCreatedAt(userData.getCreatedAt());

    return Optional.of(stats);
  }
}
