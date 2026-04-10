package com.hms.media.service;

import com.hms.media.dto.MediaFileDTO;
import com.hms.media.entity.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface MediaFileService {
    MediaFileDTO storeFile(MultipartFile file) throws Exception;
    public Optional<MediaFile> getFile(Long id) throws Exception;
}
