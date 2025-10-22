package finalprojectprogramming.project.services.setting;

import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.repositories.SettingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingServiceImplementation implements SettingService {

    private final SettingRepository settingRepository;
    private final ModelMapper modelMapper;

    public SettingServiceImplementation(SettingRepository settingRepository, ModelMapper modelMapper) {
        this.settingRepository = settingRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SettingDTO create(SettingDTO settingDTO) {
        validateUniqueKey(settingDTO.getKey(), null);

        Setting setting = new Setting();
        setting.setKey(settingDTO.getKey());
        setting.setValue(settingDTO.getValue());
        setting.setDescription(settingDTO.getDescription());
        setting.setUpdatedAt(LocalDateTime.now());

        Setting saved = settingRepository.save(setting);
        return toDto(saved);
    }

    @Override
    public SettingDTO update(Long id, SettingDTO settingDTO) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));

        if (settingDTO.getKey() != null && !Objects.equals(settingDTO.getKey(), setting.getKey())) {
            validateUniqueKey(settingDTO.getKey(), id);
            setting.setKey(settingDTO.getKey());
        }
        if (settingDTO.getValue() != null) {
            setting.setValue(settingDTO.getValue());
        }
        if (settingDTO.getDescription() != null) {
            setting.setDescription(settingDTO.getDescription());
        }
        setting.setUpdatedAt(LocalDateTime.now());

        Setting saved = settingRepository.save(setting);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingDTO findById(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));
        return toDto(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingDTO> findAll() {
        return settingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SettingDTO findByKey(String key) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with key " + key + " not found"));
        return toDto(setting);
    }

    @Override
    public void delete(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));
        settingRepository.delete(setting);
    }

    private void validateUniqueKey(String key, Long currentId) {
        if (key == null || key.isBlank()) {
            throw new BusinessRuleException("Setting key cannot be blank");
        }
        settingRepository.findByKey(key)
                .filter(existing -> !Objects.equals(existing.getId(), currentId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A setting with the provided key already exists");
                });
    }

    private SettingDTO toDto(Setting setting) {
        return modelMapper.map(setting, SettingDTO.class);
    }
}