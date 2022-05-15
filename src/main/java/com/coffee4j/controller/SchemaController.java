package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.model.CreateSchema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schemas")
public final class SchemaController {
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestBody CreateSchema schema) {
        System.out.println(schema);

        return new ResponseEntity<>(HttpStatus.OK);
    } //create
}