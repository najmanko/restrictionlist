package cz.csas.restrictionlist.model.entity

import cz.csas.restrictionlist.model.RestrictionType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "restricted_person")
data class RestrictedPersonEntity(
        @Id
        var cluid: String,
        var restrictionType: RestrictionType,
        var signed: Boolean,
        var validFrom: OffsetDateTime?,
        var validTo: OffsetDateTime?
) {
        constructor() : this("", RestrictionType.US1,false,null,null)
}