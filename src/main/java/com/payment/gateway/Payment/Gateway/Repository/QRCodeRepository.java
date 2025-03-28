package com.payment.gateway.Payment.Gateway.Repository;

import com.payment.gateway.Payment.Gateway.Model.QRCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCodeEntity, Long> {
    Optional<QRCodeEntity> findByQrContent(String qrContent);
}
