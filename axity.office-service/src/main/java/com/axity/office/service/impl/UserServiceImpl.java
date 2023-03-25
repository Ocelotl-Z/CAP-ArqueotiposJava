package com.axity.office.service.impl;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axity.office.commons.dto.UserDto;
import com.axity.office.commons.enums.ErrorCode;
import com.axity.office.commons.exception.BusinessException;
import com.axity.office.commons.request.PaginatedRequestDto;
import com.axity.office.commons.response.GenericResponseDto;
import com.axity.office.commons.response.HeaderDto;
import com.axity.office.commons.response.PaginatedResponseDto;
import com.axity.office.model.RoleDO;
import com.axity.office.model.UserDO;
import com.axity.office.persistence.RolePersistence;
import com.axity.office.persistence.UserPersistence;
import com.axity.office.service.UserService;
import com.github.dozermapper.core.Mapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Class UserServiceImpl
 * 
 * @author username@axity.com
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
	@Autowired
	private UserPersistence userPersistence;

	@Autowired
	private RolePersistence rolePersistence;

	@Autowired
	private Mapper mapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PaginatedResponseDto<UserDto> findUsers(PaginatedRequestDto request) {
		log.debug("%s", request);

		int page = request.getOffset() / request.getLimit();
		Pageable pageRequest = PageRequest.of(page, request.getLimit(), Sort.by("id"));

		var paged = this.userPersistence.findAll(pageRequest);

		var result = new PaginatedResponseDto<UserDto>(page, request.getLimit(), paged.getTotalElements());

		paged.stream().forEach(x -> result.getData().add(this.transform(x)));

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericResponseDto<UserDto> find(Integer id) {
		GenericResponseDto<UserDto> response = null;

		var optional = this.userPersistence.findById(id);
		if (optional.isPresent()) {
			response = new GenericResponseDto<>();
			response.setBody(this.transform(optional.get()));
		}

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericResponseDto<UserDto> create(UserDto dto) {

		GenericResponseDto<UserDto> genericResponseDto = new GenericResponseDto<>();

		if (userPersistence.findByEmail(dto.getEmail()).isPresent()) {
			genericResponseDto.setHeader(new HeaderDto(406, "Error correo en uso."));
			return genericResponseDto;
		}

		if (userPersistence.findByUsername(dto.getUsername()).isPresent()) {
			genericResponseDto.setHeader(new HeaderDto(406, "Error nombre de usuario en uso."));
			return genericResponseDto;
		}

		// Verify if UserDto have roles exists
		if (dto.getRoles() == null) {
			genericResponseDto.setHeader(new HeaderDto(406, "Favor de elegir un rol como minimo."));
			return genericResponseDto;
		}

		// Verify if UserDto roles is not empty
		if (dto.getRoles().isEmpty()) {
			genericResponseDto.setHeader(new HeaderDto(406, "Favor de elegir un rol como minimo."));
			return genericResponseDto;
		}

		AtomicBoolean invalidRol = new AtomicBoolean(false);

		dto.getRoles().stream().forEach(r -> {
			invalidRol.set(rolePersistence.existsById(r.getId()) ? false : true);
		});

		if (invalidRol.get()) {
			genericResponseDto.setHeader(new HeaderDto(406, "Favor de elegir un rol valido."));
			return genericResponseDto;
		}

		UserDO entity = new UserDO();
		this.mapper.map(dto, entity);
		entity.setId(null);

		var roles = new ArrayList<RoleDO>();
		entity.setRoles(roles);

		dto.getRoles().stream().forEach(r -> {
			entity.getRoles().add(this.rolePersistence.findById(r.getId()).get());
		});

		this.userPersistence.save(entity);

		dto.setId(entity.getId());

		return new GenericResponseDto<>(dto);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericResponseDto<Boolean> update(UserDto dto) {
		var optional = this.userPersistence.findById(dto.getId());
		if (optional.isEmpty()) {
			throw new BusinessException(ErrorCode.OFFICE_NOT_FOUND);
		}

		var entity = optional.get();

		entity.setUsername(dto.getUsername());
		entity.setEmail(dto.getEmail());
		entity.setName(dto.getName());
		entity.setLastName(dto.getLastName());
		// TODO: Actualizar entity.Roles (?)

		this.userPersistence.save(entity);

		return new GenericResponseDto<>(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericResponseDto<Boolean> delete(Integer id) {
		var optional = this.userPersistence.findById(id);
		if (optional.isEmpty()) {
			throw new BusinessException(ErrorCode.OFFICE_NOT_FOUND);
		}

		var entity = optional.get();
		this.userPersistence.delete(entity);

		return new GenericResponseDto<>(true);
	}

	private UserDto transform(UserDO entity) {
		UserDto dto = null;
		if (entity != null) {
			dto = this.mapper.map(entity, UserDto.class);
		}
		return dto;
	}
}
