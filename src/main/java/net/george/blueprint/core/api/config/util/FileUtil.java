package net.george.blueprint.core.api.config.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.george.blueprint.core.Blueprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    @CanIgnoreReturnValue
    public static Path getOrCreateDirectory(Path dirPath, String dirLabel) {
        if (!Files.isDirectory(dirPath.getParent())) {
            getOrCreateDirectory(dirPath.getParent(), "parent of " + dirLabel);
        }

        if (!Files.isDirectory(dirPath)) {
            LOGGER.debug(Blueprint.CORE, "Making {} directory : {}", dirLabel, dirPath);

            try {
                Files.createDirectory(dirPath);
            } catch (IOException exception) {
                if (exception instanceof FileAlreadyExistsException) {
                    LOGGER.fatal(Blueprint.CORE, "Failed to create {} directory - there is a file in the way", dirLabel);
                } else {
                    LOGGER.fatal(Blueprint.CORE, "Problem with creating {} directory (Permissions?)", dirLabel, exception);
                }

                throw new RuntimeException("Problem creating directory", exception);
            }

            LOGGER.debug(Blueprint.CORE, "Created {} directory : {}", dirLabel, dirPath);
        } else {
            LOGGER.debug(Blueprint.CORE, "Found existing {} directory : {}", dirLabel, dirPath);
        }

        return dirPath;
    }

    public static String fileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf(46);
        return index > -1 ? fileName.substring(index + 1) : "";

    }
}
