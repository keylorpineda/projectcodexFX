package finalprojectprogramming.project.services.user;

import finalprojectprogramming.project.dtos.UserInputDTO;
import finalprojectprogramming.project.dtos.UserOutputDTO;
import java.util.List;

public interface UserService {
    UserOutputDTO create(UserInputDTO inputDTO);

    UserOutputDTO update(Long id, UserInputDTO inputDTO);

    UserOutputDTO findById(Long id);

    List<UserOutputDTO> findAll();

    void delete(Long id);
}