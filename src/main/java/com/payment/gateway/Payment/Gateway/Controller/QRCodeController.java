package com.payment.gateway.Payment.Gateway.Controller;

import com.payment.gateway.Payment.Gateway.Model.QRCodeEntity;
import com.payment.gateway.Payment.Gateway.Repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.payment.gateway.Payment.Gateway.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    @Autowired
    private PaymentService paymentService;

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

    @PostMapping("/createOrder")
    public ResponseEntity<String> createPaymentOrder(@RequestParam int amount) {
        try {
            String order = paymentService.createOrder(amount, "INR", "txn_12345");
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in creating order: " + e.getMessage());
        }
    }

    @PostMapping("/verifyPayment")
    public ResponseEntity<String> verifyPayment(@RequestParam String razorpayOrderId,
                                                @RequestParam String paymentId,
                                                @RequestParam String signature,
                                                @RequestParam String userId,
                                                @RequestParam String eventId) {
        if (paymentService.verifyPayment(razorpayOrderId, paymentId, signature)) {
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

                // Save QR details in the database
                QRCodeEntity qrCodeEntity = new QRCodeEntity(null, userId, eventId, qrContent, false);
                qrCodeRepository.save(qrCodeEntity);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDisposition(ContentDisposition.builder("attachment").filename("QRCode.png").build());

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(Base64.getEncoder().encodeToString(qrBytes));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error generating QR Code: " + e.getMessage());
            }
        }
        return ResponseEntity.status(400).body("Payment Verification Failed");
    }


}
