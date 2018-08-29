# Exception handling for Spring REST APIs and Feign Clients

## Features

This project defines an exception model via swagger definition that is rendered into the http body
in case of the occurrence of an exception. The rendering is done by the ApiExceptionResolver that 
extends Spring's AbstractExceptionHandler and implements Spring's HandlerExceptionResolver. The
resolver supports the JSON and XML format. In case of other content types the body will be empty 
and some http headers will be added to the response.

A JSON output may look like this (it's configurable: you can also add causes and stack traces):

```json
{
  "message": "Pet already exists.",
  "errorCode": "PET_STORE:1234",
  "application": "petstore",
  "handlerClassName": "org.bremersee.petstore.controller.PetRestController",
  "handlerMethodName": "addPet",
  "handlerMethodParameterTypes": [
    "org.bremersee.petstore.model.Pet"
  ],
  "exceptionClassName": "org.bremersee.petstore.exception.AlreadyExistsException"
}
```

The message is the message from the exception or the reason that can be specified with Spring's
annotation @ResponseStatus. You can configure which message should be preferred. We default is to 
use the message of the exception unless it is not null or empty.

The error code can be set on the provided ServiceException of this project or on every exception
or handler method with the provided annotation @ErrorCode like you specify the http response status
with Spring's @ResponseStatus:

```java
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Pet already exists.")
@ErrorCode("PET_STORE:1234")
public class AlreadyExistsException extends RuntimeException {
}
```

The application, handlerClassName, handlerMethodName, handlerMethodParameterTypes and the 
exceptionClassName is added by the ApiExceptionResolver.

### The usage of ServiceException

A ServiceException is a RuntimeException where you can set the message, status and error code
via it's constructors or static builder methods:

```
throw new ServiceException(HttpStatus.NOT_FOUND, "MY_APP:PET_NOT_FOUND");
// or
String msg = "Pet with ID [" + id + "] was not found."
throw new ServiceException(404, msg, "MY_APP:PET_NOT_FOUND");
// or
throw ServiceException.notFoundWithErrorCode("Pet", id, "MY_APP:PET_NOT_FOUND");
``` 

### Rendering of 'normal' Java Exceptions

Java exceptions that are not annotated with @ResponseStatus and/or @ErrorCode can be configured with
the application properties of a Spring Boot Application. A default behaviour can be configured as
well as per exception class:

```yaml
bremersee:
  exception-resolver:
    default-exception-mapping:
      message: Internal server error
      status: 500
    exception-mappings:
      - exception-class-name: org.example.exception.FirstException
        message: Something went wrong
        status: 400
        code: MY_APP:FIRST
      - exception-class-name: org.example.exception.SecondException
        message: Something else went wrong
        status: 406
        code: MY_APP:SECOND
```

### Processing exceptions with a Feign Client

If you call a rest endpoint with a FeignClient and an exception occurs, you will normally get only
a message like 'status 500 reading PetRestController#getPets()'. The body of the response, that
may contain some important information for the client, gets lost as well as the response headers.

The error decoder for feign clients in this project reads the http body of the response and adds
it the FeignClientException of this project. Since it is assumed that the error description is in 
the specified format, it is attempted to parse the same. If this fails, the body content will be 
treated as the exception message. Is this FeignClientException is rendered again by the 
ApiExceptionResolver the result will be a chain of two exceptions. The first is the 
FeignClientException produced by the client, the second is the exception of the remote server:

```json
{
  "message": "status 404 reading PetFoodController#getPetFood()",
  "errorCode": "FOOD_STORE:4007",
  "application": "petstore",
  "handlerClassName": "org.bremersee.petstore.controller.PetController",
  "handlerMethodName": "listFoodForPet",
  "handlerMethodParameterTypes": [
    "org.bremersee.petstore.model.Pet"
  ],
  "exceptionClassName": "org.bremersee.common.exhandling.feign.FeignClientException",
  "cause": {
    "message": "We have no food for this pet.",
    "errorCode": "FOOD_STORE:4007",
    "application": "foodstore",
    "handlerClassName": "org.bremersee.foodstore.controller.PetFoodController",
    "handlerMethodName": "getPetFood",
    "handlerMethodParameterTypes": [],
    "exceptionClassName": "org.bremersee.foodstore.exception.NotFoundException"
  }
}
```

### Processing exceptions with Spring's RestTemplate

TODO (not yet implemented)

### Refer to the exception payload type in your swagger definitions

## Configuration

### ApiExceptionResolver

### Feign ErrorDecoder

### RestTemplate

TODO (not yet implemented)

