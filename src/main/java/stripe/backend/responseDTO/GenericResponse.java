package stripe.backend.responseDTO;

import lombok.Data;

import java.io.Serializable;

public @Data
class GenericResponse<T extends Serializable> {
    private boolean success;
    private String message;
    private Object data;
}
