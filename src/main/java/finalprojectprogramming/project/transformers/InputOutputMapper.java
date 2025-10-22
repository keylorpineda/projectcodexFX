package finalprojectprogramming.project.transformers;

public interface InputOutputMapper<I, D, O> {
    D convertFromInput(I input);
    O convertToOutput(D dtoOrDomain);
}
