package finalprojectprogramming.project.transformers;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.getConfiguration()
          .setMatchingStrategy(MatchingStrategies.STRICT)
          .setAmbiguityIgnored(true)
          .setSkipNullEnabled(true);
        return mm;
    }
}
