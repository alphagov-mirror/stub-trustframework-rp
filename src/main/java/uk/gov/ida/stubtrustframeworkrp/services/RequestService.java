package uk.gov.ida.stubtrustframeworkrp.services;

import java.net.URI;
import java.util.Map;

public class RequestService {

    private final RedisService redisService;

    public RequestService(RedisService redisService) {
        this.redisService = redisService;
    }

    public void storeNonceAndState(URI uri) {
        Map<String, String> authenticationParams = QueryParameterHelper.splitQuery(uri.getQuery());
        String nonce = authenticationParams.get("nonce");
        String state = authenticationParams.get("state");
        String transactionId = authenticationParams.get("transaction-id");

        redisService.set(transactionId, state);
        redisService.set("state::" + state, nonce);
    }
}
