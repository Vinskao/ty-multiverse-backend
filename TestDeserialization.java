import com.fasterxml.jackson.databind.ObjectMapper;
import tw.com.tymbackend.core.message.AsyncResultMessage;

public class TestDeserialization {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = "{\"requestId\":\"test-123\",\"status\":\"completed\",\"data\":[\"Test User\",\"Test Name\"],\"error\":null,\"timestamp\":\"2025-11-13T02:21:49.189577Z\",\"source\":\"consumer\"}";
            
            System.out.println("Testing deserialization...");
            AsyncResultMessage message = mapper.readValue(json, AsyncResultMessage.class);
            
            System.out.println("Success!");
            System.out.println("requestId: " + message.getRequestId());
            System.out.println("status: " + message.getStatus());
            System.out.println("data: " + message.getData());
            System.out.println("timestamp: " + message.getTimestamp());
            System.out.println("source: " + message.getSource());
            
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
