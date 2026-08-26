-- liquibase formatted sql
-- changeset elasticdata-forms:update_db_elasticdata-forms-1.0.8-1.0.9.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- No DROP here : an upgrade script must never destroy the indexation settings
-- already recorded by the back office users.
--
CREATE TABLE IF NOT EXISTS elasticdata_forms_optionalstatus (
id_optional_status_indexation int AUTO_INCREMENT,
id_form int default '0',
id_status int default '0',
PRIMARY KEY (id_optional_status_indexation)
);