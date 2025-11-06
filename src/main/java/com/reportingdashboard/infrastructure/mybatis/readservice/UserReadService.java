package com.reportingdashboard.infrastructure.mybatis.readservice;

import com.reportingdashboard.application.data.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReadService {

  UserData findByUsername(@Param("username") String username);

  UserData findById(@Param("id") String id);
}
