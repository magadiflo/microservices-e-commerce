package dev.magadiflo.notification.app.repositories;

import dev.magadiflo.notification.app.models.documents.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
