package productservice.models.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class InventoryUpdateRequest {

  @PositiveOrZero private int quantity;
}
