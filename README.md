# Masterello Server monolith

## Contribution guideline

### Module structure:
```agsl
-app [contains SpringBoot Application and properties]
-db  [contains sql migrations]
-commons 
---common-core
---common-security
---common-test 
-auth
---auth-impl
---auth-contract
---auth-contract-test

```



### To introduce a new module:
* create a directory "moduleA"
* inside the new directory create three modules 
  * "moduleA-impl" - contains the implementation (controllers, services etc)
  * "moduleA-contract" - interfaces and data files to be shared with another modules to maintain module segregation. No implementation should be included into this module.
  * "moduleA-contact-test" - contains test configuration with mocked beans to be used by dependant modules in integration tests
* configure modules in `settings.gradle`

### Security
Each module which exposes API should have its own SecurityConfig configured for module specific path, e.g.:
* auth module API `api/auth/**`
* user module API `api/user/**`
* worker module API `api/worker/**`

There're three authentication mechanisms:
1. auth endpoints to work with tokens (create/refresh/introspect token) use Basic client auth (clientId/clientSecret)
2. default endpoint auth is based on access token cookie (m_token)
3. SuperAdmin authentication:
There're two endpoints with a different kind of auth SuperAdmin auth. 
* `POST /api/auth/client` - Admin endpoint to register a client (generate clientId/clientSecret). This has to be authenticated differently cause we cannot authenticate with client credentials until we create the first client.
* `POST /api/user/admin/<user-uuid>/add-role` - an Admin endpoint to assign an Admin role.

SuperAdmin auth is required while setting up a new environment, later it can be disabled with config properties:
```yaml
masterello:
  security:
    admin:
      enabled: true
      username: "admin@masterello.com"
      password: ${SUPER_ADMIN_PASS}
```

## CI-CD

On merge to main branch Docker image is built and pushed to DigitalOcean Container registry with two tags: version-tag and latest tag
DigitalOcean platfrom app is configured to autodeploy on new image with tag latest.


## Create migration changeset
Flyway apply only specific migration changeset specified under the db/migration folder
and does not apply any other migrations in other folders
Different versions cac be separated (example: 1.0.0 and others)

### Example:
* Create migration: ./gradlew createMigration -PmigrationName="migration_name_separated_by_underscore"