package com.municipal.responses;

public record BinaryFileResponse(byte[] data, String suggestedFileName) {
}
