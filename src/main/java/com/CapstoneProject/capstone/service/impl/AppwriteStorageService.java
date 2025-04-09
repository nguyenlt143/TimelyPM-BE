package com.CapstoneProject.capstone.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Service
public class AppwriteStorageService {

    @Value("${appwrite.endpoint}")
    private String endpoint;

    @Value("${appwrite.projectId}")
    private String projectId;

    @Value("${appwrite.apiKey}")
    private String apiKey;

    @Value("${appwrite.bucketId}")
    private String bucketId;

    public String uploadFileToAppwrite(MultipartFile file) throws IOException, InterruptedException {
        String boundary = "Boundary-" + System.currentTimeMillis();

        HttpRequest.BodyPublisher bodyPublisher = ofMimeMultipartData(file, boundary);
        System.out.println(endpoint + "/storage/buckets/" + bucketId + "/files");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/storage/buckets/" + bucketId + "/files"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("X-Appwrite-Project", projectId)
                .header("X-Appwrite-Key", apiKey)
                .POST(bodyPublisher)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            String responseBody = response.body();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            String fileId = jsonNode.get("$id").asText();
            return endpoint + "/storage/buckets/" + bucketId + "/files/" + fileId + "/view"
                    + "?project=" + projectId;
        } else {
            throw new RuntimeException("Upload failed: " + response.body());
        }
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(MultipartFile file, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();

        // fileId part
        byteArrays.add(("--" + boundary + "\r\n").getBytes());
        byteArrays.add("Content-Disposition: form-data; name=\"fileId\"\r\n\r\n".getBytes());
        byteArrays.add("unique()\r\n".getBytes());

        // file part
        byteArrays.add(("--" + boundary + "\r\n").getBytes());
        byteArrays.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n").getBytes());
        byteArrays.add(("Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes());
        byteArrays.add(file.getBytes());
        byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

}
