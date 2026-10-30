package com.ahorra.features.push.service.impl;

import com.ahorra.features.push.model.PushSubscription;
import com.ahorra.features.push.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PushSender {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;
    private final String publicKey;
    private final String privateKey;
    private final String subject;

    private PushService pushService;

    public PushSender(PushSubscriptionRepository pushSubscriptionRepository,
                      ObjectMapper objectMapper,
                      @Value("${app.push.vapid-public-key:}") String publicKey,
                      @Value("${app.push.vapid-private-key:}") String privateKey,
                      @Value("${app.push.subject:}") String subject) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.subject = subject;
    }

    @PostConstruct
    void init() {
        if (publicKey.isBlank() || privateKey.isBlank() || subject.isBlank()) {
            log.warn("Web push is switched off: set VAPID_PUBLIC_KEY, VAPID_PRIVATE_KEY and VAPID_SUBJECT to turn it on");
            return;
        }
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            pushService = new PushService(publicKey, privateKey, subject);
        } catch (Exception ex) {
            log.error("Web push could not start: check the VAPID keys", ex);
        }
    }

    public boolean isEnabled() {
        return pushService != null;
    }

    @Transactional
    public int send(Long userId, String title, String body) {
        if (pushService == null) {
            return 0;
        }
        List<PushSubscription> devices = pushSubscriptionRepository.findAllByUserIdOrderByIdAsc(userId);
        int delivered = 0;
        for (PushSubscription device : devices) {
            try {
                byte[] payload = objectMapper.writeValueAsString(Map.of("title", title, "body", body))
                        .getBytes(StandardCharsets.UTF_8);
                Notification notification = new Notification(device.getEndpoint(), device.getP256dh(), device.getAuth(), payload);
                int status = pushService.send(notification).getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    pushSubscriptionRepository.delete(device);
                    log.info("Removed a push device that is no longer valid for user {}", userId);
                } else if (status >= 200 && status < 300) {
                    delivered++;
                } else {
                    log.warn("Push service answered {} for a device of user {}", status, userId);
                }
            } catch (Exception ex) {
                log.warn("Could not send a push to a device of user {}", userId, ex);
            }
        }
        return delivered;
    }
}
