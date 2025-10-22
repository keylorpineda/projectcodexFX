package finalprojectprogramming.project.transformers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GenericMapperFactory {

    private final ModelMapper modelMapper;

    public GenericMapperFactory(ModelMapper modelMapper) {
        if (modelMapper == null) throw new IllegalArgumentException("ModelMapper cannot be null.");
        this.modelMapper = modelMapper;
    }

    public <E, D> GenericMapper<E, D> createMapper(Class<E> entityClass, Class<D> dtoClass) {
        return new GenericMapperImplementation<>(entityClass, dtoClass, modelMapper);
    }

    public <I, D, O> InputOutputMapper<I, D, O> createInputOutputMapper(
            Class<I> inputClass, Class<D> dtoOrDomainClass, Class<O> outputClass) {
        return new InputOutputMapperImplementation<>(inputClass, dtoOrDomainClass, outputClass, modelMapper);
    }
}
