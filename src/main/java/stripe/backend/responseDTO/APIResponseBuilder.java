package stripe.backend.responseDTO;

public final class APIResponseBuilder {
    public APIResponseBuilder() {

    }

    public static GenericResponse build(boolean success) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setSuccess(success);
        return genericResponse;
    }

    public static GenericResponse build(boolean success, Object data) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setSuccess(success);
        genericResponse.setData(data);
        return genericResponse;
    }

    public static GenericResponse build(Object data) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setData(data);
        return genericResponse;
    }

    public static GenericResponse build(boolean success, String message) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setSuccess(success);
        genericResponse.setMessage(message);
        return genericResponse;
    }

    public static GenericResponse build(boolean success, Object data, String message) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setSuccess(success);
        genericResponse.setData(data);
        genericResponse.setMessage(message);
        return genericResponse;
    }
}
