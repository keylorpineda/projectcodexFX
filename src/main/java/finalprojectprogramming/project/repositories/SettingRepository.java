package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Setting;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKey(String key);
}