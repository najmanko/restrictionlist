# Kotlin + Spring Boot project: restrictionlist

This web service read data from Excel file with clients, persists it into DB and provides REST API for this clients.

### Run application

mvn spring-boot:run

### Check Database Changes:
If you've enabled the H2 console, you can check the database schema changes using the H2 console after the application is started. 
Use the URL [http://localhost:8080/h2-console](http://localhost:8080/h2-console) and connect to the H2 database using the JDBC URL jdbc:h2:mem:testdb. You should see the CLIENT table created by the Flyway migration.

You can test GET method by accessing [http://localhost:8080/api/restrictedpersons/123](http://localhost:8080/api/restrictedpersons/123) in your web browser. 123 is client ID from your database.
To GET list of all cleints send GET to [http://localhost:8080/api/restrictedpersons](http://localhost:8080/api/restrictedpersons).
