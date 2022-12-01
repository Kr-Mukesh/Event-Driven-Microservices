package com.mukesh.PaymentService.command.api.aggregate;

import com.mukesh.CommonService.command.CancelPaymentCommand;
import com.mukesh.CommonService.command.ValidatePaymentCommand;
import com.mukesh.CommonService.events.PaymentCancelledEvent;
import com.mukesh.CommonService.events.PaymentProcessEvent;
import com.mukesh.CommonService.model.CardDetails;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
@Slf4j
public class PaymentAggregate {

    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private String paymentStatus;

    public PaymentAggregate() {
    }

    public PaymentAggregate(ValidatePaymentCommand validatePaymentCommand) {
        // validate the payment details
        // publish the payment process event
        log.info("Executing Validate Payment Command for " +
                        "Order Id: {} and Payment Id: {}",
                validatePaymentCommand.getOrderId(), validatePaymentCommand.getPaymentId());
        PaymentProcessEvent paymentProcessEvent
                = new PaymentProcessEvent(
                validatePaymentCommand.getPaymentId(), validatePaymentCommand.getOrderId()
        );
        AggregateLifecycle.apply(paymentProcessEvent);
        log.info("Payment Process Event Applied");
    }

    @EventSourcingHandler
    public void on(PaymentProcessEvent event){
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
    }
    @CommandHandler
    public void handle(CancelPaymentCommand cancelPaymentCommand){
        PaymentCancelledEvent paymentCancelledEvent
                = new PaymentCancelledEvent();
        BeanUtils.copyProperties(cancelPaymentCommand, paymentCancelledEvent);

        AggregateLifecycle.apply(paymentCancelledEvent);
    }

    @EventSourcingHandler
    public void on(PaymentCancelledEvent event){
            this.paymentStatus = event.getPaymentStatus();
    }

}
