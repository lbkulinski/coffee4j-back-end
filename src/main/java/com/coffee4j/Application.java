package com.coffee4j;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

/**
 * An instance of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 1, 2022
 */
@SpringBootApplication
public class Application {
    /**
     * Runs an instance of the Coffee4j application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    } //main
}