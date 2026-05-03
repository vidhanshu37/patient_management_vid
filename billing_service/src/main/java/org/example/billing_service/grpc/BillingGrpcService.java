package org.example.billing_service.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.BillingResponse;
import org.example.BillingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    // Implement gRPC service methods here
    @Override
    public void createBillingAccount(org.example.BillingRequest billingRequest,
                                     StreamObserver<org.example.BillingResponse> responseObserver) {
        log.info("Received create billing request: {}", billingRequest.toString());

        // Business logic here
        BillingResponse response = BillingResponse.newBuilder()
                .setBillingAccountId("1234")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
