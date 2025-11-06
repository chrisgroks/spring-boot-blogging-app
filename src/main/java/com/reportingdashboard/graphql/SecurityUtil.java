package com.reportingdashboard.graphql;

import com.reportingdashboard.core.user.User;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
  public static Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return Optional.empty();
    }
    com.reportingdashboard.core.user.User currentUser = (com.reportingdashboard.core.user.User) authentication.getPrincipal();
    return Optional.of(currentUser);
  }
}
