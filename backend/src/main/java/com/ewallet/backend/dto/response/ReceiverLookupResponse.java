package com.ewallet.backend.dto.response;

import com.ewallet.backend.enums.WalletStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiverLookupResponse {

    private String name;

    private Long walletId;

    private WalletStatus walletStatus;

}
