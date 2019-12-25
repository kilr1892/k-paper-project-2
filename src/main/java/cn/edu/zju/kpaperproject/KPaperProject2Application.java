package cn.edu.zju.kpaperproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.zju.kpaperproject.mapper")
public class KPaperProject2Application {

    public static void main(String[] args) {
        SpringApplication.run(KPaperProject2Application.class, args);
    }

}
