package finalprojectprogramming.project.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class GenericMapperFactoryTest {

    @Test
    void constructor_rejects_null_modelmapper() {
        assertThatThrownBy(() -> new GenericMapperFactory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ModelMapper cannot be null.");
    }

    @Test
    void creates_mappers_successfully() {
        GenericMapperFactory factory = new GenericMapperFactory(new ModelMapper());

        GenericMapper<Object, Object> mapper = factory.createMapper(Object.class, Object.class);
        InputOutputMapper<Object, Object, Object> ioMapper =
                factory.createInputOutputMapper(Object.class, Object.class, Object.class);

        assertThat(mapper).isNotNull();
        assertThat(ioMapper).isNotNull();
    }
}
