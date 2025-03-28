package com.payment.gateway.Payment.Gateway.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping({"/","/home"})
    public ResponseEntity<String> greet(){
        return new ResponseEntity<>("Hellow", HttpStatus.OK);
    }
}
