package cz.csas.restrictionlist.controller

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.model.RestrictionType
import cz.csas.restrictionlist.service.RestrictionListService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(RestrictedPersonController::class)
class RestrictedPersonControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var restrictionListService: RestrictionListService

    @Test
    fun `test getRestrictedPersons`() {
        val person1 = RestrictedPerson("cluid1", RestrictionType.BY, true, null, null, false)
        val person2 = RestrictedPerson("cluid2", RestrictionType.BY, true, null, null, false)
        val persons = listOf(person1, person2)

        `when`(restrictionListService.getAllRestrictedPersons()).thenReturn(persons)

        mockMvc.perform(get("/api/restrictedpersons"))
            .andExpect(status().isOk)
            .andExpect(content().json("[{\"cluid\":\"cluid1\",\"restrictionType\":\"BY\",\"signed\":true,\"validFrom\":null,\"validTo\":null,\"changed\":false},{\"cluid\":\"cluid2\",\"restrictionType\":\"BY\",\"signed\":true,\"validFrom\":null,\"validTo\":null,\"changed\":false}]"))
    }

    @Test
    fun `test getRestrictedPersonByCluid - person found`() {
        val cluid = "cluid1"
        val person = RestrictedPerson(cluid, RestrictionType.BY, true, null, null, false)


        `when`(restrictionListService.getRestrictedPersonByCluid(cluid)).thenReturn(Optional.of(person))

        mockMvc.perform(get("/api/restrictedpersons/$cluid"))
            .andExpect(status().isOk)
            .andExpect(content().json("{\"cluid\":\"cluid1\",\"restrictionType\":\"BY\",\"signed\":true,\"validFrom\":null,\"validTo\":null,\"changed\":false}"))
    }

    @Test
    fun `test getRestrictedPersonByCluid - person not found`() {
        val cluid = "cluid3"

        `when`(restrictionListService.getRestrictedPersonByCluid(cluid)).thenReturn(Optional.empty())

        mockMvc.perform(get("/api/restrictedpersons/$cluid"))
            .andExpect(status().isNotFound)
            .andExpect(content().string("Restricted person with CLUID: '$cluid' not found"))
    }
}
