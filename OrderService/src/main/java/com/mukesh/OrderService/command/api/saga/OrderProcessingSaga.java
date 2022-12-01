package com.mukesh.OrderService.command.api.saga;

import com.mukesh.CommonService.command.*;
import com.mukesh.CommonService.events.*;
import com.mukesh.CommonService.model.User;
import com.mukesh.CommonService.queries.GetUserPaymentDetailsQuery;
import com.mukesh.OrderService.command.api.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
@Slf4j
public class OrderProcessingSaga {


    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;


    public OrderProcessingSaga() {
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent event) {
        log.info("Order Created Event in Saga for Order Id : {}", event.getOrderId());

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery
                = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;
        try {
            user = queryGateway.query(getUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)).join();

        } catch (Exception e) {
            log.error(e.getMessage());
            cancelOrderCommand(event.getOrderId());
        }

        ValidatePaymentCommand validatePaymentCommand
                = ValidatePaymentCommand.builder()
                .cardDetails(user.getCardDetails())
                .orderId(event.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .build();

        commandGateway.sendAndWait(validatePaymentCommand);
    }

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand cancelOrderCommand
                = new CancelOrderCommand(orderId);
        commandGateway.send(cancelOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    private void handle(PaymentProcessEvent event) {

        log.info("Payment Process Event in Saga for Order Id : {}",
                event.getOrderId());

        try {
//            if(true)
//                throw new Exception();

            ShipOrderCommand shipOrderCommand =
                    ShipOrderCommand.builder()
                            .shipmentId(UUID.randomUUID().toString())
                            .orderId(event.getOrderId())
                            .build();

            commandGateway.send(shipOrderCommand);
        } catch (Exception e) {
            log.error(e.getMessage());
            // if it will get error it's start the compensating transaction
            cancelPaymentCommand(event);
        }

    }

    private void cancelPaymentCommand(PaymentProcessEvent event) {
        CancelPaymentCommand cancelPaymentCommand
                = new CancelPaymentCommand(event.getPaymentId(),event.getOrderId());
        commandGateway.send(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderShippedEvent event){

        log.info("Order Shipped Event in Saga for Order Id : {}",
                event.getOrderId());

        CompleteOrderCommand completeOrderCommand
                = CompleteOrderCommand.builder()
                .orderId(event.getOrderId())
                .orderStatus("APPROVED")
                .build();

        commandGateway.send(completeOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCompletedEvent event){
        log.info("Order Completed Event in Saga for Order Id : {}",
                event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCancelledEvent event){
        log.info("Order Cancelled Event in Saga for Order Id : {}",
                event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentCancelledEvent event){
        log.info("Payment Cancelled Event in Saga for Order Id : {}",
                event.getOrderId());
        cancelOrderCommand(event.getOrderId());
    }
}
