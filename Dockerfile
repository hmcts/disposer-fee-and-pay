 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.6
FROM hmctspublic.azurecr.io/base/java:21-distroless

USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/disposer-fee-and-pay.jar /opt/app/

CMD [ "disposer-fee-and-pay.jar" ]
