package com.example.springjwttest.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@Tag(name ="Admin API", description = "관리자 권환 관련 API")
public class AdminController {

    @GetMapping("/admin")
    public String adminP(){
        return "Admin Controller";
    }
}
