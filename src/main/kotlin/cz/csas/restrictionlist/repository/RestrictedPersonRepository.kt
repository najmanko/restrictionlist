package cz.csas.restrictionlist.repository

import cz.csas.restrictionlist.model.entity.RestrictedPersonEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RestrictedPersonRepository : JpaRepository<RestrictedPersonEntity, String> {
    fun findAllByValidToIsNull(): List<RestrictedPersonEntity>
}