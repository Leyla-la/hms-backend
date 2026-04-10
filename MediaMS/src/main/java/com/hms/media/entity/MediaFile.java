package com.hms.media.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String type;
    Long size;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    byte[] data;
    Storage storage;

    @CreationTimestamp
    LocalDateTime createdAt;

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
