package cz.csas.restrictionlist.controller

import cz.csas.restrictionlist.model.RestrictedPerson
import cz.csas.restrictionlist.service.RestrictionListService
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/restrictedpersons")
class RestrictedPersonController(
    private val restrictionListService: RestrictionListService
) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> = ResponseEntity(e.message, NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<String> = ResponseEntity(e.message, BAD_REQUEST)

    @GetMapping
    fun getRestrictedPersons(): List<RestrictedPerson> {
        return restrictionListService.getAllRestrictedPersons()
    }

    @GetMapping("/{cluid}")
    fun getRestrictedPersons(@PathVariable cluid: String): ResponseEntity<Any> {
        val person = restrictionListService.getRestrictedPersonByCluid(cluid)

        return if (person.isPresent) {
            ResponseEntity.ok(person.get())
        } else {
            ResponseEntity.status(NOT_FOUND).body("Restricted person with CLUID: '$cluid' not found")
        }
    }
}
