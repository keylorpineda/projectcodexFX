package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.services.setting.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@Validated
@Tag(name = "Settings", description = "Operations related to system settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @PostMapping
    @Operation(summary = "Create a new setting")
    public ResponseEntity<SettingDTO> createSetting(@Valid @RequestBody SettingDTO settingDTO) {
        SettingDTO created = settingService.create(settingDTO);
        return ResponseEntity.created(URI.create("/api/settings/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing setting")
    public ResponseEntity<SettingDTO> updateSetting(@PathVariable Long id, @Valid @RequestBody SettingDTO settingDTO) {
        return ResponseEntity.ok(settingService.update(id, settingDTO));
    }

    @GetMapping
    @Operation(summary = "Retrieve all settings")
    public ResponseEntity<List<SettingDTO>> getAllSettings() {
        return ResponseEntity.ok(settingService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a setting by id")
    public ResponseEntity<SettingDTO> getSettingById(@PathVariable Long id) {
        return ResponseEntity.ok(settingService.findById(id));
    }

    @GetMapping(params = "key")
    @Operation(summary = "Retrieve a setting by key")
    public ResponseEntity<SettingDTO> getSettingByKey(@RequestParam @NotBlank String key) {
        return ResponseEntity.ok(settingService.findByKey(key));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a setting")
    public ResponseEntity<Void> deleteSetting(@PathVariable Long id) {
        settingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}