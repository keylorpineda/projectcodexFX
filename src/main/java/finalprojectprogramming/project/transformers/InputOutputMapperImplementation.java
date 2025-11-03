package finalprojectprogramming.project.transformers;

import org.modelmapper.ModelMapper;

public class InputOutputMapperImplementation<I, D, O> implements InputOutputMapper<I, D, O> {

    private final ModelMapper modelMapper;
    private final Class<D> middleClass;
    @SuppressWarnings("unused")
    private final Class<I> inputClass;
    private final Class<O> outputClass;

    public InputOutputMapperImplementation(Class<I> inputClass, Class<D> middleClass,
            Class<O> outputClass, ModelMapper modelMapper) {
        if (inputClass == null || middleClass == null || outputClass == null || modelMapper == null) {
            throw new IllegalArgumentException("Mapping classes and modelMapper cannot be null.");
        }
        this.modelMapper = modelMapper;
        this.inputClass = inputClass;
        this.middleClass = middleClass;
        this.outputClass = outputClass;
    }

    @Override
    public D convertFromInput(I input) {
        if (input == null)
            throw new IllegalArgumentException("InputDTO cannot be null.");
        return modelMapper.map(input, middleClass);
    }

    @Override
    public O convertToOutput(D dtoOrDomain) {
        if (dtoOrDomain == null)
            throw new IllegalArgumentException("Source cannot be null.");
        return modelMapper.map(dtoOrDomain, outputClass);
    }
}
