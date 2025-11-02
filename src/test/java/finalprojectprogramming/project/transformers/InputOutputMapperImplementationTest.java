package finalprojectprogramming.project.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class InputOutputMapperImplementationTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    void convertFromInput_createsIntermediateRepresentation() {
        InputOutputMapperImplementation<InputDto, DomainDto, OutputDto> mapper =
                new InputOutputMapperImplementation<>(InputDto.class, DomainDto.class, OutputDto.class, modelMapper);
        InputDto input = new InputDto();
        input.setName("Auditorium");
        input.setCapacity(120);

        DomainDto domain = mapper.convertFromInput(input);

        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo("Auditorium");
        assertThat(domain.getCapacity()).isEqualTo(120);
    }

    @Test
    void convertToOutput_createsOutputDtoFromDomain() {
        InputOutputMapperImplementation<InputDto, DomainDto, OutputDto> mapper =
                new InputOutputMapperImplementation<>(InputDto.class, DomainDto.class, OutputDto.class, modelMapper);
        DomainDto domain = new DomainDto();
        domain.setName("Studio");
        domain.setCapacity(12);

        OutputDto output = mapper.convertToOutput(domain);

        assertThat(output).isNotNull();
        assertThat(output.getName()).isEqualTo("Studio");
        assertThat(output.getCapacity()).isEqualTo(12);
    }

    @Test
    void convertMethods_rejectNullArguments() {
        InputOutputMapperImplementation<InputDto, DomainDto, OutputDto> mapper =
                new InputOutputMapperImplementation<>(InputDto.class, DomainDto.class, OutputDto.class, modelMapper);

        assertThatThrownBy(() -> mapper.convertFromInput(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("InputDTO cannot be null.");
        assertThatThrownBy(() -> mapper.convertToOutput(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source cannot be null.");
    }

    @Test
    void constructor_rejectsNullArguments() {
        assertThatThrownBy(() -> new InputOutputMapperImplementation<>(null, DomainDto.class, OutputDto.class, modelMapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
        assertThatThrownBy(() -> new InputOutputMapperImplementation<>(InputDto.class, null, OutputDto.class, modelMapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
        assertThatThrownBy(() -> new InputOutputMapperImplementation<>(InputDto.class, DomainDto.class, null, modelMapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
        assertThatThrownBy(() -> new InputOutputMapperImplementation<>(InputDto.class, DomainDto.class, OutputDto.class, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
    }

    static class InputDto {
        private String name;
        private int capacity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    static class DomainDto {
        private String name;
        private int capacity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    static class OutputDto {
        private String name;
        private int capacity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }
}
