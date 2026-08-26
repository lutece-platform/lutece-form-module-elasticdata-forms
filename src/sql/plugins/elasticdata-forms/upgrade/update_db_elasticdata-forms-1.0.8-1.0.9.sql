-- liquibase formatted sql
-- changeset elasticdata-forms:update_db_elasticdata-forms-1.0.8-1.0.9.sql
-- preconditions onFail:MARK_RAN onError:WARN
DROP TABLE IF EXISTS workflowstatus_forms_optionalstatus;
CREATE TABLE workflowstatus_forms_optionalstatus (
id_optional_status_indexation int AUTO_INCREMENT,
id_form int default '0',
id_status int default '0',
PRIMARY KEY (id_optional_status_indexation)
);