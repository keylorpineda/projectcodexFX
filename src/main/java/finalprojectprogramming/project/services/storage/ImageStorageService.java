package finalprojectprogramming.project.services.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String store(Long spaceId, MultipartFile file);

    void delete(String imageUrl);
}
