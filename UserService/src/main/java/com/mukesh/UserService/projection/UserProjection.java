package com.mukesh.UserService.projection;

import com.mukesh.CommonService.model.CardDetails;
import com.mukesh.CommonService.model.User;
import com.mukesh.CommonService.queries.GetUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

        @QueryHandler
        public User getUserPaymentDetails(GetUserPaymentDetailsQuery query){
           // Ideally Get The Details From The DB.
            CardDetails cardDetails
                    = CardDetails.builder()
                    .name("Mukesh Kumar")
                    .validUntilYear(2022)
                    .validUntilMonth(01)
                    .cardNumber("123456789")
                    .cvv(111)
                    .build();

            return User.builder()
                    .userId(query.getUserId())
                    .firstName("Mukesh")
                    .lastName("Kumar")
                    .cardDetails(cardDetails)
                    .build();

        }
}
