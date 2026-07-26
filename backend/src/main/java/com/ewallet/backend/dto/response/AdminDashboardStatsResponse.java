package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class AdminDashboardStatsResponse {

    private AdminDashboardResponse dashboard;

    private RedisTelemetry redisTelemetry;

    private RabbitTelemetry rabbitTelemetry;

    private DeploymentsStatus deployments;

    @Getter
    @Builder
    public static class RedisTelemetry {
        private double hitRate;
        private int memoryMb;
        private int connectedClients;
    }

    @Getter
    @Builder
    public static class RabbitTelemetry {
        private int incomingRate;
        private int ready;
        private int unacked;
        private int activeConsumers;
    }

    @Getter
    @Builder
    public static class DeploymentsStatus {
        private String apiGateway;
        private String authService;
        private String database;
        private String rabbitmq;
    }
}
