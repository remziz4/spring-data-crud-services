package com.tourneycompanion.domain;

import com.tourneycompanion.data.SelfIdentifiedBaseEntity;

/**
 *
 * @param <E> Entity to map to and from
 * @param <D> DTO object o map to and from
 */
public interface Mapper<E extends SelfIdentifiedBaseEntity, D extends DTO> {

    E toEntity (D dto);

    D fromEntity (E entity);

}
