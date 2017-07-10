# otpApi
Repository for OTP Management API

## Requirements

- This API service will connect to an LDAP directory supporting axaOtp objectClass. It must be running before starting this API.
Complete the application.yml file in resources/ to specify the correct LDAP infos.

## Importing the project

On Eclipse, choose File > Import... > Import existing maven project

## Starting up

To launch the service with its embedded webserver, use the following maven command:

mvn spring-boot:run