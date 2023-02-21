package com.capgemini.payloadmanagement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capgemini.payloadmanagement.model.PayloadEntity;

public interface PayloadRepository extends JpaRepository<PayloadEntity, String> {
	Optional<PayloadEntity> findBySchemaId(String schemaId);
}
