package hbnu.project.zhiyanmessgae.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "Message Service is running on 8088";
    }
}