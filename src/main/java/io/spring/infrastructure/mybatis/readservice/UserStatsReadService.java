package io.spring.infrastructure.mybatis.readservice;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserStatsReadService {

  Integer countArticlesByUserId(@Param("userId") String userId);

  Integer countFollowersByUserId(@Param("userId") String userId);

  Integer countFollowingByUserId(@Param("userId") String userId);

  Integer countFavoritesReceivedByUserId(@Param("userId") String userId);
}
