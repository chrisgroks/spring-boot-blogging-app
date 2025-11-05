package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserStatsData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserStatsReadService {
  UserStatsData getUserStatsByUsername(@Param("username") String username);
}
