package com.mall.common.feign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.exception.BizException;
import com.mall.common.result.R;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.body() == null) {
            return defaultDecoder.decode(methodKey, response);
        }
        try (InputStream inputStream = response.body().asInputStream()) {
            R<?> result = objectMapper.readValue(inputStream, new TypeReference<R<Object>>() {});
            return new BizException(result.getCode(), result.getMessage());
        } catch (IOException e) {
            log.warn("Failed to decode Feign error response for {}: {}", methodKey, e.getMessage());
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
