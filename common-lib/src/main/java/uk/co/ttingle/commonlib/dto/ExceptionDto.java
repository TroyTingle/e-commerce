package uk.co.ttingle.commonlib.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class ExceptionDto {

  private String message;
  private Map<String, String> validationErrors;
}
