package io.spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("io.spring.infrastructure.mybatis")
public class RealWorldApplication {

  public static void main(String[] args) {
    SpringApplication.run(RealWorldApplication.class, args);
  }
}
