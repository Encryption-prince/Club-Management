package com.payment.gateway.Payment.Gateway.Service;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.api.secret}")
    private String razorpaySecret;
    public PaymentService(@Value("${razorpay.keyId}") String keyId,
                          @Value("${razorpay.keySecret}") String keySecret) throws Exception {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public String createOrder(int amount, String currency, String receipt) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Razorpay accepts amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(orderRequest);
        return order.toString(); // Send this order details to frontend
    }

    public boolean verifyPayment(String razorpayOrderId, String paymentId, String signature) {
            try {
                String payload = razorpayOrderId + "|" + paymentId;
                return Utils.verifySignature(payload, signature, razorpaySecret);
            } catch (Exception e) {
                return false;
        }
    }

}
