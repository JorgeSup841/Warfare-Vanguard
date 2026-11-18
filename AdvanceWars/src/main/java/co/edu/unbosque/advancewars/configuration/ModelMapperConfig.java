package co.edu.unbosque.advancewars.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Proporciona el bean de mapeo entre objetos, con coincidencias estrictas.
 */
@Configuration
public class ModelMapperConfig {
    /**
     * Crea un ModelMapper con estrategia estricta de correspondencia.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
