# App infrastructure

This folder provisions Azure Monitor alerts for the Fee and Pay disposer CronJob.

The job emits Application Insights custom events (`Fee and Pay Deletion - Started` / `- Completed - Success|Partial Success|Failed`). Terraform watches those events and emails the support mailbox.

Before enabling alerts, add Key Vault secret `disposer-fee-and-pay-support-email` to `disposer-{env}`. Then set in the environment tfvars:

```hcl
enable_deletion_not_started_alerts   = true
enable_deletion_not_completed_alerts = true
enable_deletion_failure_alerts       = true
```

Alerts stay disabled by default so preview/AAT pipelines do not fail before the mailbox secret exists.
