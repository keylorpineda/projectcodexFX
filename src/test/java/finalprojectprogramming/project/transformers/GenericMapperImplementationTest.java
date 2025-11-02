package finalprojectprogramming.project.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class GenericMapperImplementationTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    void convertToDTO_mapsEntityFieldsToDto() {
        GenericMapperImplementation<DummyEntity, DummyDto> mapper =
                new GenericMapperImplementation<>(DummyEntity.class, DummyDto.class, modelMapper);
        DummyEntity entity = new DummyEntity();
        entity.setName("Conference Room");
        entity.setCapacity(25);

        DummyDto dto = mapper.convertToDTO(entity);

        assertThat(dto.getName()).isEqualTo("Conference Room");
        assertThat(dto.getCapacity()).isEqualTo(25);
    }

    @Test
    void convertToEntity_mapsDtoFieldsToEntity() {
        GenericMapperImplementation<DummyEntity, DummyDto> mapper =
                new GenericMapperImplementation<>(DummyEntity.class, DummyDto.class, modelMapper);
        DummyDto dto = new DummyDto();
        dto.setName("Lecture Hall");
        dto.setCapacity(80);

        DummyEntity entity = mapper.convertToEntity(dto);

        assertThat(entity.getName()).isEqualTo("Lecture Hall");
        assertThat(entity.getCapacity()).isEqualTo(80);
    }

    @Test
    void constructor_rejectsNullArguments() {
        assertThatThrownBy(() -> new GenericMapperImplementation<>(null, DummyDto.class, modelMapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
        assertThatThrownBy(() -> new GenericMapperImplementation<>(DummyEntity.class, null, modelMapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
        assertThatThrownBy(() -> new GenericMapperImplementation<>(DummyEntity.class, DummyDto.class, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapping classes and modelMapper cannot be null.");
    }

    static class DummyEntity {
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

    static class DummyDto {
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
