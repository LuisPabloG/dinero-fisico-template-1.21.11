package com.ratonera.transfer;

import java.util.UUID;

public record PendingTransfer(
	UUID requestId,
	UUID senderId,
	UUID recipientId,
	String senderName,
	String recipientName,
	int amount
) {
}