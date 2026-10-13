provider "azurerm" {
  features {}
}

locals {
  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ? "aat" : "saat") : var.env
  vault_name = "disposer-${local.local_env}"
  resource_group_name = "disposer-${local.local_env}"
  app_insights_name = "disposer-${local.local_env}"
  process_name = "Fee and Pay Deletion"
  deletion_alerts_enabled = var.enable_deletion_not_started_alerts || var.enable_deletion_not_completed_alerts || var.enable_deletion_failure_alerts
}

data "azurerm_key_vault" "disposer_key_vault" {
  name                = local.vault_name
  resource_group_name = local.resource_group_name
}

