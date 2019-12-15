package com.example.fileupload;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import ch.qos.logback.core.db.dialect.MySQLDialect;

@Service
public class StorageService {

	private final Path fileStorageLocation;

	@Autowired
	public StorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception e) {
			throw new StorageFileNotFoundException("Could not create directory." + e);
		}
	}

	public String storeFile(MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		try {
			if (fileName.contains("..")) {
				throw new StorageFileNotFoundException("Filename contains invalid path sequence " + fileName);
			}

			// copy file to the target location
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return fileName;
		} catch (IOException e) {
			throw new StorageFileNotFoundException("Could not store file " + fileName + "." + e);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("File not found " + fileName);
			}

		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("File not found " + fileName);
		}
	}

//	void init();
//
//	void store(MultipartFile file);
//
//	Stream<Path> loadAll();
//
//	Path load(String filename);
//
//	Resource loadAsResource(String filename);
//
//	void deleteAll();

}