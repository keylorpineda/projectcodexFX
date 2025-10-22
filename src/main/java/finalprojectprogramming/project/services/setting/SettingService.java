package finalprojectprogramming.project.services.setting;

import finalprojectprogramming.project.dtos.SettingDTO;
import java.util.List;

public interface SettingService {

    SettingDTO create(SettingDTO settingDTO);

    SettingDTO update(Long id, SettingDTO settingDTO);

    SettingDTO findById(Long id);

    List<SettingDTO> findAll();

    SettingDTO findByKey(String key);

    void delete(Long id);
}