package telekit.base.util;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtils {

    private static final int BUFFER_SIZE = 4096;

    public static void unzip(Path zipPath, Path destDir) throws IOException {
        unzip(new FileInputStream(zipPath.toFile()), destDir);
    }

    public static void unzip(InputStream inputStream, Path destDir) throws IOException {
        if (!Files.exists(destDir)) {
            Files.createDirectory(destDir);
        }

        ZipInputStream zipStream = new ZipInputStream(
                new BufferedInputStream(inputStream)
        );

        ZipEntry zipEntry = zipStream.getNextEntry();
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

    private static void extractFile(ZipInputStream zipStream, Path path) throws IOException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream(path.toFile())
        );

        byte[] bytes = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, read);
        }
        bufferedOutputStream.close();
    }

    public InputStream readFile(Path zipPath, String subPath) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(zipPath)) {
            Path fileToExtract = fileSystem.getPath(subPath);
            return new FileInputStream(fileToExtract.toFile());
        }
    }

    public static boolean isExtractable(Path zipFile) throws IOException {
        int fileSignature;
        try (RandomAccessFile raf = new RandomAccessFile(zipFile.toFile(), "r")) {
            fileSignature = raf.readInt();
        }
        return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    }
}
