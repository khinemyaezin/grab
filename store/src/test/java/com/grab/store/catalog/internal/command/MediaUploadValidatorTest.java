package com.grab.store.catalog.internal.command;

import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.service.MediaUploadValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaUploadValidatorTest {

    private final MediaUploadValidator validator = new MediaUploadValidator(1024L);

    @Test
    void validate_blankFilenameThrows() {
        assertThatThrownBy(() -> validator.validate(" ", "image/jpeg", 100L))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(ex -> {
                    var cse = (CatalogServiceException) ex;
                    assertThat(cse.getMessageSource().code()).isEqualTo("cat.service.product.media_upload_invalid");
                    assertThat(cse.getMessageSource().args()).containsEntry("reason", "filename is required");
                });
    }

    @Test
    void validate_disallowedContentTypeThrows() {
        assertThatThrownBy(() -> validator.validate("document.pdf", "application/pdf", 100L))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(ex -> {
                    var cse = (CatalogServiceException) ex;
                    assertThat(cse.getMessageSource().code()).isEqualTo("cat.service.product.media_upload_invalid");
                    assertThat(cse.getMessageSource().args()).containsEntry("reason", "contentType is not allowed");
                });
    }

    @Test
    void validate_exceedsMaxSizeThrows() {
        assertThatThrownBy(() -> validator.validate("photo.jpg", "image/jpeg", 1025L))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(ex -> {
                    var cse = (CatalogServiceException) ex;
                    assertThat(cse.getMessageSource().code()).isEqualTo("cat.service.product.media_upload_invalid");
                    assertThat(cse.getMessageSource().args()).containsEntry("reason", "file exceeds max size");
                });
    }

    @Test
    void validate_validInputsPasses() {
        validator.validate("photo.jpg", "image/jpeg", 1024L);
        validator.validate("image.PNG", "IMAGE/PNG", 500L);
        validator.validate("video.mp4", "video/mp4", null);
    }

    @Test
    void extension_extractsSanitizedExtension() {
        assertThat(validator.extension("photo.JPG")).isEqualTo(".jpg");
        assertThat(validator.extension("archive.tar.gz")).isEqualTo(".gz");
        assertThat(validator.extension("no-extension")).isEqualTo("");
        assertThat(validator.extension(null)).isEqualTo("");
    }
}
