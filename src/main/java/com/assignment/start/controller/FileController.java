package com.assignment.start.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller to handle all the file operations
 */
@RestController
@RequestMapping("/file")
@PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Value("${file.dir}")
    private String path;

    /**
     * URL: http://localhost:8080/file/listFiles
     * Method to display the list of files stored inside the server.
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/listFiles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> listFiles() throws IOException {
        LOGGER.debug("Path from properties file=" + path);
        List<String> paths = null;
        try {
            if (Files.isDirectory(Paths.get(path))) {
                Stream<Path> listOfFiles = Files.walk(Paths.get(path));
                paths = parseFilePath(listOfFiles);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while trying to fetch list of files", e);
        }

        return new ResponseEntity<Object>(paths, HttpStatus.OK);
    }

    /**
     * URL: http://localhost:8080/file/upload
     * Provide the valid File data
     * Method to upload the file to the server
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadFile(@RequestParam MultipartFile file) {
        try {
            Path uploadPath = Paths.get(path.toString() + "\\" + file.getOriginalFilename());
            Files.write(uploadPath, file.getBytes());
        } catch (IOException e) {
            LOGGER.error("Exception occurred while trying to create the new file", e);
        }
        return new ResponseEntity<Object>("File uploaded successfully to ", HttpStatus.OK);
    }

    /**
     * URL: http://localhost:8080/file/download?filePath=1to10%5Ctest.txt (encoded file path is required)
     * Method to download the file from the server
     *
     * @param filePath
     * @param response
     * @return
     */
    @RequestMapping(value = "/download", headers = "Accept=*/*", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String filePath, HttpServletResponse response) {
        String completePath = path.toString() + filePath;
        File file = null;
        InputStreamResource resource = null;
        String fileType = null;
        if (Files.exists(Paths.get(completePath))) {
            try {
                fileType = Files.probeContentType(Paths.get(completePath));
                file = new File(completePath);
                InputStream data = new FileInputStream(file);
                resource = new InputStreamResource(data);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while trying to read the data from the stored file", e);
            }
        }
        HttpHeaders httpHeaders = getHttpHeaders(filePath, fileType);
        httpHeaders.setContentLength(file.length());
        response.setContentType(fileType);
        return new ResponseEntity<InputStreamResource>(resource, httpHeaders, HttpStatus.OK);
    }

    /**
     * Add headers to download the file
     *
     * @param filePath
     * @param fileType
     * @return
     */
    private HttpHeaders getHttpHeaders(String filePath, String fileType) {
        String extension = filePath.substring(filePath.indexOf("."));
        String fileName = filePath.split(extension)[0];
        LOGGER.debug("fileName=" + fileName);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName + extension);
        httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        httpHeaders.setContentType(MediaType.valueOf(fileType));
        return httpHeaders;
    }

    /**
     * The file path obtained from the server needs to be parsed to avaoid the common path
     *
     * @param listOfFiles
     * @return
     */
    private List<String> parseFilePath(Stream<Path> listOfFiles) {
        List<String> fileNames = listOfFiles.map(filePath -> {
            String fileName = null;
            if (filePath != null && !Files.isDirectory(filePath)) {
                final String originalPath = path;
                String folderPath = "";
                if (!originalPath.toString().equals(filePath.toString())) {
                    folderPath = filePath.getParent().toString().substring(originalPath.toString().lastIndexOf("\\") + 7) + "\\";
                }
                fileName = folderPath + filePath.getFileName().toString();
            }
            return fileName;
        }).filter(path -> path != null).collect(Collectors.toList());
        return fileNames;
    }
}
