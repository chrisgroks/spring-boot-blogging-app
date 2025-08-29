package io.spring.application;

import io.spring.application.data.UserStatsData;
import io.spring.infrastructure.mybatis.readservice.UserStatsReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserStatsQueryService {
  private UserStatsReadService userStatsReadService;

  @Cacheable("userStats")
  public Optional<UserStatsData> getUserStats(String username) {
    return Optional.ofNullable(userStatsReadService.getUserStatsByUsername(username));
  }
}
