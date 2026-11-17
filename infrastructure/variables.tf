variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}

variable "enable_deletion_not_started_alerts" {
  type        = bool
  default     = false
  description = "Alert when Fee and Pay Deletion - Started is missing from the daily window"
}

variable "enable_deletion_not_completed_alerts" {
  type        = bool
  default     = false
  description = "Alert when Fee and Pay Deletion starts but does not complete"
}

variable "enable_deletion_failure_alerts" {
  type        = bool
  default     = false
  description = "Alert when Fee and Pay Deletion completes as Failed or Partial Success"
}


