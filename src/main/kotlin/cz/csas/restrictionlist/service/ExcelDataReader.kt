package cz.csas.restrictionlist.service

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.model.RestrictionType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.OffsetDateTime

@Service
class ExcelDataReader {

    private val RUSSIAN_PERSONS_TAB_NAME = "Fyzické osoby Rusko"
    private val USA_RESTRICTIONS_TAB_NAME = "Sankce USA"

    private val CLUID_COLUMN_HEADER = "CLUID"
    private val CHANGE_COLUMN_HEADER = "Stav oproti minulému reportu"
    private val CHANGE_NOTE = "Změna"
    private val WITHOUT_CHANGE_NOTE = "Bez změny"
    private val WITHOUT_CHANGE_NOTE2 = "Beze změny"
    private val WITHOUT_CHANGE_NOTE3 = "Nový"

    private val HEADER_ROW = 1

    private val logger = LoggerFactory.getLogger(ExcelDataReader::class.java)

    fun readRestrictedPersonsFromExcel(excelInputStream: InputStream): List<RestrictedPerson> {
        val persons = mutableListOf<RestrictedPerson>()

        try {
            val workbook = XSSFWorkbook(excelInputStream)
            persons.addAll(readRuAndByFromExcel(workbook.getSheet(RUSSIAN_PERSONS_TAB_NAME)))
            persons.addAll(readUsaFromExcel(workbook.getSheet(USA_RESTRICTIONS_TAB_NAME)))
            workbook.close()
        } catch (e: Exception) {
            logger.error("Can not read from Excel file inputStream, because of error: ${e.message}")
        } finally {
            excelInputStream.close()
        }

        return persons
    }

    private fun readRuAndByFromExcel(sheet: Sheet): List<RestrictedPerson> {
        val cluidColumnIndex = getColumnIndexByHeader(sheet, CLUID_COLUMN_HEADER)
        val ruTypeColumnIndex = getColumnIndexByHeader(sheet, "Echt Rus")
        val byTypeColumnIndex = getColumnIndexByHeader(sheet, "Echt Bělorus")
        val changedColumnIndex = getColumnIndexByHeader(sheet, CHANGE_COLUMN_HEADER)
        val signed = false //TODO bude RU BY zbieratt eSpis podpis?

        val persons = mutableListOf<RestrictedPerson>()

        for (row in sheet) {
            if (row.rowNum <= HEADER_ROW) continue
            val restrictedPerson = RestrictedPerson()
            val cluid = row.getCell(cluidColumnIndex)?.stringCellValue ?: continue
            restrictedPerson.cluid = cluid
            //TODO co ak je rus i belorus nula?
            val ruValue = row.getCell(ruTypeColumnIndex)?.numericCellValue ?: continue

            restrictedPerson.restrictionType = if (ruValue.toInt() == 1) RestrictionType.RU else RestrictionType.BY
            restrictedPerson.signed = signed
            restrictedPerson.validFrom = OffsetDateTime.now()

            val changedState = row.getCell(changedColumnIndex)?.stringCellValue ?: continue
            restrictedPerson.changed = getChangedState(changedState, cluid) ?: continue

            persons.add(restrictedPerson)
        }
        return persons
    }

    private fun readUsaFromExcel(sheet: Sheet): List<RestrictedPerson> {
        val cluidColumnIndex = getColumnIndexByHeader(sheet, CLUID_COLUMN_HEADER)
        val signedColumnIndex = getColumnIndexByHeader(sheet, "Dotazník eSpis")
        val restrictedTypeColumnIndex = getColumnIndexByHeader(sheet, "Skupina")
        val changedColumnIndex = getColumnIndexByHeader(sheet, CHANGE_COLUMN_HEADER)

        val persons = mutableListOf<RestrictedPerson>()

        for (row in sheet) {
            if (row.rowNum <= HEADER_ROW) continue
            val restrictedPerson = RestrictedPerson()
            val cluid = row.getCell(cluidColumnIndex)?.stringCellValue ?: continue
            restrictedPerson.cluid = cluid
            val restrictionTypeNumber = row.getCell(restrictedTypeColumnIndex)?.numericCellValue ?: continue

            val restrictionType = when (restrictionTypeNumber.toInt()) {
                1 -> RestrictionType.US1
                2 -> RestrictionType.US2
                3 -> RestrictionType.US3
                4 -> RestrictionType.US4
                else -> {
                    logger.error("Can not parse US restriction type from number ${restrictionTypeNumber}" +
                            " from Excel file for CLUID: ${cluid}")
                    continue
                }
            }

            restrictedPerson.restrictionType = restrictionType

            val signed = row.getCell(signedColumnIndex)?.numericCellValue ?: continue
            restrictedPerson.signed = signed.toInt() == 1
            restrictedPerson.validFrom = OffsetDateTime.now()

            val changedState = row.getCell(changedColumnIndex)?.stringCellValue ?: continue
            restrictedPerson.changed = getChangedState(changedState, cluid) ?: continue

            persons.add(restrictedPerson)
        }
        return persons
    }

    private fun getColumnIndexByHeader(sheet: Sheet, header: String): Int {
        val headerRow = sheet.getRow(HEADER_ROW)
        for (cell in headerRow) {
            val cellValue = cell.stringCellValue
            if (cellValue.equals(header, ignoreCase = true)) {
                return cell.columnIndex
            }
        }
        throw throw NoSuchElementException("Column ${header} not found in sheet ${sheet.sheetName} in Excel file inputStream.")
    }

    private fun getChangedState(changedState: String, cluid: String): Boolean? {
        return when (changedState) {
            CHANGE_NOTE -> true
            WITHOUT_CHANGE_NOTE -> false
            WITHOUT_CHANGE_NOTE2 -> false
            WITHOUT_CHANGE_NOTE3 -> false
            else -> {
                logger.error("Can not read changed state ${changedState} from Excel file for CLUID: ${cluid}")
                null
            }
        }
    }
}