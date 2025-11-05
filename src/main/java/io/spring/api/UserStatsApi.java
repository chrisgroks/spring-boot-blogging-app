package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserStatsQueryService;
import io.spring.application.data.UserStatsData;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users/{username}/stats")
@AllArgsConstructor
public class UserStatsApi {
  private UserStatsQueryService userStatsQueryService;

  @GetMapping
  public ResponseEntity getUserStats(@PathVariable("username") String username) {
    return userStatsQueryService
        .getUserStats(username)
        .map(this::statsResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ResponseEntity statsResponse(UserStatsData stats) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("stats", stats);
          }
        });
  }
}
