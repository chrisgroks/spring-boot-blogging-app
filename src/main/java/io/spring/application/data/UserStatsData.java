package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsData {
  private String username;
  private Integer totalArticles;
  private Integer followersCount;
  private Integer followingCount;
  private Integer favoritesReceived;
  private DateTime createdAt;
}
