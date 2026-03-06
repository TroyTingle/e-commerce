package uk.co.ttingle.commonlib.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ExceptionDto {

  private String message;
  private Map<String, String> validationErrors;
}
