package cz.csas.restrictionlist.controller

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.model.RestrictionType
import cz.csas.restrictionlist.service.RestrictionListService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

class RestrictedPersonControllerTest {

    @Mock
    private lateinit var restrictionListService: RestrictionListService

    @InjectMocks
    private lateinit var restrictedPersonController: RestrictedPersonController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test getRestrictedPersons`() {
        val person1 = RestrictedPerson("MOCKED_CLUID", RestrictionType.BY, true, null, null, false)
        val person2 = RestrictedPerson("MOCKED_CLUID2", RestrictionType.BY, true, null, null, false)
        val persons = listOf(person1, person2)

        `when`(restrictionListService.getAllRestrictedPersons()).thenReturn(persons)

        val response = restrictedPersonController.getRestrictedPersons()

        assertEquals(persons, response)
    }

    @Test
    fun `test getRestrictedPersonByCluid - person found`() {
        val cluid = "MOCKED_CLUID"
        val person = RestrictedPerson(cluid, RestrictionType.BY, true, null, null, false)

        `when`(restrictionListService.getRestrictedPersonByCluid(cluid)).thenReturn(Optional.of(person))

        val response = restrictedPersonController.getRestrictedPersons(cluid)

        assertEquals(ResponseEntity.ok(person), response)
    }

    @Test
    fun `test getRestrictedPersonByCluid - person not found`() {
        val cluid = "cluid3"

        `when`(restrictionListService.getRestrictedPersonByCluid(cluid)).thenReturn(Optional.empty())

        val response = restrictedPersonController.getRestrictedPersons(cluid)

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Restricted person with CLUID: '$cluid' not found"), response)
    }
}
