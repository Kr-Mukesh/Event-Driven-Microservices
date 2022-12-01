package com.mukesh.PaymentService.command.api.events;

import com.mukesh.CommonService.events.PaymentCancelledEvent;
import com.mukesh.CommonService.events.PaymentProcessEvent;
import com.mukesh.PaymentService.command.api.data.Payment;
import com.mukesh.PaymentService.command.api.data.PaymentRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PaymentEventHandler {

    private PaymentRepository paymentRepository;

    public PaymentEventHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @EventHandler
    public void on(PaymentProcessEvent event){
        Payment payment =
                Payment.builder()
                        .paymentId(event.getPaymentId())
                        .orderId(event.getOrderId())
                        .paymentStatus("COMPLETED")
                        .timeStamp(new Date())
                        .build();

        paymentRepository.save(payment);
    }

    @EventHandler
    public void on(PaymentCancelledEvent event){
        Payment payment
                = paymentRepository.findById(event.getPaymentId()).get();
        payment.setPaymentStatus(event.getPaymentStatus());

        paymentRepository.save(payment);
    }
}
