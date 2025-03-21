package com.club.management.Club.Management.Controller;

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

@RestController
@RequestMapping("/api/payment")
public class QRCodeController {

    @Autowired
    QRCodeRepository qrCodeRepository;

    @PostMapping("/generateOR")
    public ResponseEntity<String> generateQR(@RequestParam String userId, @RequestParam String eventId){

        try {
            String qrContent = userId + "|" + eventId + "|" + System.currentTimeMillis();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            String qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            QRCodeEntity qrCodeEntity = new QRCodeEntity(userId, eventId, qrContent, false);
            qrCodeRepository.save(qrCodeEntity);

            return new ResponseEntity<>("data:image/png;base64," + qrBase64, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error generating OR Code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/validateQR")
    public ResponseEntity<String> validateQRCode(@RequestParam String qrContent) {
        Optional<QRCodeEntity> qrCodeEntity = qrCodeRepository.findByQrContent(qrContent);

        if (qrCodeEntity.isPresent() && !qrCodeEntity.get().isUsed()) {
            qrCodeEntity.get().setUsed(true);
            qrCodeRepository.save(qrCodeEntity.get());
            return ResponseEntity.ok("QR Code Validated Successfully");
        } else {
            return ResponseEntity.status(400).body("Invalid or Already Used QR Code");
        }
    }

    private void markQRAsUsed(String userId, String eventId) {
        System.out.println("OR Code marked as used for user : "+userId);
    }

    private boolean checkIfQRIsValid(String userId, String eventId) {
        return true;
    }

}
