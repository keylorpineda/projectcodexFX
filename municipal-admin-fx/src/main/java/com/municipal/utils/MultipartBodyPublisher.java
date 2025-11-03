package com.municipal.utils;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public final class MultipartBodyPublisher {

    private static final String BOUNDARY_PREFIX = "----MunicipalBoundary";

    private final String boundary;
    private final List<byte[]> parts;

    private MultipartBodyPublisher() {
        this.boundary = BOUNDARY_PREFIX + UUID.randomUUID();
        this.parts = new ArrayList<>();
    }

    public static MultipartBodyPublisher newBuilder() {
        return new MultipartBodyPublisher();
    }

    public MultipartBodyPublisher addPart(String name, String value) {
        if (value == null) {
            return this;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"")
                .append(name)
                .append("\"\r\n\r\n");
        builder.append(value).append("\r\n");
        parts.add(builder.toString().getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public MultipartBodyPublisher addBinaryPart(String name, String filename, String contentType, byte[] data) {
        Objects.requireNonNull(data, "data");
        StringBuilder builder = new StringBuilder();
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"")
                .append(name)
                .append("\"");
        if (filename != null && !filename.isBlank()) {
            builder.append("; filename=\"").append(filename).append("\"");
        }
        builder.append("\r\n");
        if (contentType != null && !contentType.isBlank()) {
            builder.append("Content-Type: ").append(contentType).append("\r\n");
        }
        builder.append("\r\n");
        byte[] header = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] footer = "\r\n".getBytes(StandardCharsets.UTF_8);
        parts.add(concat(header, data, footer));
        return this;
    }

    public HttpRequest.BodyPublisher build() {
        byte[] closing = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        return HttpRequest.BodyPublishers.ofByteArrays(Stream.concat(parts.stream(), Stream.of(closing))::iterator);
    }

    public String getBoundary() {
        return boundary;
    }

    private byte[] concat(byte[] first, byte[] middle, byte[] last) {
        byte[] result = new byte[first.length + middle.length + last.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(middle, 0, result, first.length, middle.length);
        System.arraycopy(last, 0, result, first.length + middle.length, last.length);
        return result;
    }
}
