package com.example.dongxin.controller;


import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {


    @RequestMapping("/test")
    public String test(){
        return "hello world";
    }

    @GetMapping("/get")
    public String upload(@RequestParam String json) {
        System.out.println(json);
        return "get success";
    }

    @PostMapping("/post")
    public String post(@RequestBody String json) {
        System.out.println(json);
        return "post success";
    }


    public static void main(String[] args) {
        String s = "ASInfo:  15121  212  10;  DX18:  101  50  0  23.2  50.6  10;  DX18:  102  50  0  22.8  52.7  10;  ……  DX28:  103  50  0  21.8  10;  DX28:  104  50  0  22.2  10;  ……  DX38:  105  50  0  0030  10;  DX38:  106  50  0  0040  10;  ……  DX48:  107  50  0  0035  10;  DX48:  108  50  0  0045  10;  ……  DX49:  109  50  0  0057  10;  DX49:  110  50  0  0065  10;  ……;  AEInfo  ";
        int length = s.getBytes().length;
        System.out.println(length);
    }
}
