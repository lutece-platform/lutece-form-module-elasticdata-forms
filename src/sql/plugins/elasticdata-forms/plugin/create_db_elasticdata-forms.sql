-- liquibase formatted sql
-- changeset elasticdata-forms:create_db_elasticdata-forms.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Structure for table elasticdata_forms_optionalquestionresponse
--

DROP TABLE IF EXISTS elasticdata_forms_optionalquestionresponse;
CREATE TABLE elasticdata_forms_optionalquestionresponse (
id_optional_question_indexation int AUTO_INCREMENT,
id_form int default 0 NOT NULL,
id_question int default 0 NOT NULL,
PRIMARY KEY (id_optional_question_indexation)
);

DROP TABLE IF EXISTS workflowstatus_forms_optionalstatus;
CREATE TABLE workflowstatus_forms_optionalstatus (
id_optional_status_indexation int AUTO_INCREMENT,
id_form int default '0',
id_status int default '0',
PRIMARY KEY (id_optional_status_indexation)
);