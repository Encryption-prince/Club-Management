package com.club.management.Club.Management.Repository;

import com.club.management.Club.Management.Model.QRCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCodeEntity, Long> {
    Optional<QRCodeEntity> findByQrContent(String qrContent);
}
