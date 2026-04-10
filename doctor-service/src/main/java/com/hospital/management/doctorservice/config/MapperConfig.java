package com.hospital.management.doctorservice.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        return new ModelMapper();
    }
}


    // We can also use this in the DcotorserviceApplication because by default it is configuaration class
    // We just need to inject the bean, what is there below
//    @Bean
//    public ModelMapper modelMapper() {
//
//        return new ModelMapper();
//    }
