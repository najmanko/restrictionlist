package cz.csas.restrictionlist.util.mapper

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.model.entity.RestrictedPersonEntity
import org.springframework.stereotype.Component

@Component
class RestrictedPersonMapper {
    fun mapEntityToRestrictedPerson(entity: RestrictedPersonEntity): RestrictedPerson {
        return RestrictedPerson(
            entity.cluid,
            entity.restrictionType,
            entity.signed,
            entity.validFrom,
            entity.validTo,
            false
        )
    }

    fun mapToEntity(person: RestrictedPerson): RestrictedPersonEntity {
        return RestrictedPersonEntity(
            person.cluid,
            person.restrictionType,
            person.signed,
            person.validFrom,
            person.validTo
        )
    }
}