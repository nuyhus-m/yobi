package com.S209.yobi.schedules.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class ImageResizeService {

    /**
     * 이미지가 너무 크면 리사이징하는 메소드
     * @param imageFile 원본 이미지 파일
     * @return 리사이징된 이미지 파일 (원본이 작으면 원본 그대로 반환)
     * @throws IOException 이미지 처리 중 오류 발생시
     */
    public MultipartFile resizeImageIfNeeded(MultipartFile imageFile) throws IOException {
        // 최대 허용 크기 (5MB)
        final long MAX_FILE_SIZE = 5 * 1024 * 1024;

        // 이미 작은 이미지면 그대로 반환
        if (imageFile.getSize() <= MAX_FILE_SIZE) {
            return imageFile;
        }

        // 이미지 읽기
        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
        if (originalImage == null) {
            throw new IOException("유효하지 않은 이미지 파일입니다.");
        }

        // 최대 해상도 설정
        int MAX_WIDTH = 1200;
        int MAX_HEIGHT = 1200;

        // 원본 이미지 크기
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 리사이징 필요 여부 확인 및 비율 계산
        double scale = 1.0;
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            scale = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        }

        // 새 크기 계산
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        // 이미지 리사이징
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        // 고품질 리사이징을 위한 렌더링 힌트 설정
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 이미지 그리기
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        // 이미지를 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = getImageFormat(imageFile.getOriginalFilename());
        ImageIO.write(resizedImage, formatName, baos);
        byte[] imageBytes = baos.toByteArray();

        // 바이트 배열을 MultipartFile로 변환하여 반환
        return new InMemoryMultipartFile(
                imageFile.getName(),
                imageFile.getOriginalFilename(),
                imageFile.getContentType(),
                imageBytes
        );
    }

    /**
     * 파일 확장자로부터 이미지 포맷 이름 추출
     */
    private String getImageFormat(String filename) {
        if (filename == null) {
            return "jpeg"; // 기본 포맷
        }
        String lowercaseName = filename.toLowerCase();
        if (lowercaseName.endsWith(".png")) {
            return "png";
        } else if (lowercaseName.endsWith(".bmp")) {
            return "bmp";
        } else if (lowercaseName.endsWith(".gif")) {
            return "gif";
        } else {
            return "jpeg"; // .jpg, .jpeg 또는 알 수 없는 형식은 JPEG로 기본 설정
        }
    }

    /**
     * 메모리 내 MultipartFile 구현 클래스
     */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}