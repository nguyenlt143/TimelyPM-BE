package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;

@Service
public class GoogleDriveService {

    @Value("${google.drive_application}")
    private String APPLICATION_NAME;

    @Value("${google.drive_folder_id}")
    private String FOLDER_ID;

    @Value("${google.project_id}")
    private String projectId;

    @Value("${google.private_key_id}")
    private String privateKeyId;

    @Value("${google.private_key}")
    private String privateKey;

    @Value("${google.client_email}")
    private String clientEmail;

    @Value("${google.client_id}")
    private String clientId;

    @Value("${google.auth_uri}")
    private String authUri;

    @Value("${google.token_uri}")
    private String tokenUri;

    @Value("${google.auth_provider_x509_cert_url}")
    private String authProviderCertUrl;

    @Value("${google.client_x509_cert_url}")
    private String clientX509CertUrl;

    @Value("${google.universe_domain}")
    private String universeDomain;


    public Drive getDriveService() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(getCredentialsInputStream())
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    private InputStream getCredentialsInputStream() {
        String credentialsJson = String.format("{\"type\": \"service_account\", \"project_id\": \"%s\", \"private_key_id\": \"%s\", \"private_key\": \"%s\", \"client_email\": \"%s\", \"client_id\": \"%s\", \"auth_uri\": \"%s\", \"token_uri\": \"%s\", \"auth_provider_x509_cert_url\": \"%s\", \"client_x509_cert_url\": \"%s\", \"universe_domain\": \"%s\"}",
                projectId, privateKeyId, privateKey, clientEmail, clientId, authUri, tokenUri, authProviderCertUrl, clientX509CertUrl, universeDomain);
        return new java.io.ByteArrayInputStream(credentialsJson.getBytes());
    }
    public GoogleDriveResponse uploadFileToDrive(MultipartFile file) throws IOException {
        Drive driveService = getDriveService();

        java.io.File tempFile = java.io.File.createTempFile("temp", null);
        file.transferTo(tempFile);

        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        FileContent mediaContent = new FileContent(file.getContentType(), tempFile);
        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id,webContentLink,webViewLink")
                .execute();

        tempFile.delete();

        GoogleDriveResponse googleDriveResponse = new GoogleDriveResponse();
        googleDriveResponse.setFileName(file.getOriginalFilename());
        googleDriveResponse.setFileUrl(uploadedFile.getWebViewLink());

        return googleDriveResponse;
    }

    public String extractFileId(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("d/")) {
            String[] parts = fileUrl.split("d/");
            String idPart = parts[1];
            if (idPart.contains("/")) {
                idPart = idPart.split("/")[0];
            }
            return idPart;
        }
        throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
    }
}
