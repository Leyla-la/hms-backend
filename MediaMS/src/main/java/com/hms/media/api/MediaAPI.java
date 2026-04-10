package com.hms.media.api;

import com.hms.media.dto.MediaFileDTO;
import com.hms.media.entity.MediaFile;
import com.hms.media.exception.HmsException;
import com.hms.media.service.MediaFileService;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/media")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Media Storage", description = "Endpoints for uploading and retrieving medical records, profile pictures, and other media files.")
public class MediaAPI {

    MediaFileService mediaFileService;

    @Operation(summary = "Upload a file", description = "Stores a file in the system and returns its metadata and unique ID.")
    @PostMapping("/upload")
    public ResponseEntity<MediaFileDTO> uploadMediaFile(@RequestPart("file") MultipartFile file) throws HmsException {
        try {
            MediaFileDTO mediaFileDTO = mediaFileService.storeFile(file);
            return ResponseEntity.ok(mediaFileDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Download a file", description = "Retrieves the raw binary data of a file by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) throws Exception {
        Optional<MediaFile> mediaFile = mediaFileService.getFile(id);
        if (mediaFile.isPresent()) {
            MediaFile file = mediaFile.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getType()))
                    .body(file.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
