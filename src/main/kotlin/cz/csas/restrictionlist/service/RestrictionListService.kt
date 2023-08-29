package cz.csas.restrictionlist.service

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.repository.RestrictedPersonRepository
import cz.csas.restrictionlist.util.mapper.RestrictedPersonMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
class RestrictionListService(
    private val cpopsSoapClient: CpopsSoapClient,
    private val restrictedPersonRepository: RestrictedPersonRepository,
    private val excelDataReader: ExcelDataReader,
    private val restrictedPersonMapper: RestrictedPersonMapper
) {

    private val logger = LoggerFactory.getLogger(RestrictionListService::class.java)

    fun getAllRestrictedPersons(): List<RestrictedPerson> {
        return restrictedPersonRepository.findAll().map { restrictedPersonMapper.mapEntityToRestrictedPerson(it) }
    }

    fun getRestrictedPersonByCluid(cluid: String): Optional<RestrictedPerson> {
            return restrictedPersonRepository.findById(cluid).map { restrictedPersonMapper.mapEntityToRestrictedPerson(it) }
        }

    fun updateRestrictedPersons() {
        val excelStream = cpopsSoapClient.getRestrictedPersonsExcelFile()
        val restrictedPersonsFromFile = excelDataReader.readRestrictedPersonsFromExcel(excelStream)
        val restrictedPersonsFromDBMap = restrictedPersonRepository.findAll()
            .map { entity -> restrictedPersonMapper.mapEntityToRestrictedPerson(entity) }
            .associateBy { it.cluid }
            .toMutableMap()
        val addOrUpdateRestrictedPersonMap = mutableMapOf<String, RestrictedPerson>()

        for (personFromFile in restrictedPersonsFromFile) {
            if (personFromFile.changed) {
                val loadedPerson = restrictedPersonsFromDBMap[personFromFile.cluid]
                if (loadedPerson?.validFrom != null) personFromFile.validFrom = loadedPerson.validFrom
                addOrUpdateRestrictedPersonMap[personFromFile.cluid] = personFromFile
            } else {
                if (!restrictedPersonsFromDBMap.containsKey(personFromFile.cluid)) {
                    addOrUpdateRestrictedPersonMap[personFromFile.cluid] = personFromFile
                    logger.info("Missing unchanged restricted person with CLUID ${personFromFile.cluid} was created")
                }
            }
        }

        val allRestrictedPersonForUpdate =
            addOrUpdateRestrictedPersonMap.values.toList() + getRestrictedPersonsWithEndOfValidation(
                restrictedPersonsFromDBMap,
                addOrUpdateRestrictedPersonMap
            )

        restrictedPersonRepository.saveAll(
            allRestrictedPersonForUpdate.map { person -> restrictedPersonMapper.mapToEntity(person) })
    }

    private fun getRestrictedPersonsWithEndOfValidation(
        restrictedPersonsFromDBMap: MutableMap<String, RestrictedPerson>,
        addOrUpdateRestrictedPersonMap: MutableMap<String, RestrictedPerson>,
    ): List<RestrictedPerson> {

        val now = OffsetDateTime.now()

        return restrictedPersonsFromDBMap
            .filter { (key, value) -> key !in addOrUpdateRestrictedPersonMap.keys && value.validTo == null }
            .values
            .map { person -> person.apply { person.validTo = now } }
    }
}