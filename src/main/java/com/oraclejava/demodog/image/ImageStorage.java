package com.oraclejava.demodog.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 업로드된 이미지를 images 폴더에 저장하고 /images/** URL로 돌려준다.
 */
@Component
public class ImageStorage {

	public static final String URL_PREFIX = "/images/";

	private static final Logger log = LoggerFactory.getLogger(ImageStorage.class);

	/** 파일 앞부분(시그니처)으로 판별한다. 클라이언트가 보내는 Content-Type은 믿을 수 없어서 쓰지 않는다. */
	private static final int SIGNATURE_LENGTH = 12;

	private final Path directory;

	public ImageStorage(@Value("${demodog.image-dir:images}") String directory) throws IOException {
		this.directory = Path.of(directory).toAbsolutePath().normalize();
		Files.createDirectories(this.directory);
	}

	public Path getDirectory() {
		return directory;
	}

	/** 파일을 저장하고 접근 URL(/images/{파일명})을 반환한다. 파일명은 충돌과 경로 조작을 막기 위해 새로 만든다. */
	public String store(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty");
		}
		String extension = detectExtension(file);
		if (extension == null) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
					"Only JPEG, PNG, GIF and WebP images are allowed");
		}
		String filename = UUID.randomUUID() + "." + extension;
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, directory.resolve(filename));
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to store image " + filename, ex);
		}
		return URL_PREFIX + filename;
	}

	private static String detectExtension(MultipartFile file) {
		byte[] head;
		try (InputStream in = file.getInputStream()) {
			head = Arrays.copyOf(in.readNBytes(SIGNATURE_LENGTH), SIGNATURE_LENGTH);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to read uploaded image", ex);
		}
		if (startsWith(head, 0, 0xFF, 0xD8, 0xFF)) {
			return "jpg";
		}
		if (startsWith(head, 0, 0x89, 'P', 'N', 'G')) {
			return "png";
		}
		if (startsWith(head, 0, 'G', 'I', 'F', '8')) {
			return "gif";
		}
		if (startsWith(head, 0, 'R', 'I', 'F', 'F') && startsWith(head, 8, 'W', 'E', 'B', 'P')) {
			return "webp";
		}
		return null;
	}

	private static boolean startsWith(byte[] data, int offset, int... signature) {
		for (int i = 0; i < signature.length; i++) {
			if ((data[offset + i] & 0xFF) != signature[i]) {
				return false;
			}
		}
		return true;
	}

	/** store()가 반환한 URL의 파일을 지운다. 이 폴더의 파일이 아니면 무시한다. */
	public void delete(String url) {
		if (url == null || !url.startsWith(URL_PREFIX)) {
			return;
		}
		Path target = directory.resolve(url.substring(URL_PREFIX.length())).normalize();
		if (!target.getParent().equals(directory)) {
			return;
		}
		try {
			Files.deleteIfExists(target);
		}
		catch (IOException ex) {
			log.warn("Failed to delete image {}", target, ex);
		}
	}

}
