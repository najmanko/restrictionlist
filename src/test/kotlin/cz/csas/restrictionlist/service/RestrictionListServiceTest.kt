package cz.csas.restrictionlist.service

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.model.RestrictionType
import cz.csas.restrictionlist.model.entity.RestrictedPersonEntity
import cz.csas.restrictionlist.repository.RestrictedPersonRepository
import cz.csas.restrictionlist.util.mapper.RestrictedPersonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor

import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.InputStream
import java.time.OffsetDateTime
import java.time.ZoneOffset

class RestrictionListServiceTest {

    @Mock
    private lateinit var cpopsSoapClient: CpopsSoapClient

    @Mock
    private lateinit var restrictedPersonRepository: RestrictedPersonRepository

    @Mock
    private lateinit var excelDataReader: ExcelDataReader

    init {
        MockitoAnnotations.openMocks(this)
    }

    val restrictionListService = RestrictionListService(cpopsSoapClient, restrictedPersonRepository, excelDataReader, RestrictedPersonMapper())
    private val DATE_1_8_2023_12_00 = OffsetDateTime.of(2023, 8, 1, 12, 0 , 0, 0, ZoneOffset.ofHours(12))
    private val MOCKED_CLUID = "mocked_cluid"

    @Test
    fun updateChangedRestrictedPersons() {
        //given
        val inputStream = mock(InputStream::class.java)
        `when`(cpopsSoapClient.getRestrictedPersonsExcelFile()).thenReturn(inputStream)
        `when`(restrictedPersonRepository.findAll())
            .thenReturn(listOf(RestrictedPersonEntity(MOCKED_CLUID, RestrictionType.US1,false,DATE_1_8_2023_12_00,null)))
        `when`(excelDataReader.readRestrictedPersonsFromExcel(inputStream))
            .thenReturn(listOf(RestrictedPerson(MOCKED_CLUID, RestrictionType.BY, true, null, null, true)))

        //when
        restrictionListService.updateRestrictedPersons()

        //then
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<RestrictedPersonEntity>>

        verify(restrictedPersonRepository).saveAll(captor.capture())

        val savedRestrictedPersons = captor.value
        assertEquals(1, savedRestrictedPersons.size)
        assertEquals(MOCKED_CLUID, savedRestrictedPersons[0].cluid)
        assertEquals(RestrictionType.BY, savedRestrictedPersons[0].restrictionType)
        assertTrue(savedRestrictedPersons[0].signed)
        assertEquals(DATE_1_8_2023_12_00.year, savedRestrictedPersons[0].validFrom!!.year)
        assertEquals(DATE_1_8_2023_12_00.month, savedRestrictedPersons[0].validFrom!!.month)
        assertEquals(DATE_1_8_2023_12_00.dayOfMonth, savedRestrictedPersons[0].validFrom!!.dayOfMonth)
        assertNull(savedRestrictedPersons[0].validTo)
    }

    @Test
    fun addChangedRestrictedPersonIfNotExistInDB() {
        //given
        val inputStream = mock(InputStream::class.java)
        `when`(cpopsSoapClient.getRestrictedPersonsExcelFile()).thenReturn(inputStream)
        `when`(restrictedPersonRepository.findAll()).thenReturn(emptyList())
        `when`(excelDataReader.readRestrictedPersonsFromExcel(inputStream))
            .thenReturn(listOf(RestrictedPerson(MOCKED_CLUID, RestrictionType.BY, true, DATE_1_8_2023_12_00, null, true)))

        //when
        restrictionListService.updateRestrictedPersons()

        //then
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<RestrictedPersonEntity>>

        verify(restrictedPersonRepository).saveAll(captor.capture())

        val savedRestrictedPersons = captor.value
        assertEquals(1, savedRestrictedPersons.size)
        assertEquals(MOCKED_CLUID, savedRestrictedPersons[0].cluid)
        assertEquals(RestrictionType.BY, savedRestrictedPersons[0].restrictionType)
        assertTrue(savedRestrictedPersons[0].signed)
        assertEquals(DATE_1_8_2023_12_00.year, savedRestrictedPersons[0].validFrom!!.year)
        assertEquals(DATE_1_8_2023_12_00.month, savedRestrictedPersons[0].validFrom!!.month)
        assertEquals(DATE_1_8_2023_12_00.dayOfMonth, savedRestrictedPersons[0].validFrom!!.dayOfMonth)
        assertNull(savedRestrictedPersons[0].validTo)
    }

    @Test
    fun addUnchangedRestrictedPersonsIfNotExistInDB() {
        //given
        val inputStream = mock(InputStream::class.java)
        `when`(cpopsSoapClient.getRestrictedPersonsExcelFile()).thenReturn(inputStream)
        `when`(restrictedPersonRepository.findAll()).thenReturn(emptyList())
        `when`(excelDataReader.readRestrictedPersonsFromExcel(inputStream))
            .thenReturn(listOf(RestrictedPerson(MOCKED_CLUID, RestrictionType.BY, true, DATE_1_8_2023_12_00, null, false)))

        //when
        restrictionListService.updateRestrictedPersons()

        //then
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<RestrictedPersonEntity>>

        verify(restrictedPersonRepository).saveAll(captor.capture())

        val savedRestrictedPersons = captor.value
        assertEquals(1, savedRestrictedPersons.size)
        assertEquals(MOCKED_CLUID, savedRestrictedPersons[0].cluid)
        assertEquals(RestrictionType.BY, savedRestrictedPersons[0].restrictionType)
        assertTrue(savedRestrictedPersons[0].signed)
        assertEquals(DATE_1_8_2023_12_00.year, savedRestrictedPersons[0].validFrom!!.year)
        assertEquals(DATE_1_8_2023_12_00.month, savedRestrictedPersons[0].validFrom!!.month)
        assertEquals(DATE_1_8_2023_12_00.dayOfMonth, savedRestrictedPersons[0].validFrom!!.dayOfMonth)
        assertNull(savedRestrictedPersons[0].validTo)
    }

    @Test
    fun setValidToDateForRestrictedPersonsMissingInImport() {
        //given
        val inputStream = mock(InputStream::class.java)
        `when`(cpopsSoapClient.getRestrictedPersonsExcelFile()).thenReturn(inputStream)
        `when`(restrictedPersonRepository.findAll())
            .thenReturn(listOf(RestrictedPersonEntity(MOCKED_CLUID, RestrictionType.US1,false,null,null)))
        `when`(restrictedPersonRepository.findAllByValidToIsNull())
            .thenReturn(listOf(RestrictedPersonEntity(MOCKED_CLUID, RestrictionType.US1,false,null,null)))
        `when`(excelDataReader.readRestrictedPersonsFromExcel(inputStream)).thenReturn(emptyList())
        val now = OffsetDateTime.now()

        //when
        restrictionListService.updateRestrictedPersons()

        //then
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<RestrictedPersonEntity>>

        verify(restrictedPersonRepository).saveAll(captor.capture())

        val savedRestrictedPersons = captor.value
        assertEquals(1, savedRestrictedPersons.size)
        assertEquals(MOCKED_CLUID, savedRestrictedPersons[0].cluid)
        assertEquals(RestrictionType.US1, savedRestrictedPersons[0].restrictionType)
        assertFalse(savedRestrictedPersons[0].signed)
        assertEquals(now.year, savedRestrictedPersons[0].validTo!!.year)
        assertEquals(now.month, savedRestrictedPersons[0].validTo!!.month)
        assertEquals(now.dayOfMonth, savedRestrictedPersons[0].validTo!!.dayOfMonth)
        assertNull(savedRestrictedPersons[0].validFrom)
    }
}