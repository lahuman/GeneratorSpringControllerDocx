package lahuman.doc.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Application
 * 
 * @author daniel
 *
 */
@SpringBootApplication
@Slf4j
public class App {

  @RestController
  public static class ControllerA {
    @Autowired
    MyService myService;

    /**
     * show hello world
     * @param name
     * @param say
     * @return 
     */
    @GetMapping("/hello")
    public String getHello(@RequestParam(name = "name") String name,
        @RequestParam(name = "say") String say) {
      return myService.getHello() + " " + name + " " + say;
    }

    @PostMapping("/hello")
    public String addHello(@RequestBody HelloVO helloVO) {
      return myService.getHello() + " " + helloVO.getName();
    }

    @PutMapping("/hello")
    public String modiHello(String name) {
      return myService.getHello() + " " + name;
    }

    @DeleteMapping("/hello")
    public String delHello(String name) {
      return myService.getHello() + " " + name;
    }
  }

  @Service
  public static class MyService {
    public String getHello() {
      return "Hello";
    }
  }

  @Data
  public static class HelloVO {
    private String name;
    private String say;
  }

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
