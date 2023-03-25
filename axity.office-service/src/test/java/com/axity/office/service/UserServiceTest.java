package com.axity.office.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.axity.office.commons.dto.RoleDto;
import com.axity.office.commons.dto.UserDto;
import com.axity.office.commons.enums.ErrorCode;
import com.axity.office.commons.exception.BusinessException;
import com.axity.office.commons.request.PaginatedRequestDto;

/**
 * Class UserServiceTest
 * 
 * @author username@axity.com
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class UserServiceTest {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceTest.class);

	@Autowired
	private UserService userService;

	private RoleDto creatRole(int id) {
		var role = new RoleDto();
		role.setId(id);
		return role;
	}

	/**
	 * Method to validate the paginated search
	 */
	@Test
	void testFindUsers() {
		var request = new PaginatedRequestDto();
		request.setLimit(5);
		request.setOffset(0);
		var users = this.userService.findUsers(request);

		LOG.info("Response: {}", users);

		assertNotNull(users);
		assertNotNull(users.getData());
		assertFalse(users.getData().isEmpty());
	}

	/**
	 * Method to validate the search by id
	 * 
	 * @param userId
	 */
	@ParameterizedTest
	@ValueSource(ints = { 1 })
	void testFind(Integer userId) {
		var user = this.userService.find(userId);
		assertNotNull(user);
		LOG.info("Response: {}", user);
	}

	/**
	 * Method to validate the search by id inexistent
	 */
	@Test
	void testFind_NotExists() {
		var user = this.userService.find(999999);
		assertNull(user);
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#create(com.axity.office.commons.dto.UserDto)}.
	 */
	@Test
	void testCreate() {
		var dto = new UserDto();
		var roles = new ArrayList<RoleDto>();

		roles.add(creatRole(1));
		roles.add(creatRole(2));

		dto.setName("Prueba");
		dto.setUsername("Pruebita");
		dto.setEmail("some@email.com");
		dto.setLastName("Test");
		dto.setRoles(roles);

		var response = this.userService.create(dto);
		assertNotNull(response);
		assertEquals(0, response.getHeader().getCode());
		assertNotNull(response.getBody());

		this.userService.delete(dto.getId());
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#create(com.axity.office.commons.dto.UserDto)}.
	 */
	@Test
	void testVerifyExistEmail() {
		var dto = new UserDto();
		var roles = new ArrayList<RoleDto>();

		roles.add(creatRole(1));
		roles.add(creatRole(2));

		dto.setName("Prueba");
		dto.setUsername("Pruebita2");
		dto.setEmail("guy.stark@company.net"); // USED EMAIL
		dto.setLastName("Test");
		dto.setRoles(roles);

		var response = this.userService.create(dto);
		assertNotNull(response);
		assertEquals(406, response.getHeader().getCode());
		assertNull(response.getBody());
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#create(com.axity.office.commons.dto.UserDto)}.
	 */
	@Test
	void testVerifyExistUsername() {
		var dto = new UserDto();
		var roles = new ArrayList<RoleDto>();

		roles.add(creatRole(1));
		roles.add(creatRole(2));

		dto.setName("Prueba");
		dto.setUsername("ezekiel.farmer"); // USED USERNAME
		dto.setEmail("someEmail2@company.net");
		dto.setLastName("Test");
		dto.setRoles(roles);

		var response = this.userService.create(dto);
		assertNotNull(response);
		assertEquals(406, response.getHeader().getCode());
		assertNull(response.getBody());
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#create(com.axity.office.commons.dto.UserDto)}.
	 */
	@Test
	void testVerifySelectedRoles() {
		var dto = new UserDto();
		var roles = new ArrayList<RoleDto>();

		roles.add(creatRole(2));
		roles.add(creatRole(5)); // NOT A VALID ROL

		dto.setName("Prueba");
		dto.setUsername("Pruebita3");
		dto.setEmail("someEmail3@company.net");
		dto.setLastName("Test");
		dto.setRoles(roles);

		var response = this.userService.create(dto);
		assertNotNull(response);
		assertEquals(406, response.getHeader().getCode());
		assertNull(response.getBody());
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#create(com.axity.office.commons.dto.UserDto)}.
	 */
	@Test
	void testVerifyNullRoles() {
		var dto = new UserDto();
		var roles = new ArrayList<RoleDto>();

		dto.setName("Prueba");
		dto.setUsername("Pruebita3");
		dto.setEmail("someEmail3@company.net");
		dto.setLastName("Test");

		var response = this.userService.create(dto);
		assertNotNull(response);
		assertEquals(406, response.getHeader().getCode());
		assertNull(response.getBody());
	}


	/**
	 * Method to validate update
	 */
	@Test
	@Disabled("TODO: Actualizar la prueba de acuerdo a la entidad")
	void testUpdate() {
		var user = this.userService.find(1).getBody();
		// TODO: actualizar de acuerdo a la entidad

		var response = this.userService.update(user);

		assertNotNull(response);
		assertEquals(0, response.getHeader().getCode());
		assertTrue(response.getBody());
		user = this.userService.find(1).getBody();

		// Verificar que se actualice el valor
	}

	/**
	 * Method to validate an inexistent registry
	 */
	@Test
	void testUpdate_NotFound() {
		var user = new UserDto();
		user.setId(999999);
		var ex = assertThrows(BusinessException.class, () -> this.userService.update(user));

		assertEquals(ErrorCode.OFFICE_NOT_FOUND.getCode(), ex.getCode());
	}

	/**
	 * Test method for
	 * {@link com.axity.office.service.impl.UserServiceImpl#delete(java.lang.String)}.
	 */
	@Test
	void testDeleteNotFound() {
		var ex = assertThrows(BusinessException.class, () -> this.userService.delete(999999));
		assertEquals(ErrorCode.OFFICE_NOT_FOUND.getCode(), ex.getCode());
	}
}
