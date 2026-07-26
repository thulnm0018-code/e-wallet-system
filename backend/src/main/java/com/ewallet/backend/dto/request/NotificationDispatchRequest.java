package com.ewallet.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDispatchRequest {
    private String eventType;
}
