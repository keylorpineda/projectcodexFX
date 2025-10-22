package finalprojectprogramming.project.services.user;

import finalprojectprogramming.project.dtos.UserInputDTO;
import finalprojectprogramming.project.dtos.UserOutputDTO;
import finalprojectprogramming.project.models.AuditLog;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.transformers.GenericMapperFactory;
import finalprojectprogramming.project.transformers.InputOutputMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final InputOutputMapper<UserInputDTO, User, UserOutputDTO> userMapper;
    private final ModelMapper modelMapper;

    public UserServiceImplementation(UserRepository userRepository, GenericMapperFactory mapperFactory,
            ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userMapper = mapperFactory.createInputOutputMapper(UserInputDTO.class, User.class, UserOutputDTO.class);
        this.modelMapper = modelMapper;
    }

    @Override
    public UserOutputDTO create(UserInputDTO inputDTO) {
        SecurityUtils.requireAny(UserRole.ADMIN);
        validateUniqueEmail(inputDTO.getEmail(), null);
        User user = userMapper.convertFromInput(inputDTO);
        if (user.getReservations() == null) {
            user.setReservations(new java.util.ArrayList<>());
        }
        if (user.getApprovedReservations() == null) {
            user.setApprovedReservations(new java.util.ArrayList<>());
        }
        if (user.getAuditLogs() == null) {
            user.setAuditLogs(new java.util.ArrayList<>());
        }
        user.setId(null);
        LocalDateTime now = LocalDateTime.now();
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);

        User saved = userRepository.save(user);
        return toOutput(saved);
    }

    @Override
    public UserOutputDTO update(Long id, UserInputDTO inputDTO) {
        SecurityUtils.requireAny(UserRole.ADMIN);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        validateUniqueEmail(inputDTO.getEmail(), id);
        modelMapper.map(inputDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(existing);
        return toOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserOutputDTO findById(Long id) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return toOutput(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOutputDTO> findAll() {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        return userRepository.findAll().stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        SecurityUtils.requireAny(UserRole.ADMIN);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        existing.setActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existing);
    }

     private void validateUniqueEmail(String email, Long currentId) {
        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email)
                    .filter(user -> !Objects.equals(user.getId(), currentId))
                    .ifPresent(user -> {
                        throw new BusinessRuleException("Email is already registered for another user");
                    });
        }
    }

    private UserOutputDTO toOutput(User user) {
        UserOutputDTO outputDTO = userMapper.convertToOutput(user);
        java.util.List<Reservation> reservations = user.getReservations() == null ? java.util.List.<Reservation>of()
                : user.getReservations();
        outputDTO.setReservationIds(reservations.stream()
                .map(Reservation::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        java.util.List<Reservation> approved = user.getApprovedReservations() == null ? java.util.List.<Reservation>of()
                : user.getApprovedReservations();
        outputDTO.setApprovedReservationIds(approved.stream()
                .map(Reservation::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        java.util.List<AuditLog> auditLogs = user.getAuditLogs() == null ? java.util.List.<AuditLog>of()
                : user.getAuditLogs();
        outputDTO.setAuditLogIds(auditLogs.stream()
                .map(log -> log.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return outputDTO;
    }
}