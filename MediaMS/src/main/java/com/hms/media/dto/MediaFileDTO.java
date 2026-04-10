package com.hms.media.dto;

import com.hms.media.entity.MediaFile;
import com.hms.media.entity.Storage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaFileDTO {
    Long id;
    String name;
    String type;
    Long size;
    byte[] data;
    Storage storage;

    LocalDateTime createdAt;

    public static MediaFileDTO fromMediaFile(MediaFile mediaFile) {
        return new MediaFileDTO(mediaFile.getId(), mediaFile.getName(), mediaFile.getType(), mediaFile.getSize(), mediaFile.getData(), mediaFile.getStorage(), mediaFile.getCreatedAt());
    }
//
//    public MediaFile setName(String name) {
//        this.name = name;
//        return this;
//    }
//
//    public MediaFile setType(String type) {
//        this.type = type;
//        return this;
//    }
//
//    public MediaFile build() {
//        return this;
//    }
//
//    public void getObj() {
//        MediaFile.builder().name()
//    }
}
