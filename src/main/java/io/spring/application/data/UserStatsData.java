package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsData {
  private int totalArticles;
  private int followersCount;
  private int followingCount;
  private int totalFavoritesReceived;
  private String createdAt;
}
