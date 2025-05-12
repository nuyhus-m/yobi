package com.S209.yobi;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 원본 파일명
        String originalFilename = file.getOriginalFilename();

        // 파일명 중복 방지를 위해 UUID 사용
        String fileName = "client/profile/" + UUID.randomUUID() + "_" + originalFilename;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            // S3에 파일 업로드
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );

            // 업로드된 파일의 URL 반환
            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new IOException("S3 파일 업로드 실패: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.contains(bucket)) {
                String fileName = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
                amazonS3Client.deleteObject(bucket, fileName);
                log.info("S3 파일 삭제 성공: {}", fileName);
            }
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
        }
    }
}