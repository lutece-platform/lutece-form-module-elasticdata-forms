-- liquibase formatted sql
-- changeset elasticdata-forms:update_db_elasticdata-forms-2.0.0-2.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Structure for table elasticdata_forms_optionalstatus
--
-- No DROP here : an upgrade script must never destroy the indexation settings
-- already recorded by the back office users.
--

CREATE TABLE IF NOT EXISTS elasticdata_forms_optionalstatus (
id_optional_status_indexation int AUTO_INCREMENT,
id_form int default 0 NOT NULL,
id_status int default 0 NOT NULL,
PRIMARY KEY (id_optional_status_indexation)
);
