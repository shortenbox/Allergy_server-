package com.example.allergy_server.external_ocr;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * =========================================
 * ClovaOcrClient
 * =========================================
 * [역할]
 * - Clova OCR API 실제 호출
 *
 * [기능]
 * - multipart/form-data 요청 생성
 * - OCR JSON message 생성
 * - 이미지 업로드
 * - API 응답 반환
 *
 * [특징]
 * - 외부 OCR 서버 전담 클래스
 * - OcrPicture 에서 호출됨
 *
 * [주의]
 * - application.properties 필요
 * - clova.ocr.url
 * - clova.ocr.secret
 * =========================================
 */

@Component
public class ClovaOcrClient {

    @Value("${clova.ocr.url}")
    private String clovaUrl;

    @Value("${clova.ocr.secret}")
    private String secretKey;

    /**
     * Clova OCR 요청
     */
    public String requestOcr(MultipartFile file) {

        try {

            // multipart boundary 생성
            String boundary =
                    "----OCRBoundary" + UUID.randomUUID();

            URL url = new URL(clovaUrl);

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");

            // Header 설정
            conn.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + boundary
            );

            conn.setRequestProperty(
                    "X-OCR-SECRET",
                    secretKey
            );

            // =========================
            // OCR Message 생성
            // =========================

            JSONObject message = new JSONObject();

            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();

            // 확장자 추출
            String originalFilename = file.getOriginalFilename();

            String format = "jpg";

            if (originalFilename != null &&
                    originalFilename.contains(".")) {

                format = originalFilename.substring(
                        originalFilename.lastIndexOf(".") + 1
                );
            }

            image.put("format", format);
            image.put("name", "meal");

            JSONArray images = new JSONArray();
            images.put(image);

            message.put("images", images);

            // =========================
            // multipart body 생성
            // =========================

            OutputStream os = conn.getOutputStream();

            // message part
            String messagePart =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"message\"\r\n\r\n" +
                            message.toString() + "\r\n";

            os.write(messagePart.getBytes(StandardCharsets.UTF_8));

            // file part
            String filePartHeader =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; " +
                            "name=\"file\"; filename=\"" +
                            originalFilename + "\"\r\n" +
                            "Content-Type: application/octet-stream\r\n\r\n";

            os.write(filePartHeader.getBytes(StandardCharsets.UTF_8));

            // 실제 파일 데이터
            os.write(file.getBytes());

            os.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // multipart 종료
            String endBoundary =
                    "--" + boundary + "--\r\n";

            os.write(endBoundary.getBytes(StandardCharsets.UTF_8));

            os.flush();
            os.close();

            // =========================
            // 응답 처리
            // =========================

            int responseCode = conn.getResponseCode();

            InputStream responseStream;

            if (responseCode >= 200 &&
                    responseCode < 300) {

                responseStream = conn.getInputStream();

            } else {

                responseStream = conn.getErrorStream();

                System.out.println(
                        "Clova OCR ERROR CODE = " + responseCode
                );
            }

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    responseStream,
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();

            return sb.toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "";
        }
    }
}
