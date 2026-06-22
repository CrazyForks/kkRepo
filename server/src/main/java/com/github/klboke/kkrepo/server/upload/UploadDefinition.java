package com.github.klboke.kkrepo.server.upload;

import java.util.List;

public record UploadDefinition(
    String format,
    boolean multipleUpload,
    List<UploadFieldDefinition> componentFields,
    List<UploadFieldDefinition> assetFields) {
}
