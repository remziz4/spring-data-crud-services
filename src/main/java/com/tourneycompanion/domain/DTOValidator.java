package com.tourneycompanion.domain;

import java.util.List;

public interface DTOValidator<T> {

    List<String> validate (T dtoObject, Operation operation);

    enum Operation {
        CREATE,
        UPDATE
    }

}
