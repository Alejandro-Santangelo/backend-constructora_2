package com.rodrigo.construccion.config;

import com.rodrigo.construccion.dto.mapper.EmpresaMapper;
import com.rodrigo.construccion.dto.mapper.EmpresaMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {

    @Bean
    public EmpresaMapper empresaMapper() {
        return new EmpresaMapperImpl();
    }
}
