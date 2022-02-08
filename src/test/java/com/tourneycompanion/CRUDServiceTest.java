package com.tourneycompanion;

import com.tourneycompanion.data.SelfIdentifiedBaseEntity;
import com.tourneycompanion.domain.DTO;
import com.tourneycompanion.domain.DTOValidator;
import com.tourneycompanion.domain.Mapper;
import com.tourneycompanion.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CRUDServiceTest {

    // Sample Data
    TestEntity entity;
    TestDTO dto;

    // Components
    TestRepository repository;
    TestMapper mapper;
    CrudServiceTestImpl service;
    TestValidator validator;

    @BeforeEach
    void setup() {

        dto = new TestDTO(1L);
        entity = new TestEntity(1L);
        repository = mock(TestRepository.class);
        mapper = new TestMapper();
        validator = new TestValidator();
        service = new CrudServiceTestImpl(repository, mapper, validator);
    }

    @Test
    void test_getById_happyPath(){
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TestDTO result = service.getById(1L);
        TestDTO expected = new TestDTO(1L);

        assertThat(result)
                .as("DTO with data matching that of entity is returned after successful save.")
                .isEqualTo(expected);
    }

    @Test
    void test_getById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ServiceException.class)
                .as("Proper exception is thrown if can't find entity with provided ID.")
                .isThrownBy(() -> service.getById(1L))
                .withMessage("Unable to find item with ID 1.");
    }

    @Test
    void test_getById_idNotProvided() {
        assertThatExceptionOfType(ServiceException.class)
                .as("Proper exception is thrown if entity ID isn't provided.")
                .isThrownBy(() -> service.getById(null))
                .withMessage("Failed to retrieve item with ID null.");
    }

    @Test
    void test_create_happyPath() {
        when(repository.save(any())).thenReturn(entity);

        TestDTO result = service.create(dto);
        TestDTO expected = new TestDTO(1L);

        assertThat(result)
                .as("DTO with ID mapped is returned after successful creation.")
                .isEqualTo(expected);
    }

    @Test
    void test_create_invalidData() {
        assertThatExceptionOfType(ServiceException.class)
                .as("Proper exception is thrown if invalid data is provided.")
                .isThrownBy(() -> service.create(null))
                .withMessage("Can't save item due to validation failures.");
    }

    @Test
    void test_create_saveFailure() {
        assertThatExceptionOfType(ServiceException.class)
                .as("Proper exception is thrown if system failure occurs during save.")
                .isThrownBy(() -> service.create(new TestDTO(1L)))
                .withMessage("Failed to save supplied item.");
    }

    @Test
    void test_update_happyPath(){
        when(repository.save(any())).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TestDTO result = service.update(dto);
        TestDTO expected = new TestDTO(1L);

        assertThat(result)
                .as("DTO with ID mapped is returned after successful update.")
                .isEqualTo(expected);
    }

    @Test
    void test_delete_happyPath(){
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThatNoException()
                .as("Entity can be deleted successfully.")
                .isThrownBy(() -> service.delete(1L));
    }

    @Test
    void test_delete_missingId(){
        assertThatExceptionOfType(ServiceException.class)
                .as("Failure occurs if ID isn't provided.")
                .isThrownBy(() -> service.delete(null))
                .withMessage("Item with ID null doesn't exist. Cannot delete it.");
    }

    @Test
    void test_deleteUser_userNotFound(){
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ServiceException.class)
                .as("Failure occurs if system cannot locate entity with given ID.")
                .isThrownBy(() -> service.delete(1L))
                .withMessage("Item with ID 1 doesn't exist. Cannot delete it.");
    }

    // Defining Mock Implementations for Testing

    static class CrudServiceTestImpl extends CRUDService<TestEntity, TestDTO, TestRepository>{

        CrudServiceTestImpl(TestRepository databaseRepository, Mapper<TestEntity, TestDTO> mapper, DTOValidator<TestDTO> validator) {
            super(databaseRepository, mapper, validator);
        }
    }

    static class TestEntity extends SelfIdentifiedBaseEntity {
        TestEntity(Long id) {setId(id);}
    }

    static class TestDTO extends DTO {
        TestDTO(Long id) {setId(id);}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DTO dto = (DTO) o;
            return Objects.equals(getId(), dto.getId());
        }
    }

    static class TestMapper implements Mapper<TestEntity, TestDTO>{

        @Override
        public TestEntity toEntity(TestDTO dto) {
            return new TestEntity(dto.getId());
        }

        @Override
        public TestDTO fromEntity(TestEntity entity) {
            return new TestDTO(entity.getId());
        }
    }

    interface TestRepository extends CrudRepository<TestEntity, Long>{}

    static class TestValidator implements DTOValidator<TestDTO>{
        @Override
        public List<String> validate(TestDTO dtoObject, Operation operation) {
            if (dtoObject == null){
                return Collections.singletonList("No DTO Provided.");
            } else {
                return new ArrayList<>();
            }
        }
    }

}
