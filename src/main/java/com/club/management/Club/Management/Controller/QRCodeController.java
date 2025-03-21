package com.club.management.Club.Management.Controller;

import com.club.management.Club.Management.Model.QRCodeEntity;
import com.club.management.Club.Management.Repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class QRCodeController {

    @Autowired
    QRCodeRepository qrCodeRepository;

    @PostMapping("/generateQR")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam String userId, @RequestParam String eventId) {
        try {
            String qrContent = userId + "|" + eventId + "|" + System.currentTimeMillis();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] qrBytes = baos.toByteArray();

            // Save QR Code details in the database
            QRCodeEntity qrCodeEntity = new QRCodeEntity(null, userId, eventId, qrContent, false);
            qrCodeRepository.save(qrCodeEntity);

            // Return image as a downloadable response
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=qr_code.png")
                    .header("Content-Type", "image/png")
                    .body(qrBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }



    @PostMapping("/validateQR")
    public ResponseEntity<String> validateQRCode(@RequestParam String qrContent) {
        Optional<QRCodeEntity> qrCodeEntity = qrCodeRepository.findByQrContent(qrContent);

        if (qrCodeEntity.isPresent()) {
            QRCodeEntity qr = qrCodeEntity.get();

            if (!qr.isUsed()) {
                qr.setUsed(true);
                qrCodeRepository.save(qr); // Mark QR as used
                return ResponseEntity.ok("✅ QR Code Validated Successfully!");
            } else {
                return ResponseEntity.status(400).body("⚠️ QR Code Already Used!");
            }
        } else {
            return ResponseEntity.status(400).body("❌ Invalid QR Code!");
        }
    }


    private void markQRAsUsed(String userId, String eventId) {
        System.out.println("OR Code marked as used for user : "+userId);
    }

    private boolean checkIfQRIsValid(String userId, String eventId) {
        return true;
    }

}
