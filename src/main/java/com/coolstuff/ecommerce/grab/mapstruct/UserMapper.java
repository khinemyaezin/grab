package com.coolstuff.ecommerce.grab.mapstruct;

import com.coolstuff.ecommerce.grab.dto.UserData;
import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {
    UserData map(UserEntity source);
    @AfterMapping
    default void map(UserEntity source, @MappingTarget UserData.UserDataBuilder target) {
        target.name( String.format("%s %s", source.getFirstName(), source.getLastName()));
    }
}
