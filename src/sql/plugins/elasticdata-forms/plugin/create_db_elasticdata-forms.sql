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
