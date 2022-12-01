package com.mukesh.ProductService.command.api.aggregate;

import com.mukesh.ProductService.command.api.commands.CreateProductCommand;
import com.mukesh.ProductService.command.api.events.ProductCreateEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {
        // we can perform all the validation here
        ProductCreateEvent productCreateEvent =
                new ProductCreateEvent();
        BeanUtils.copyProperties(createProductCommand, productCreateEvent);

        AggregateLifecycle.apply(productCreateEvent);
    }

    public ProductAggregate() {
    }

    @EventSourcingHandler
    public void on(ProductCreateEvent productCreateEvent){
        this.quantity = productCreateEvent.getQuantity();
        this.productId = productCreateEvent.getProductId();
        this.name = productCreateEvent.getName();
        this.price = productCreateEvent.getPrice();
    }
}
