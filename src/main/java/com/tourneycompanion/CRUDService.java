package com.tourneycompanion;

import com.tourneycompanion.data.SelfIdentifiedBaseEntity;
import com.tourneycompanion.domain.DTO;
import com.tourneycompanion.domain.DTOValidator;
import com.tourneycompanion.domain.Mapper;
import com.tourneycompanion.exception.ServiceException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * Base Service loaded with common CRUD operations.
 * @param <E> The JPA entity that the service directly interfaces with.
 * @param <D> The DTO object that represents the underlying Entity
 * @param <R> The repository that handles DB operations for Entity {@link E}
 */
public abstract class CRUDService<
        E extends SelfIdentifiedBaseEntity,
        D extends DTO,
        R extends CrudRepository<E, Long>>{

    protected final R databaseRepository;
    protected final Mapper<E, D> mapper;
    protected final DTOValidator<D> validator;

    protected CRUDService(
            R databaseRepository,
            Mapper<E, D> mapper,
            DTOValidator<D> validator) {
        this.databaseRepository = databaseRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    /**
     * Searches for an entity in the database by ID. If found, it maps that entity to a {@link DTO} object.
     * @param id entity ID to search for
     * @return domain object mapped from entity
     */
    @Transactional
    public D getById(final Long id) {
        return Optional.ofNullable(id)
                .map(this::getEntityById)
                .map(mapper::fromEntity)
                .orElseThrow(() -> new ServiceException(
                        String.format("Failed to retrieve item with ID %d.", id),
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                ));
    }

    /**
     * Maps the {@link DTO} object to an entity and attempts to persist it in the database.
     * @param dto domain object containing information to be saved
     * @return DTO representation of the newly created entity
     */
    @Transactional
    public D create(final D dto) {
        return save(withoutId(dto), DTOValidator.Operation.CREATE);
    }

    /**
     * Maps the {@link DTO} object to an entity and attempts to update it in the database.
     * @param dto domain object containing information to be saved
     * @return DTO representation of the updated item
     */
    @Transactional
    public D update(final D dto) {
        return save(dto, DTOValidator.Operation.UPDATE);
    }

    /**
     * Deletes the item corresponding to the provided ID.
     * @param id ID of item to be deleted
     */
    @Transactional
    public void delete(final Long id){
        E entity = Optional.ofNullable(id)
                .flatMap(databaseRepository::findById)
                .orElseThrow(() -> new ServiceException(
                        String.format("Item with ID %d doesn't exist. Cannot delete it.", id),
                        Response.Status.NOT_FOUND.getStatusCode()
                ));

        databaseRepository.delete(entity);
    }


    /**
     * Retrieves an entity from the database by ID.
     * Throws a {@link ServiceException} if an entity is not found.
     * @param id ID to lookup by
     * @return Entity that corresponds to provided ID
     */
    private E getEntityById(final Long id) {
        return databaseRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                                String.format("Unable to find item with ID %d.",  id),
                                Response.Status.NOT_FOUND.getStatusCode()
                        )
                );
    }

    /**
     * Maps the {@link DTO} object to an entity and attempts to persist it in the database.
     * @param dto domain object containing information to be saved
     * @param operation CRUD operation being performed, controls validation actions
     * @return DTO representation of the saved item
     */
    private D save(final D dto, final DTOValidator.Operation operation){

        List<String> violations = validator.validate(dto, operation);
        if (!CollectionUtils.isEmpty(violations)){
            throw new ServiceException(
                    "Can't save item due to validation failures.",
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    violations
            );
        }

        return Optional.ofNullable(dto)
                .map(mapper::toEntity)
                .map(databaseRepository::save)
                .map(mapper::fromEntity)
                .orElseThrow(() -> new ServiceException(
                        "Failed to save supplied item.",
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                ));
    }

    /**
     * Removes the ID from a {@link DTO} object and returns it for chaining
     * @param original DTO to remove ID from
     * @return reference to object for chaining
     */
    private D withoutId (final D original){
        return Optional.ofNullable(original)
                .map(dto -> {
                    dto.setId(null);
                    return dto;
                })
                .orElse(null);
    }

}
