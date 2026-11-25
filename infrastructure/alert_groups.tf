data "azurerm_key_vault_secret" "disposer_fee_and_pay_support_email" {
  count        = local.deletion_alerts_enabled ? 1 : 0
  name         = "disposer-fee-and-pay-support-email"
  key_vault_id = data.azurerm_key_vault.disposer_key_vault.id
}

module "disposer-fee-and-pay-action-group" {
  count    = local.deletion_alerts_enabled ? 1 : 0
  source   = "git@github.com:hmcts/cnp-module-action-group"
  location = "global"
  env      = local.local_env

  resourcegroup_name     = local.resource_group_name
  action_group_name      = "disposer-fee-and-pay-support-${local.local_env}"
  short_name             = "dfp-alert"
  email_receiver_name    = "Fee and Pay Disposer Support Mailing List"
  email_receiver_address = data.azurerm_key_vault_secret.disposer_fee_and_pay_support_email[0].value
  tags                   = var.common_tags
}
