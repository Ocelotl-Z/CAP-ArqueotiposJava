package com.axity.office.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.axity.office.model.OfficeDO;

/**
 * Persistence interface of de {@link com.axity.office.model.OfficeDO}
 * 
 * @author username@axity.com
 */
@Repository
public interface OfficePersistence extends JpaRepository<OfficeDO, Integer> {
	// Agregar consultas personalizadas
}
