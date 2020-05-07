package com.ft.sling.core.apm.services.impl;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
import com.ft.sling.core.apm.services.ApmAgent;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service(value = ApmAgent.class)
@Component(immediate = true, policy = ConfigurationPolicy.OPTIONAL, metatype = true, label="AppDynamics APM Logging Service")
public class AppDynamicsAgentImpl implements ApmAgent {

    private static final Logger log = LoggerFactory.getLogger(AppDynamicsAgentImpl.class);


    @Property(label="Enabled", description = "Use this agent to send APM data?", boolValue = false)
    private static final String PROPERTY_ENABLED = "enabled";
    private boolean enabled = false;

    @Override
    public void sendPageMetric(HttpServletRequest request, String transaction) {
        Map<String, String> keyVsValue = new HashMap<String, String>() {{
            put("sling request", transaction);
        }};
        AppdynamicsAgent.getEventPublisher().publishInfoEvent("Sling Request", keyVsValue);
    }

    @Override
    public Object startComponentMetric(String transaction) {
        Transaction appdTransaction = null;
        try {
            appdTransaction = AppdynamicsAgent.startTransaction("Sling Request", null, EntryTypes.HTTP, false);
        } finally {
            if (appdTransaction != null) {
                appdTransaction.endSegment();
            }
        }
        return appdTransaction;
    }

    @Override
    public void endComponentMetric(Object span) {
        if(span==null) {
            return;
        }
        Transaction appdSpan = (Transaction)span;
        appdSpan.end();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean logComponents() {
        return false;
    }

    @Activate
    protected void activate(final BundleContext bundleContext, final Map<String, Object> properties) {
        configureService(properties);
    }

    @Modified
    private void configureService(Map<String, Object> properties) {
        enabled = PropertiesUtil.toBoolean(properties.get(PROPERTY_ENABLED), false);
        log.info("enabled:" + enabled);

    }
}
