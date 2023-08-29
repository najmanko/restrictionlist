package cz.csas.restrictionlist.model

import java.time.OffsetDateTime

class RestrictedPerson(
        var cluid: String,
        var restrictionType: RestrictionType,
        var signed: Boolean,
        var validFrom: OffsetDateTime?,
        var validTo: OffsetDateTime?,
        var changed: Boolean
) {
        constructor() : this("", RestrictionType.BY, false, OffsetDateTime.now(),null, false)
}