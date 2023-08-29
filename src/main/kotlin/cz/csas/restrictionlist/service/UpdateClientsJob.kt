package cz.csas.restrictionlist.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class UpdateClientsJob(
    private val restrictionListService: RestrictionListService
) {

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours in milliseconds
    fun updateClientsDataEverySixHours() {
        restrictionListService.updateRestrictedPersons()
    }
}