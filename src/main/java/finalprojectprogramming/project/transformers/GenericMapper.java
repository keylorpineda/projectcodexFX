package finalprojectprogramming.project.transformers;

public interface GenericMapper<E, D> {
    D convertToDTO(E entity);
    E convertToEntity(D dto);
}
