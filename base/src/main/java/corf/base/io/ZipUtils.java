package corf.base.io;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipInputStream;

public final class ZipUtils {

    private static final int BUFFER_SIZE = 4096;

    private ZipUtils() { }

    /** Extracts ZIP file to the destination directory. */
    public static void unzip(Path zipFile, Path destDir) throws IOException {
        Objects.requireNonNull(zipFile, "inputStream");
        unzip(new FileInputStream(zipFile.toFile()), destDir);
    }

    /** Extracts ZIP file to the destination directory. */
    public static void unzip(InputStream inputStream, Path destDir) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(destDir, "destDir");

        if (!Files.exists(destDir)) { Files.createDirectory(destDir); }

        var zipStream = new ZipInputStream(new BufferedInputStream(inputStream));

        var zipEntry = zipStream.getNextEntry();
        while (zipEntry != null) {
            Path path = destDir.resolve(zipEntry.getName());
            if (!zipEntry.isDirectory()) {
                extractFile(zipStream, path);
            } else {
                Files.createDirectory(path);
            }
            zipStream.closeEntry();
            zipEntry = zipStream.getNextEntry();
        }

        zipStream.close();
    }

    private static void extractFile(ZipInputStream inputStream, Path path) throws IOException {
        var out = new BufferedOutputStream(new FileOutputStream(path.toFile()));

        byte[] bytes = new byte[BUFFER_SIZE];
        int read;
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.close();
    }

    /** Reads single file from the ZIP archive. */
    public InputStream readFile(Path zipPath, String subPath) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(zipPath)) {
            Path fileToExtract = fileSystem.getPath(subPath);
            return new FileInputStream(fileToExtract.toFile());
        }
    }

    /** Checks whether ZIP archive has valid format. */
    public static boolean isExtractable(Path zipFile) throws IOException {
        int fileSignature;
        try (RandomAccessFile raf = new RandomAccessFile(zipFile.toFile(), "r")) {
            fileSignature = raf.readInt();
        }
        return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    }
}
