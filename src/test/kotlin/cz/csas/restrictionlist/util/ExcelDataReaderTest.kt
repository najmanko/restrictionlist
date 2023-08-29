package cz.csas.restrictionlist.util

import cz.csas.restrictionlist.model.RestrictionType
import cz.csas.restrictionlist.service.ExcelDataReader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ExcelDataReaderTest {

    @Test
    fun readRestrictedPersonsFromExcel() {
        //given
        val reader = ExcelDataReader()
        val file = this.javaClass.classLoader.getResourceAsStream("testdata/Sankce  Rusko a Bělorusko.xlsx")
        val now = OffsetDateTime.now()

        //when
        val restrictedPersons = reader.readRestrictedPersonsFromExcel(file)

        //then
        assertEquals(19973 + 3597, restrictedPersons.size)

        val restrictedPersonsFromDBMap = restrictedPersons.associateBy { it.cluid }.toMutableMap()

        val rusUnchanged = restrictedPersonsFromDBMap["9007-07-25-08.55.31.375443"]
        assertEquals(RestrictionType.RU, rusUnchanged!!.restrictionType)
        assertFalse(rusUnchanged.changed)
        assertValidationDate(now, rusUnchanged.validFrom!!)
        assertNull(rusUnchanged.validTo)

        val ruChanged = restrictedPersonsFromDBMap["8022-03-07-13.37.14.272337"]
        assertEquals(RestrictionType.RU, ruChanged!!.restrictionType)
        assertTrue(ruChanged.changed)
        assertValidationDate(now, ruChanged.validFrom!!)
        assertNull(ruChanged.validTo)

        val byUnchanged = restrictedPersonsFromDBMap["8012-02-24-11.26.11.008591"]
        assertEquals(RestrictionType.BY, byUnchanged!!.restrictionType)
        assertFalse(byUnchanged.changed)
        assertValidationDate(now, byUnchanged.validFrom!!)
        assertNull(byUnchanged.validTo)

        val byChanged = restrictedPersonsFromDBMap["8019-09-06-09.51.28.106128"]
        assertEquals(RestrictionType.BY, byChanged!!.restrictionType)
        assertTrue(byChanged.changed)
        assertValidationDate(now, byChanged.validFrom!!)
        assertNull(byChanged.validTo)

        val us1UnchangedSigned = restrictedPersonsFromDBMap["1997-04-09-17.07.27.811657"]
        assertEquals(RestrictionType.US1, us1UnchangedSigned!!.restrictionType)
        assertFalse(us1UnchangedSigned.changed)
        assertTrue(us1UnchangedSigned.signed)
        assertValidationDate(now, us1UnchangedSigned.validFrom!!)
        assertNull(us1UnchangedSigned.validTo)

        val us2UnchangedUnsigned = restrictedPersonsFromDBMap["8021-11-23-10.39.25.033150"]
        assertEquals(RestrictionType.US2, us2UnchangedUnsigned!!.restrictionType)
        assertFalse(us2UnchangedUnsigned.changed)
        assertFalse(us2UnchangedUnsigned.signed)
        assertValidationDate(now, us2UnchangedUnsigned.validFrom!!)
        assertNull(us2UnchangedUnsigned.validTo)

        val us3 = restrictedPersonsFromDBMap["1997-04-09-17.05.41.386355"]
        assertEquals(RestrictionType.US3, us3!!.restrictionType)

        val us4 = restrictedPersonsFromDBMap["8007-09-07-10.51.09.758316"]
        assertEquals(RestrictionType.US4, us4!!.restrictionType)

        //TODO u US je nieco so zmenou??
    }

    private fun assertValidationDate(now: OffsetDateTime, date: OffsetDateTime) {
        assertEquals(now.dayOfMonth, date.dayOfMonth)
        assertEquals(now.month, date.month)
        assertEquals(now.year, date.year)
    }
}