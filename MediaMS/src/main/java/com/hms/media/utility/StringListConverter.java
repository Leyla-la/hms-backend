package com.hms.media.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringListConverter {
    public static String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    public static List<String> fromCsv(String csv) {
        if (csv == null) return Collections.emptyList();
        String trimmed = csv.trim();
        if (trimmed.isEmpty()) return Collections.emptyList();

        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
