import com.fasterxml.jackson.databind.ObjectMapper;
import tw.com.tymbackend.core.message.AsyncResultMessage;

public class TestJson {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"requestId\":\"64b44866-12ef-4985-918c-7ed27b7a9614\",\"status\":\"completed\",\"data\":[\"Test User\",\"������������\"],\"error\":null,\"timestamp\":\"2025-11-13T02:21:49.189577Z\",\"source\":\"consumer\"}";
        
        AsyncResultMessage message = mapper.readValue(json, AsyncResultMessage.class);
        System.out.println("Success: " + message.getRequestId());
    }
}
