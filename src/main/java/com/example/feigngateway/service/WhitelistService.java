package com.example.feigngateway.service;

import com.example.feigngateway.config.GatewayWhitelistProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WhitelistService {
    
    private final GatewayWhitelistProperties whitelistProperties;
    
    public boolean isRequestAllowed(String serviceName, String pathInService) {
        if (!whitelistProperties.isEnabled()) {
            return true; // If whitelist is disabled, allow all requests
        }

        GatewayWhitelistProperties.ServiceConfig service = getServiceByName(serviceName);
        if (service == null) {
            return false;
        }

        return isPathMatched(pathInService, service.getEndpoints());
    }

    public String getTargetUrl(String serviceName, String pathInService) {
        GatewayWhitelistProperties.ServiceConfig service = getServiceByName(serviceName);
        if (service == null) {
            return null;
        }

        return service.getBaseUrl() + pathInService;
    }

    private GatewayWhitelistProperties.ServiceConfig getServiceByName(String serviceName) {
        List<GatewayWhitelistProperties.ServiceConfig> services = whitelistProperties.getServices();
        if (services == null) {
            return null;
        }
        for (GatewayWhitelistProperties.ServiceConfig s : services) {
            if (serviceName != null && serviceName.equals(s.getName())) {
                return s;
            }
        }
        return null;
    }
    
    private boolean isPathMatched(String requestPath, List<String> allowedEndpoints) {
        if (allowedEndpoints == null || allowedEndpoints.isEmpty()) {
            return false;
        }
        
        for (String endpoint : allowedEndpoints) {
            // Convert Spring path pattern to regex
            // Handle ** first to avoid it being converted by * replacement
            String regex = endpoint
                .replace("**", ".*")  // ** matches any number of path segments including /
                .replace("*", "[^/]*")  // * matches any characters except /
                .replace("{", "(")
                .replace("}", ")");
            
            // Ensure the regex matches the entire path
            if (!regex.startsWith("^")) {
                regex = "^" + regex;
            }
            if (!regex.endsWith("$")) {
                regex = regex + "$";
            }
            
            if (Pattern.matches(regex, requestPath)) {
                return true;
            }
        }
        
        return false;
    }
}
