#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-disposer}"
CRONJOB="${CRONJOB:-disposer-fee-and-pay-job}"
RUN_ID="$(date -u +%Y%m%d%H%M%S)"
JOB_NAME="${JOB_NAME:-${CRONJOB}-manual-${RUN_ID}}"

all_active_jobs="$(
  kubectl -n "${NAMESPACE}" get jobs \
    -o jsonpath='{range .items[?(@.status.active>0)]}{.metadata.name}{"\n"}{end}'
)"
active_jobs="$(printf '%s\n' "${all_active_jobs}" | grep "^${CRONJOB}" || true)"

if [[ -n "${active_jobs}" ]]; then
  echo "Manual disposer run blocked: active ${CRONJOB} job already running in namespace ${NAMESPACE}."
  echo "${active_jobs}"
  exit 1
fi

kubectl -n "${NAMESPACE}" create job "${JOB_NAME}" --from="cronjob/${CRONJOB}"
echo "Manual disposer job created: ${JOB_NAME}"
