package makelainen.mobro

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, MapperFeature, SerializationFeature, ObjectMapper => OM}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
 * ObjectMapper configuration for (Jackson) JSON (de)serialization.
 */
trait ObjectMapper {

  val objectMapper: com.fasterxml.jackson.databind.ObjectMapper = new OM().registerModule(DefaultScalaModule)
    .registerModule(new JavaTimeModule)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
