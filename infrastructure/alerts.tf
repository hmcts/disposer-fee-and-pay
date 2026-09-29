module "disposer-fee-and-pay-not-started-alert" {
  count  = var.enable_deletion_not_started_alerts ? 1 : 0
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  location = "uksouth"

  app_insights_name = local.app_insights_name

  alert_name = "disposer-fee-and-pay-not-started-${local.local_env}"
  alert_desc = "Triggers when Fee and Pay Deletion has not started in the expected daily window in disposer-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName == 'disposer-fee-and-pay' | where name == '${local.process_name} - Started' | limit 1"
  custom_email_subject       = "Alert: Fee and Pay Deletion has not started in disposer-${local.local_env}"
  frequency_in_minutes       = 1440
  time_window_in_minutes     = 1440
  severity_level             = "2"
  action_group_name          = module.disposer-fee-and-pay-action-group[0].action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold          = "1"
  resourcegroup_name         = local.resource_group_name
  common_tags                = var.common_tags
  enabled                    = var.enable_deletion_not_started_alerts
}

module "disposer-fee-and-pay-not-completed-alert" {
  count  = var.enable_deletion_not_completed_alerts ? 1 : 0
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  location = "uksouth"

  app_insights_name = local.app_insights_name

  alert_name = "disposer-fee-and-pay-not-completed-${local.local_env}"
  alert_desc = "Triggers when Fee and Pay Deletion has started but not completed in disposer-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName == 'disposer-fee-and-pay' | where name startswith '${local.process_name}' | where timestamp >= ago(60m) | order by timestamp asc | extend prevName = prev(name) | where prevName == '${local.process_name} - Started' and name startswith '${local.process_name} - Completed'"
  custom_email_subject       = "Alert: Fee and Pay Deletion has not completed in disposer-${local.local_env}"
  frequency_in_minutes       = 15
  time_window_in_minutes     = 60
  severity_level             = "2"
  action_group_name          = module.disposer-fee-and-pay-action-group[0].action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold          = "1"
  resourcegroup_name         = local.resource_group_name
  common_tags                = var.common_tags
  enabled                    = var.enable_deletion_not_completed_alerts
}

module "disposer-fee-and-pay-failure-alert" {
  count  = var.enable_deletion_failure_alerts ? 1 : 0
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  location = "uksouth"

  app_insights_name = local.app_insights_name

  alert_name = "disposer-fee-and-pay-failure-${local.local_env}"
  alert_desc = "Triggers when Fee and Pay Deletion fails or partially succeeds in disposer-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName == 'disposer-fee-and-pay' | where name startswith '${local.process_name}' and (name endswith ' - Partial Success' or name endswith ' - Failed' or name endswith ' - Audit Failed')"
  custom_email_subject       = "Alert: Fee and Pay Deletion failed in disposer-${local.local_env}"
  frequency_in_minutes       = 15
  time_window_in_minutes     = 1440
  severity_level             = "2"
  action_group_name          = module.disposer-fee-and-pay-action-group[0].action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  resourcegroup_name         = local.resource_group_name
  common_tags                = var.common_tags
  enabled                    = var.enable_deletion_failure_alerts
}
