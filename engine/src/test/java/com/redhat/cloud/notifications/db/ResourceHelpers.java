package com.redhat.cloud.notifications.db;

import com.redhat.cloud.notifications.db.repositories.EmailAggregationRepository;
import com.redhat.cloud.notifications.models.AggregationEmailTemplate;
import com.redhat.cloud.notifications.models.Application;
import com.redhat.cloud.notifications.models.Bundle;
import com.redhat.cloud.notifications.models.EmailAggregation;
import com.redhat.cloud.notifications.models.Endpoint;
import com.redhat.cloud.notifications.models.EndpointType;
import com.redhat.cloud.notifications.models.Event;
import com.redhat.cloud.notifications.models.EventType;
import com.redhat.cloud.notifications.models.InstantEmailTemplate;
import com.redhat.cloud.notifications.models.Template;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

import static com.redhat.cloud.notifications.models.EmailSubscriptionType.DAILY;
import static java.lang.Boolean.TRUE;

@ApplicationScoped
public class ResourceHelpers {

    @Inject
    EmailAggregationRepository emailAggregationRepository;

    @Inject
    EntityManager entityManager;

    // TODO NOTIF-603 Remove when switching to orgId
    public void addEmailAggregation(String accountId, String bundleName, String applicationName, JsonObject payload) {
        EmailAggregation aggregation = new EmailAggregation();
        aggregation.setAccountId(accountId);
        aggregation.setBundleName(bundleName);
        aggregation.setApplicationName(applicationName);
        aggregation.setPayload(payload);
        emailAggregationRepository.addEmailAggregation(aggregation);
    }

    public Boolean addEmailAggregation(String accountId, String orgId, String bundleName, String applicationName, JsonObject payload) {
        EmailAggregation aggregation = new EmailAggregation();
        aggregation.setAccountId(accountId);
        aggregation.setOrgId(orgId);
        aggregation.setBundleName(bundleName);
        aggregation.setApplicationName(applicationName);
        aggregation.setPayload(payload);
        return emailAggregationRepository.addEmailAggregation(aggregation);
    }

    @Transactional
    public Bundle createBundle(String bundleName) {
        Bundle bundle = new Bundle(bundleName, "A bundle");
        entityManager.persist(bundle);
        return bundle;
    }

    @Transactional
    public Application createApp(UUID bundleId, String appName) {
        Application app = new Application();
        app.setBundle(entityManager.find(Bundle.class, bundleId));
        app.setBundleId(bundleId);
        app.setName(appName);
        app.setDisplayName("The best app in the life");
        entityManager.persist(app);
        return app;
    }

    @Transactional
    public EventType createEventType(UUID appId, String eventTypeName) {
        EventType eventType = new EventType();
        eventType.setApplication(entityManager.find(Application.class, appId));
        eventType.setApplicationId(appId);
        eventType.setName(eventTypeName);
        eventType.setDisplayName("Policies will take care of the rules");
        eventType.setDescription("Policies is super cool, you should use it");
        entityManager.persist(eventType);
        return eventType;
    }

    @Transactional
    public Event createEvent(EventType eventType) {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setAccountId("account-id");
        event.setEventType(eventType);
        event.setEventTypeDisplayName(eventType.getDisplayName());
        event.setApplicationId(eventType.getApplication().getId());
        event.setApplicationDisplayName(eventType.getApplication().getDisplayName());
        event.setBundleId(eventType.getApplication().getBundle().getId());
        event.setBundleDisplayName(eventType.getApplication().getBundle().getDisplayName());
        entityManager.persist(event);
        return event;
    }

    @Transactional
    public Endpoint createEndpoint(EndpointType type, String subType) {
        Endpoint endpoint = new Endpoint();
        endpoint.setType(type);
        endpoint.setSubType(subType);
        endpoint.setName("endpoint-" + new SecureRandom().nextInt());
        endpoint.setDescription("Endpoint description");
        endpoint.setEnabled(TRUE);
        entityManager.persist(endpoint);
        return endpoint;
    }

    @Transactional
    public Template createTemplate(String name, String description, String data) {
        Template template = new Template();
        template.setName(name);
        template.setDescription(description);
        template.setData(data);
        entityManager.persist(template);
        return template;
    }

    @Transactional
    public InstantEmailTemplate createInstantEmailTemplate(UUID eventTypeId, UUID subjectTemplateId, UUID bodyTemplateId, boolean enabled) {
        InstantEmailTemplate instantEmailTemplate = new InstantEmailTemplate();
        instantEmailTemplate.setEventType(entityManager.find(EventType.class, eventTypeId));
        instantEmailTemplate.setEventTypeId(eventTypeId);
        instantEmailTemplate.setSubjectTemplate(entityManager.find(Template.class, subjectTemplateId));
        instantEmailTemplate.setSubjectTemplateId(subjectTemplateId);
        instantEmailTemplate.setBodyTemplate(entityManager.find(Template.class, bodyTemplateId));
        instantEmailTemplate.setBodyTemplateId(bodyTemplateId);
        entityManager.persist(instantEmailTemplate);
        return instantEmailTemplate;
    }

    @Transactional
    public AggregationEmailTemplate createAggregationEmailTemplate(UUID appId, UUID subjectTemplateId, UUID bodyTemplateId, boolean enabled) {
        AggregationEmailTemplate aggregationEmailTemplate = new AggregationEmailTemplate();
        aggregationEmailTemplate.setApplication(entityManager.find(Application.class, appId));
        aggregationEmailTemplate.setApplicationId(appId);
        aggregationEmailTemplate.setSubjectTemplate(entityManager.find(Template.class, subjectTemplateId));
        aggregationEmailTemplate.setSubjectTemplateId(subjectTemplateId);
        aggregationEmailTemplate.setBodyTemplate(entityManager.find(Template.class, bodyTemplateId));
        aggregationEmailTemplate.setBodyTemplateId(bodyTemplateId);
        aggregationEmailTemplate.setSubscriptionType(DAILY);
        entityManager.persist(aggregationEmailTemplate);
        return aggregationEmailTemplate;
    }

    @Transactional
    public void deleteAllEmailTemplates() {
        entityManager.createQuery("DELETE FROM InstantEmailTemplate").executeUpdate();
        entityManager.createQuery("DELETE FROM AggregationEmailTemplate").executeUpdate();
    }

    @Transactional
    public void deleteEndpoint(UUID id) {
        entityManager.createQuery("DELETE FROM Endpoint WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
