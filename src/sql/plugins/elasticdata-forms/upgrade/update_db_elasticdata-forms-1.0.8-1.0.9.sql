DROP TABLE IF EXISTS workflowstatus_forms_optionalstatus;
CREATE TABLE workflowstatus_forms_optionalstatus (
id_optional_status_indexation int AUTO_INCREMENT,
id_form int default '0',
id_status int default '0',
PRIMARY KEY (id_optional_status_indexation)
);