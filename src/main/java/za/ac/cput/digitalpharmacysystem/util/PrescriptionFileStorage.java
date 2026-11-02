package za.ac.cput.digitalpharmacysystem.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores uploaded prescription files on disk.
 *
 * Safety rules:
 *  - the file type is decided from the file's first bytes, not from the name or Content-Type the
 *    client sent (both can be faked);
 *  - the stored name is always a server-generated UUID plus a fixed extension, so a client can never
 *    choose a path or overwrite another file;
 *  - the database only ever holds that stored name, never a full path.
 */
@Component
public class PrescriptionFileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Pattern STORED_NAME =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|pdf)$");

    private final Path root;

    public PrescriptionFileStorage(@Value("${app.upload.dir:uploads/prescriptions}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Validates and saves the file, returning the stored file name. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a prescription file to upload");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("The file is too large (maximum 5 MB)");
        }

        try {
            byte[] header = new byte[8];
            int read;
            try (InputStream in = file.getInputStream()) {
                read = in.readNBytes(header, 0, header.length);
            }
            String extension = detectExtension(Arrays.copyOf(header, read));
            if (extension == null) {
                throw new IllegalArgumentException("Only JPG, PNG or PDF files are allowed");
            }

            Files.createDirectories(root);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = root.resolve(storedName).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Invalid file name");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedName;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the uploaded file", e);
        }
    }

    /** Returns the on-disk path for a stored name, or null if the name is invalid or the file is missing. */
    public Path find(String storedName) {
        if (storedName == null || !STORED_NAME.matcher(storedName).matches()) {
            return null;
        }
        Path path = root.resolve(storedName).normalize();
        return path.startsWith(root) && Files.isRegularFile(path) ? path : null;
    }

    public String contentTypeOf(String storedName) {
        if (storedName.endsWith(".png")) return "image/png";
        if (storedName.endsWith(".pdf")) return "application/pdf";
        return "image/jpeg";
    }

    private static String detectExtension(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return "png";
        }
        if (h.length >= 5 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F' && h[4] == '-') {
            return "pdf";
        }
        return null;
    }
}

