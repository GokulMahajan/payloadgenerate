package com.capgemini.payloadmanagement.model;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="payload_entity")
public class PayloadEntity {
	@Id
	@Column(name="schema_id")
	protected String schemaId;
	protected String description;
	@Column(name="payload_schema",columnDefinition = "text")
	protected String payloadSchema;
}
